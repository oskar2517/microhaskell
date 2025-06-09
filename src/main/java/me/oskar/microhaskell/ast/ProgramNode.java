package me.oskar.microhaskell.ast;

import me.oskar.microhaskell.ast.visitor.Visitor;
import me.oskar.microhaskell.position.Span;

import java.util.ArrayList;
import java.util.List;

public class ProgramNode extends Node{

    private final List<Node> bindings;

    public ProgramNode(Span span, List<Node> bindings) {
        super(span);

        this.bindings = bindings;
    }

    public List<Node> getBindings() {
        return bindings;
    }

    public ProgramNode merge(ProgramNode other) {
        var newBindings = new ArrayList<Node>();
        newBindings.addAll(bindings);
        newBindings.addAll(other.getBindings());

        return new ProgramNode(new Span(getSpan().start(), other.getSpan().end()), newBindings);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
