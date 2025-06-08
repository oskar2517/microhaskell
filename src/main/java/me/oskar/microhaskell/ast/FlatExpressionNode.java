package me.oskar.microhaskell.ast;

import me.oskar.microhaskell.ast.visitor.Visitor;
import me.oskar.microhaskell.position.Span;

import java.util.List;

public class FlatExpressionNode extends ExpressionNode {

    public record Operator(String name) implements FlatExpressionElement {

    }

    private final List<FlatExpressionElement> elements;

    public FlatExpressionNode(Span span, List<FlatExpressionElement> elements) {
        super(span);

        this.elements = elements;
    }

    public List<FlatExpressionElement> getElements() {
        return elements;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
