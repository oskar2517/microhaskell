package me.oskar.microhaskell.ast;

import me.oskar.microhaskell.ast.visitor.Visitor;

public class IdentifierNode extends AtomicExpressionNode {

    private final String name;

    public IdentifierNode(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
