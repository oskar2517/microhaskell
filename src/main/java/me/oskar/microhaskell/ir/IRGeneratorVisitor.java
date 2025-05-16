package me.oskar.microhaskell.ir;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.ast.visitor.Visitor;
import me.oskar.microhaskell.evaluation.expression.*;

import java.util.*;

public class IRGeneratorVisitor implements Visitor<Expression> {

    private String currentFunctionName = null;
    private final Map<String, Expression> dispatchedLambdaBodies = new HashMap<>();
    private Map<String, Integer> dispatchedLambdaIds;

    private static final Expression Y_COMBINATOR = new Lambda("f",
            new Application(
                    new Lambda("x",
                            new Application(
                                    new Variable("f"),
                                    new Application(new Variable("x"), new Variable("x"))
                            )
                    ),
                    new Lambda("x",
                            new Application(
                                    new Variable("f"),
                                    new Application(new Variable("x"), new Variable("x"))
                            )
                    )
            )
    );

    private static final String MUTUAL_DISPATCHER_NAME = "<mutual_dispatch>";

    @Override
    public Expression visit(AnonymousFunctionNode anonymousFunctionNode) {
        var body = anonymousFunctionNode.getBody().accept(this);

        for (var i = anonymousFunctionNode.getParameters().size() - 1; i >= 0; i--) {
            var param = (IdentifierNode) anonymousFunctionNode.getParameters().get(i);
            body = new Lambda(param.getName(), body);
        }

        return body;
    }

    @Override
    public Expression visit(FunctionApplicationNode functionApplicationNode) {
        return new Application(
                functionApplicationNode.getFunction().accept(this),
                functionApplicationNode.getArgument().accept(this)
        );
    }

    @Override
    public Expression visit(FunctionDefinitionNode functionDefinitionNode) {
        if (functionDefinitionNode.isAppliedMutuallyRecursively()) {
            var body = functionDefinitionNode.getBody().accept(this);

            for (int i = functionDefinitionNode.getParameters().size() - 1; i >= 0; i--) {
                var param = (IdentifierNode) functionDefinitionNode.getParameters().get(i);
                body = new Lambda(param.getName(), body);
            }

            dispatchedLambdaBodies.put(functionDefinitionNode.getName(), body);

            return new Application(new Variable(MUTUAL_DISPATCHER_NAME), new IntLiteral(functionDefinitionNode.getDispatchId()));
        }

        currentFunctionName = functionDefinitionNode.getName();

        var body = functionDefinitionNode.getBody().accept(this);

        for (int i = functionDefinitionNode.getParameters().size() - 1; i >= 0; i--) {
            var param = (IdentifierNode) functionDefinitionNode.getParameters().get(i);
            body = new Lambda(param.getName(), body);
        }

        if (functionDefinitionNode.isAppliedRecursively()) {
            body = new Lambda(currentFunctionName, body);
            body = new Application(Y_COMBINATOR, body);
        }

        return body;
    }

    @Override
    public Expression visit(IdentifierNode identifierNode) {
        var name = identifierNode.getName();

        if (dispatchedLambdaIds.containsKey(name)) {
            var dispatchedLambdaId = dispatchedLambdaIds.get(name);

            return new Application(new Variable(MUTUAL_DISPATCHER_NAME), new IntLiteral(dispatchedLambdaId));
        }

        if (name.equals(currentFunctionName)) {
            return new Variable(currentFunctionName);
        }

        return new Variable(name);
    }


    @Override
    public Expression visit(IfNode ifNode) {
        return new Application(new Application(new Application(new Variable("if"),
                ifNode.getCondition().accept(this)),
                ifNode.getConsequence().accept(this)),
                ifNode.getAlternative().accept(this));
    }

    @Override
    public Expression visit(IntLiteralNode intLiteralNode) {
        return new IntLiteral(intLiteralNode.getValue());
    }

    @Override
    public Expression visit(LetNode letNode) {
        var bindings = letNode.getBindings();

        var body = letNode.getExpression().accept(this);

        for (var binding : bindings.reversed()) {
            body = new Application(new Lambda(binding.getName(), body), binding.accept(this));
        }

        return body;
    }

    @Override
    public Expression visit(ProgramNode programNode) {
        var lambdaCollector = new MutuallyRecursiveLambdaCollectorVisitor();
        programNode.accept(lambdaCollector);
        dispatchedLambdaIds = lambdaCollector.getLambdaIds();

        var functionIRs = new LinkedHashMap<String, Expression>();
        for (var d : programNode.getBindings()) {
            functionIRs.put(d.getName(), d.accept(this));
        }

        Expression dispatcherBody = null;

        for (var e : dispatchedLambdaBodies.entrySet()) {
            if (dispatcherBody == null) {
                dispatcherBody = e.getValue();
            } else {
                var dispatchId = dispatchedLambdaIds.get(e.getKey());

                var condition = new Application(
                        new Application(new Variable("=="), new Variable("tag")),
                        new IntLiteral(dispatchId)
                );

                dispatcherBody = new Application(
                        new Application(
                                new Application(new Variable("if"), condition),
                                e.getValue()),
                        dispatcherBody);
            }
        }

        dispatcherBody = new Lambda("tag", dispatcherBody);

        var dispatcher = new Application(Y_COMBINATOR, new Lambda(MUTUAL_DISPATCHER_NAME, dispatcherBody));

        Expression body = new Variable("main");
        for (var name : functionIRs.keySet().stream().toList().reversed()) {
            var expr = functionIRs.get(name);
            body = new Application(new Lambda(name, body), expr);
        }

        body = new Application(
                new Lambda(MUTUAL_DISPATCHER_NAME, body),
                dispatcher
        );

        return body;
    }


}
