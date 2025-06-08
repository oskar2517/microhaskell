package me.oskar.microhaskell.ast;

import me.oskar.microhaskell.position.Span;

public abstract class ExpressionNode extends Node implements FlatExpressionElement {

    protected ExpressionNode(Span span) {
        super(span);
    }
}
