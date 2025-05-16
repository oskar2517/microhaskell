package me.oskar.microhaskell.ir;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.ast.visitor.Visitor;
import me.oskar.microhaskell.evaluation.expression.*;

import java.util.*;

public class IRGeneratorVisitor implements Visitor<Expression> {

    private String currentFunctionName = null;
    private String recursiveAlias = null;
    private final Map<String, LambdaDispatch> dispatchedLambdas = new HashMap<>();
    private Map<String, Integer> dispatchedLambdaIds;
    private boolean insideDispatcher = false;

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

    @Override
    public Expression visit(AnonymousFunctionNode anonymousFunctionNode) {
        var body = anonymousFunctionNode.getBody().accept(this);

        for (int i = anonymousFunctionNode.getParameters().size() - 1; i >= 0; i--) {
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

            Expression body;
            if (functionDefinitionNode.isAppliedMutuallyRecursively()) {
                boolean previousInsideDispatcher = insideDispatcher;
                insideDispatcher = true;
                body = functionDefinitionNode.getBody().accept(this);
                insideDispatcher = previousInsideDispatcher;
            } else {
                body = functionDefinitionNode.getBody().accept(this);
            }

            for (int i = functionDefinitionNode.getParameters().size() - 1; i >= 0; i--) {
                var param = (IdentifierNode) functionDefinitionNode.getParameters().get(i);
                body = new Lambda(param.getName(), body);
            }

            dispatchedLambdas.put(functionDefinitionNode.getName(), new LambdaDispatch(functionDefinitionNode.getDispatchId(), body));

            return new Application(new Variable("dispatch"), new IntLiteral(functionDefinitionNode.getDispatchId()));
        }

        var previousFunctionName = currentFunctionName;
        var previousAlias = recursiveAlias;

        currentFunctionName = functionDefinitionNode.getName();
        recursiveAlias = functionDefinitionNode.isAppliedRecursively()
                ? "__rec_%s".formatted(currentFunctionName)
                : null;

        var body = functionDefinitionNode.getBody().accept(this);

        for (int i = functionDefinitionNode.getParameters().size() - 1; i >= 0; i--) {
            var param = (IdentifierNode) functionDefinitionNode.getParameters().get(i);
            body = new Lambda(param.getName(), body);
        }

        if (functionDefinitionNode.isAppliedRecursively()) {
            body = new Lambda(recursiveAlias, body);
            body = new Application(Y_COMBINATOR, body);
        }

        currentFunctionName = previousFunctionName;
        recursiveAlias = previousAlias;

        return body;
    }

    @Override
    public Expression visit(IdentifierNode identifierNode) {
        String name = identifierNode.getName();

        if (dispatchedLambdaIds.containsKey(name)) {
            int dispatchedLambdaId = dispatchedLambdaIds.get(name);

            String dispatcherVar = insideDispatcher ? "__rec_dispatch" : "dispatch";

            return new Application(new Variable(dispatcherVar), new IntLiteral(dispatchedLambdaId));
        }

        if (name.equals(currentFunctionName) && recursiveAlias != null) {
            return new Variable(recursiveAlias);
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

        Expression dispatchBody = null;

        var sortedLambdas = dispatchedLambdas.values().stream()
                .sorted(Comparator.comparingInt(LambdaDispatch::id))
                .toList();

        insideDispatcher = true;
        for (var l : sortedLambdas.reversed()) {
            Expression tagEq = new Application(
                    new Application(new Variable("=="), new Variable("tag")),
                    new IntLiteral(l.id())
            );

            Expression lambdaExpr = l.lambda();
            dispatchBody = dispatchBody == null
                    ? lambdaExpr
                    : new Application(
                    new Application(
                            new Application(new Variable("if"), tagEq),
                            lambdaExpr),
                    dispatchBody);
        }
        insideDispatcher = false;

        dispatchBody = new Lambda("tag", dispatchBody);
        Expression dispatcher = new Lambda("__rec_dispatch", dispatchBody);
        dispatcher = new Application(Y_COMBINATOR, dispatcher);


        Expression body = new Variable("main");
        for (String name : functionIRs.keySet().stream().toList().reversed()) {
            var expr = functionIRs.get(name);
            body = new Application(new Lambda(name, body), expr);
        }

        body = new Application(
                new Lambda("dispatch", body),
                dispatcher
        );

        return body;
    }


}
