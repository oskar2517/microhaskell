package me.oskar.microhaskell.ir;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.ast.visitor.Visitor;
import me.oskar.microhaskell.evaluation.expression.*;
import me.oskar.microhaskell.table.BindingEntry;
import me.oskar.microhaskell.table.SymbolTable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IrGeneratorVisitor implements Visitor<Expression> {

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

    private final SymbolTable symbolTable;
    private final Set<String> recursionTargets;
    private final Map<Integer, Expression> dispatchedLambdaBodies;

    public IrGeneratorVisitor(SymbolTable symbolTable) {
        this(symbolTable, new HashSet<>(), new HashMap<>());
    }

    public IrGeneratorVisitor(SymbolTable symbolTable,
                              Set<String> recursionTargets,
                              Map<Integer, Expression> dispatchedLambdaBodies) {
        this.symbolTable = symbolTable;
        this.recursionTargets = recursionTargets;
        this.dispatchedLambdaBodies = dispatchedLambdaBodies;
    }

    private Expression generateFunctionBody(FunctionNode function, IrGeneratorVisitor visitor) {
        var body = function.getBody().accept(visitor);

        for (var i = function.getParameters().size() - 1; i >= 0; i--) {
            var param = (IdentifierNode) function.getParameters().get(i);
            body = new Lambda(param.getName(), body);
        }

        return body;
    }

    @Override
    public Expression visit(AnonymousFunctionNode anonymousFunctionNode) {
        if (symbolTable != anonymousFunctionNode.getLocalTable()) {
            var localIrGeneratorVisitor = new IrGeneratorVisitor(anonymousFunctionNode.getLocalTable(), recursionTargets,
                    dispatchedLambdaBodies);

            return anonymousFunctionNode.accept(localIrGeneratorVisitor);
        }

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
        var entry = (BindingEntry) symbolTable.lookup(functionDefinitionNode.getName());

        var localRecursionTargets = recursionTargets;
        if (entry.isAppliedSelfRecursively() || entry.isAppliedMutuallyRecursively()) {
            localRecursionTargets = new HashSet<>(recursionTargets);
            localRecursionTargets.add(functionDefinitionNode.getName());
        }

        var localIrGeneratorVisitor = new IrGeneratorVisitor(entry.getLocalTable(), localRecursionTargets,
                dispatchedLambdaBodies);

        var body = generateFunctionBody(functionDefinitionNode, localIrGeneratorVisitor);

        if (entry.isAppliedMutuallyRecursively()) {
            dispatchedLambdaBodies.put(entry.getDispatchId(), body);

            return new Application(new Variable(MUTUAL_DISPATCHER_NAME),
                    new IntLiteral(functionDefinitionNode.getDispatchId()));
        }

        if (entry.isAppliedSelfRecursively()) {
            return new Application(Y_COMBINATOR, new Lambda(functionDefinitionNode.getName(), body));
        }

        return body;
    }

    @Override
    public Expression visit(IdentifierNode identifierNode) {
        var entry = symbolTable.lookup(identifierNode.getName());

        if (!(entry instanceof BindingEntry b)) return new Variable(identifierNode.getName());

        if (!recursionTargets.contains(identifierNode.getName())) return b.getNode().accept(this);

        if (b.isAppliedMutuallyRecursively()) {
            return new Application(
                    new Variable(MUTUAL_DISPATCHER_NAME),
                    new IntLiteral(b.getDispatchId())
            );
        }

        return new Variable(identifierNode.getName());
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

    public Expression visit(LetNode letNode) {
        if (symbolTable != letNode.getLocalTable()) {
            var localIrGeneratorVisitor = new IrGeneratorVisitor(letNode.getLocalTable(), recursionTargets,
                    dispatchedLambdaBodies);

            return letNode.accept(localIrGeneratorVisitor);
        }

        for (var b : letNode.getBindings()) {
            var entry = (BindingEntry) letNode.getLocalTable().lookup(b.getName());
            entry.setNode(b);
        }

        var bindings = letNode.getBindings();

        var body = letNode.getExpression().accept(this);

        // NOTE: Potential optimization: Only generate binding when necessary
        // Only generate when not applied mutually recursively
        // ALso check children
        for (var binding : bindings.reversed()) {
            body = new Application(new Lambda(binding.getName(), body), binding.accept(this));
        }

        return body;
    }

    @Override
    public Expression visit(ProgramNode programNode) {
        for (var b : programNode.getBindings()) {
            var entry = (BindingEntry) symbolTable.lookup(b.getName());
            entry.setNode(b);
        }

        var body = programNode.getBindings().stream()
                .filter(b -> b.getName().equals("main"))
                .findFirst()
                .get()
                .accept(this);

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
