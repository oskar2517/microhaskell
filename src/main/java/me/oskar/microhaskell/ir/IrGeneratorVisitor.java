package me.oskar.microhaskell.ir;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.ast.visitor.BaseVisitor;
import me.oskar.microhaskell.error.Error;
import me.oskar.microhaskell.evaluation.expression.*;
import me.oskar.microhaskell.table.FunctionEntry;
import me.oskar.microhaskell.table.SymbolTable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IrGeneratorVisitor extends BaseVisitor<Expression> {

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
    private final Error error;

    public IrGeneratorVisitor(SymbolTable symbolTable, Error error) {
        this(symbolTable, new HashSet<>(), new HashMap<>(), error);
    }

    public IrGeneratorVisitor(SymbolTable symbolTable,
                              Set<String> recursionTargets,
                              Map<Integer, Expression> dispatchedLambdaBodies,
                              Error error) {
        this.symbolTable = symbolTable;
        this.recursionTargets = recursionTargets;
        this.dispatchedLambdaBodies = dispatchedLambdaBodies;
        this.error = error;
    }

    private Expression generateFunctionBody(FunctionNode function, IrGeneratorVisitor visitor) {
        var body = function.getBody().accept(visitor);

        for (var p : function.getParameters().reversed()) {
            body = new Lambda(((IdentifierNode) p).getName(), body);
        }

        return body;
    }

    @Override
    public Expression visit(AnonymousFunctionNode anonymousFunctionNode) {
        if (symbolTable != anonymousFunctionNode.getLocalTable()) {
            var localIrGeneratorVisitor = new IrGeneratorVisitor(anonymousFunctionNode.getLocalTable(), recursionTargets,
                    dispatchedLambdaBodies, error);

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
    public Expression visit(BindingNode bindingNode) {
        var entry = (FunctionEntry) symbolTable.lookupFunction(bindingNode.getName());

        var localRecursionTargets = recursionTargets;
        if (entry.isAppliedSelfRecursively() || entry.isAppliedMutuallyRecursively()) {
            localRecursionTargets = new HashSet<>(recursionTargets);
            localRecursionTargets.add(bindingNode.getName());
        }

        var localIrGeneratorVisitor = new IrGeneratorVisitor(entry.getLocalTable(), localRecursionTargets,
                dispatchedLambdaBodies, error);

        var body = generateFunctionBody(bindingNode, localIrGeneratorVisitor);

        if (entry.isAppliedMutuallyRecursively()) {
            dispatchedLambdaBodies.put(entry.getDispatchId(), body);

            return new Application(new Variable(MUTUAL_DISPATCHER_NAME),
                    new IntLiteral(entry.getDispatchId()));
        }

        if (entry.isAppliedSelfRecursively()) {
            return new Application(Y_COMBINATOR, new Lambda(bindingNode.getName(), body));
        }

        return body;
    }

    @Override
    public Expression visit(IdentifierNode identifierNode) {
        var entry = symbolTable.lookupFunction(identifierNode.getName());

        if (!(entry instanceof FunctionEntry fe)) return new Variable(identifierNode.getName());

        if (!recursionTargets.contains(identifierNode.getName())) return fe.getNode().accept(this);

        if (fe.isAppliedMutuallyRecursively()) {
            return new Application(
                    new Variable(MUTUAL_DISPATCHER_NAME),
                    new IntLiteral(fe.getDispatchId())
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
                    dispatchedLambdaBodies, error);

            return letNode.accept(localIrGeneratorVisitor);
        }

        var declarations = letNode.getDeclarations();

        for (var d : declarations) {
            if (!(d instanceof BindingNode b)) continue;

            var entry = (FunctionEntry) letNode.getLocalTable().lookupFunction(b.getName());
            entry.setNode(b);
        }

        var body = letNode.getExpression().accept(this);

        // NOTE: Potential optimization: Only generate binding when necessary
        // Only generate when not applied mutually recursively
        // ALso check children
        for (var d : declarations.reversed()) {
            if (!(d instanceof BindingNode b)) continue;

            body = new Application(new Lambda(b.getName(), body), d.accept(this));
        }

        return body;
    }

    @Override
    public Expression visit(ListLiteralNode listLiteralNode) {
        var nilEntry = (FunctionEntry) symbolTable.lookupFunction("nil");
        var consEntry = (FunctionEntry) symbolTable.lookupFunction("cons");

        var previous = nilEntry.getNode().accept(this);
        for (var v : listLiteralNode.getValue().reversed()) {
            var appliedValue = new Application(consEntry.getNode().accept(this), v.accept(this));
            previous = new Application(appliedValue, previous);
        }

        return previous;
    }

    @Override
    public Expression visit(ProgramNode programNode) {
        for (var d : programNode.getDeclarations()) {
            if (!(d instanceof BindingNode b)) continue;

            var entry = (FunctionEntry) symbolTable.lookupFunction(b.getName());
            entry.setNode(b);
        }

        var main = programNode.getDeclarations().stream()
                .filter(d -> d instanceof BindingNode)
                .filter(b -> ((BindingNode) b).getName().equals("main"))
                .findFirst();

        if (main.isEmpty()) {
            throw error.mainFunctionMissing();
        }

        var body = main.get().accept(this);

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
