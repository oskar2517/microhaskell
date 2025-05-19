package me.oskar.microhaskell.ast;

import me.oskar.microhaskell.ast.visitor.Visitor;
import me.oskar.microhaskell.table.SymbolTable;

import java.util.List;

public class AnonymousFunctionNode extends ExpressionNode implements FunctionNode {

    private final List<AtomicExpressionNode> parameters;
    private final ExpressionNode body;
    @Node.NoProperty
    private SymbolTable localTable;

    public AnonymousFunctionNode(List<AtomicExpressionNode> parameters, ExpressionNode body) {
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
