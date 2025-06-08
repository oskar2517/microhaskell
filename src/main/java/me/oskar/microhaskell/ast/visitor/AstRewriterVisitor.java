package me.oskar.microhaskell.ast.visitor;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.table.FunctionEntry;
import me.oskar.microhaskell.table.SymbolTable;

import java.util.ArrayList;

public abstract class AstRewriterVisitor implements Visitor<Node> {

    protected final SymbolTable symbolTable;

    public AstRewriterVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    abstract protected AstRewriterVisitor createInstance(SymbolTable localTable);

    @Override
    public Node visit(AnonymousFunctionNode anonymousFunctionNode) {
        if (symbolTable != anonymousFunctionNode.getLocalTable()) {
            var localRewriter = createInstance(anonymousFunctionNode.getLocalTable());
            return anonymousFunctionNode.accept(localRewriter);
        }

        var parameters = new ArrayList<AtomicExpressionNode>();

        for (var p : anonymousFunctionNode.getParameters()) {
            parameters.add((AtomicExpressionNode) p.accept(this));
        }

        var body = (ExpressionNode) anonymousFunctionNode.getBody().accept(this);

        return new AnonymousFunctionNode(anonymousFunctionNode.getSpan(), parameters, body);
    }

    @Override
    public Node visit(FixityNode fixityNode) {
        return fixityNode;
    }

    @Override
    public Node visit(FlatExpressionNode flatExpressionNode) {
        var elements = new ArrayList<FlatExpressionElement>();

        for (var e : flatExpressionNode.getElements()) {
            if (e instanceof Node n) {
                elements.add((FlatExpressionElement) n.accept(this));
            } else {
                elements.add(e);
            }
        }

        return new FlatExpressionNode(flatExpressionNode.getSpan(), elements);
    }

    @Override
    public Node visit(FunctionApplicationNode functionApplicationNode) {
        var function = (ExpressionNode) functionApplicationNode.getFunction().accept(this);
        var argument = (ExpressionNode) functionApplicationNode.getArgument().accept(this);

        return new FunctionApplicationNode(functionApplicationNode.getSpan(), function, argument);
    }

    @Override
    public Node visit(FunctionDefinitionNode functionDefinitionNode) {
        var entry = (FunctionEntry) symbolTable.lookup(functionDefinitionNode.getName());

        if (symbolTable != entry.getLocalTable()) {
            var localRewriter = createInstance(entry.getLocalTable());
            return functionDefinitionNode.accept(localRewriter);
        }

        var parameters = new ArrayList<AtomicExpressionNode>();

        for (var p : functionDefinitionNode.getParameters()) {
            parameters.add((AtomicExpressionNode) p.accept(this));
        }

        var body = (ExpressionNode) functionDefinitionNode.getBody().accept(this);

        return new FunctionDefinitionNode(functionDefinitionNode.getSpan(), functionDefinitionNode.getName(),
                parameters, body);
    }

    @Override
    public Node visit(IdentifierNode identifierNode) {
        return identifierNode;
    }

    @Override
    public Node visit(IfNode ifNode) {
        var condition = (ExpressionNode) ifNode.getCondition().accept(this);
        var consequence = (ExpressionNode) ifNode.getConsequence().accept(this);
        var alternative = (ExpressionNode) ifNode.getAlternative().accept(this);

        return new IfNode(ifNode.getSpan(), condition, consequence, alternative);
    }

    @Override
    public Node visit(IntLiteralNode intLiteralNode) {
        return intLiteralNode;
    }

    @Override
    public Node visit(LetNode letNode) {
        if (symbolTable != letNode.getLocalTable()) {
            var localRewriter = createInstance(letNode.getLocalTable());
            return letNode.accept(localRewriter);
        }

        var bindings = new ArrayList<Node>();

        for (var b : letNode.getBindings()) {
            bindings.add(b.accept(this));
        }

        var expression = (ExpressionNode) letNode.getExpression().accept(this);

        var node = new LetNode(letNode.getSpan(), bindings, expression);
        node.setLocalTable(letNode.getLocalTable());

        return node;
    }

    @Override
    public Node visit(ProgramNode programNode) {
        var bindings = new ArrayList<Node>();

        for (var b : programNode.getBindings()) {
            bindings.add(b.accept(this));
        }

        return new ProgramNode(programNode.getSpan(), bindings);
    }
}
