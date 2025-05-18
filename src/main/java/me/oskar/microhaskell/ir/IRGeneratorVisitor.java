package me.oskar.microhaskell.ir;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.ast.visitor.Visitor;
import me.oskar.microhaskell.evaluation.expression.*;
import me.oskar.microhaskell.table.BindingEntry;
import me.oskar.microhaskell.table.SymbolTable;

import java.util.*;

public class IRGeneratorVisitor implements Visitor<Expression> {

    private final Map<Integer, Expression> dispatchedLambdaBodies;

    private SymbolTable currentTable;

    public IRGeneratorVisitor(SymbolTable currentTable) {
        this(currentTable, new HashMap<>());
    }

    private IRGeneratorVisitor(SymbolTable currentTable, Map<Integer, Expression> dispatchedLambdaBodies) {
        this.currentTable = currentTable;
        this.dispatchedLambdaBodies = dispatchedLambdaBodies;
    }

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
    private static final String MUTUAL_DISPATCHER_TAG = "<tag>";

    private Expression generateFunctionBody(FunctionNode function, IRGeneratorVisitor visitor) {
        var body = function.getBody().accept(visitor);

        for (var i = function.getParameters().size() - 1; i >= 0; i--) {
            var param = (IdentifierNode) function.getParameters().get(i);
            body = new Lambda(param.getName(), body);
        }

        return body;
    }

    @Override
    public Expression visit(AnonymousFunctionNode anonymousFunctionNode) {
        return generateFunctionBody(anonymousFunctionNode, this);
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
        var entry = (BindingEntry) currentTable.lookup(functionDefinitionNode.getName());

        var localIrGenerator = new IRGeneratorVisitor(entry.getLocalTable(), dispatchedLambdaBodies);

        var body = generateFunctionBody(functionDefinitionNode, localIrGenerator);

        if (entry.isAppliedMutuallyRecursively()) {
            dispatchedLambdaBodies.put(entry.getDispatchId(), body);

            return new Application(new Variable(MUTUAL_DISPATCHER_NAME),
                    new IntLiteral(functionDefinitionNode.getDispatchId()));
        }

        if (entry.isAppliedRecursively()) {
            return new Application(Y_COMBINATOR, new Lambda(functionDefinitionNode.getName(), body));
        }

        return body;
    }

    @Override
    public Expression visit(IdentifierNode identifierNode) {
        var name = identifierNode.getName();

        var entry = currentTable.lookup(name);

        if (entry != null && entry instanceof BindingEntry be && be.isAppliedMutuallyRecursively()) {
            return new Application(new Variable(MUTUAL_DISPATCHER_NAME), new IntLiteral(be.getDispatchId()));
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
        Expression body = new Variable("main");
        for (var binding : programNode.getBindings().reversed()) {
            body = new Application(new Lambda(binding.getName(), body), binding.accept(this));
        }

        // Only add dispatcher when necessary
        if (dispatchedLambdaBodies.isEmpty()) return body;

        Expression dispatcherBody = null;

        for (var e : dispatchedLambdaBodies.entrySet()) {
            if (dispatcherBody == null) {
                dispatcherBody = e.getValue();
            } else {
                var dispatchId = e.getKey();

                var condition = new Application(
                        new Application(new Variable("=="), new Variable(MUTUAL_DISPATCHER_TAG)),
                        new IntLiteral(dispatchId)
                );

                dispatcherBody = new Application(
                        new Application(
                                new Application(new Variable("if"), condition),
                                e.getValue()),
                        dispatcherBody);
            }
        }

        dispatcherBody = new Lambda(MUTUAL_DISPATCHER_TAG, dispatcherBody);

        var dispatcher = new Application(Y_COMBINATOR, new Lambda(MUTUAL_DISPATCHER_NAME, dispatcherBody));

        body = new Application(
                new Lambda(MUTUAL_DISPATCHER_NAME, body),
                dispatcher
        );

        return body;
    }
}
