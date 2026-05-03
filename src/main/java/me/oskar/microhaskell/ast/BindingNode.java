package me.oskar.microhaskell.ast;

import me.oskar.microhaskell.ast.visitor.Visitor;
import me.oskar.microhaskell.position.Span;

import java.util.List;

public class BindingNode extends Node implements ParameterizedNode {

    private final String name;
    private final List<AtomicExpressionNode> parameters;
    private final ExpressionNode body;

    public BindingNode(Span span, String name, List<AtomicExpressionNode> parameters, ExpressionNode body) {
        super(span);

        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    public String getName() {
        return name;
    }

    @Override
    public List<AtomicExpressionNode> getParameters() {
        return parameters;
    }

    @Override
    public ExpressionNode getBody() {
        return body;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
