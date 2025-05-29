package me.oskar.microhaskell.ast;

import me.oskar.microhaskell.position.Span;

public abstract class AtomicExpressionNode extends ExpressionNode {

    protected AtomicExpressionNode(Span span) {
        super(span);
    }
}
