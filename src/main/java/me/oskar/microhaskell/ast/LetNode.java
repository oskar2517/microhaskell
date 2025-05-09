package me.oskar.microhaskell.ast;

import me.oskar.microhaskell.ast.visitor.Visitor;

import java.util.List;

public class LetNode extends ExpressionNode {

    private final List<FunctionDefinitionNode> bindings;
    private final ExpressionNode expression;

    public LetNode(List<FunctionDefinitionNode> bindings, ExpressionNode expression) {
        this.bindings = bindings;
        this.expression = expression;
    }

    public List<FunctionDefinitionNode> getBindings() {
        return bindings;
    }

    public ExpressionNode getExpression() {
        return expression;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
