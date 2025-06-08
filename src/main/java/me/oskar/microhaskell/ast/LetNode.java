package me.oskar.microhaskell.ast;

import me.oskar.microhaskell.ast.visitor.Visitor;
import me.oskar.microhaskell.position.Span;
import me.oskar.microhaskell.table.SymbolTable;

import java.util.List;

public class LetNode extends ExpressionNode {

    private final List<Node> bindings;
    private final ExpressionNode expression;
    @Node.NoProperty
    private SymbolTable localTable;

    public LetNode(Span span, List<Node> bindings, ExpressionNode expression) {
        super(span);

        this.bindings = bindings;
        this.expression = expression;
    }

    public List<Node> getBindings() {
        return bindings;
    }

    public ExpressionNode getExpression() {
        return expression;
    }

    public SymbolTable getLocalTable() {
        return localTable;
    }

    public void setLocalTable(SymbolTable localTable) {
        this.localTable = localTable;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
