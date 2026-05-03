package me.oskar.microhaskell.ast;

import me.oskar.microhaskell.ast.visitor.Visitor;
import me.oskar.microhaskell.position.Span;
import me.oskar.microhaskell.table.SymbolTable;

import java.util.List;

public class LambdaNode extends ExpressionNode implements ParameterizedNode {

    private final List<AtomicExpressionNode> parameters;
    private final ExpressionNode body;
    @Node.NoProperty
    private SymbolTable localTable;

    public LambdaNode(Span span, List<AtomicExpressionNode> parameters, ExpressionNode body) {
        super(span);

        this.parameters = parameters;
        this.body = body;
    }

    @Override
    public List<AtomicExpressionNode> getParameters() {
        return parameters;
    }

    @Override
    public ExpressionNode getBody() {
        return body;
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
