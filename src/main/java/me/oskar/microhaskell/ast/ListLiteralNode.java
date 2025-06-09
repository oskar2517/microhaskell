package me.oskar.microhaskell.ast;

import me.oskar.microhaskell.ast.visitor.Visitor;
import me.oskar.microhaskell.position.Span;

import java.util.List;

public class ListLiteralNode extends ExpressionNode {

    private final List<ExpressionNode> value;

    public ListLiteralNode(Span span, List<ExpressionNode> value) {
        super(span);

        this.value = value;
    }

    public List<ExpressionNode> getValue() {
        return value;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
