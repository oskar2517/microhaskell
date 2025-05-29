package me.oskar.microhaskell.ast;

import me.oskar.microhaskell.ast.visitor.Visitor;
import me.oskar.microhaskell.position.Span;

import java.util.List;

public class FunctionDefinitionNode extends Node implements FunctionNode {

    private static int dispatchCounter = 0;

    private final String name;
    private final List<AtomicExpressionNode> parameters;
    private final ExpressionNode body;

    @Node.NoProperty
    private final int dispatchId = dispatchCounter++;

    public FunctionDefinitionNode(Span span, String name, List<AtomicExpressionNode> parameters, ExpressionNode body) {
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

    public int getDispatchId() {
        return dispatchId;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
