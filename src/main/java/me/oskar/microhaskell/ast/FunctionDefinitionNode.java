package me.oskar.microhaskell.ast;

import me.oskar.microhaskell.ast.visitor.Visitor;

import java.util.List;

public class FunctionDefinitionNode extends Node {

    private static int dispatchCounter = 0;

    private final String name;
    private final List<AtomicExpressionNode> parameters;
    private final ExpressionNode body;

    @Node.NoProperty
    private boolean appliedRecursively = false;
    @Node.NoProperty
    private boolean isAppliedMutuallyRecursively = false;
    @Node.NoProperty
    private final int dispatchId = dispatchCounter++;

    public FunctionDefinitionNode(String name, List<AtomicExpressionNode> parameters, ExpressionNode body) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public List<AtomicExpressionNode> getParameters() {
        return parameters;
    }

    public ExpressionNode getBody() {
        return body;
    }

    public boolean isAppliedRecursively() {
        return appliedRecursively;
    }

    public void setAppliedRecursively(boolean recursive) {
        appliedRecursively = recursive;
    }

    public boolean isAppliedMutuallyRecursively() {
        return isAppliedMutuallyRecursively;
    }

    public void setAppliedMutuallyRecursively(boolean recursive) {
        isAppliedMutuallyRecursively = recursive;
    }

    public int getDispatchId() {
        return dispatchId;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
