package me.oskar.microhaskell.ast;

import me.oskar.microhaskell.position.Span;

public abstract class ExpressionNode extends Node {

    protected ExpressionNode(Span span) {
        super(span);
    }
}
