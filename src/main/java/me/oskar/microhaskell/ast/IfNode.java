package me.oskar.microhaskell.ast;

import me.oskar.microhaskell.ast.visitor.Visitor;

public class IfNode extends ExpressionNode {

    private final ExpressionNode condition;
    private final ExpressionNode consequence;
    private final ExpressionNode alternative;

    public IfNode(ExpressionNode condition, ExpressionNode consequence, ExpressionNode alternative) {
        this.condition = condition;
        this.consequence = consequence;
        this.alternative = alternative;
    }

    public ExpressionNode getCondition() {
        return condition;
    }

    public ExpressionNode getConsequence() {
        return consequence;
    }

    public ExpressionNode getAlternative() {
        return alternative;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
