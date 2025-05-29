package me.oskar.microhaskell.ast;

import me.oskar.microhaskell.ast.visitor.Visitor;
import me.oskar.microhaskell.position.Span;

public class IntLiteralNode extends AtomicExpressionNode {

    private final int value;

    public IntLiteralNode(Span span, int value) {
        super(span);

        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
