package me.oskar.microhaskell.ast;

import me.oskar.microhaskell.ast.visitor.Visitor;
import me.oskar.microhaskell.position.Span;

public class IfNode extends ExpressionNode {

    private final ExpressionNode condition;
    private final ExpressionNode consequence;
    private final ExpressionNode alternative;

    public IfNode(Span span, ExpressionNode condition, ExpressionNode consequence, ExpressionNode alternative) {
        super(span);

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
