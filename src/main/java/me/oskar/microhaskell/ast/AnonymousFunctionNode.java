package me.oskar.microhaskell.ast;

import me.oskar.microhaskell.ast.visitor.Visitor;

import java.util.List;

public class AnonymousFunctionNode extends ExpressionNode {

    private final List<AtomicExpressionNode> parameters;
    private final ExpressionNode body;

    public AnonymousFunctionNode(List<AtomicExpressionNode> parameters, ExpressionNode body) {
        this.parameters = parameters;
        this.body = body;
    }

    public List<AtomicExpressionNode> getParameters() {
        return parameters;
    }

    public ExpressionNode getBody() {
        return body;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
