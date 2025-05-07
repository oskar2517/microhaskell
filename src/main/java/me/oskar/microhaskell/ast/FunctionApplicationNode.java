package me.oskar.microhaskell.ast;

import me.oskar.microhaskell.ast.visitor.Visitor;

public class FunctionApplicationNode extends ExpressionNode {

    private final ExpressionNode function;
    private final ExpressionNode argument;

    public FunctionApplicationNode(ExpressionNode function, ExpressionNode argument) {
        this.function = function;
        this.argument = argument;
    }

    public ExpressionNode getFunction() {
        return function;
    }

    public ExpressionNode getArgument() {
        return argument;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
