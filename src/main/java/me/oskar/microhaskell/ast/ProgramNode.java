package me.oskar.microhaskell.ast;

import me.oskar.microhaskell.ast.visitor.Visitor;
import me.oskar.microhaskell.position.Span;

import java.util.ArrayList;
import java.util.List;

public class ProgramNode extends Node{

    private final List<Node> declarations;

    public ProgramNode(Span span, List<Node> declarations) {
        super(span);

        this.declarations = declarations;
    }

    public List<Node> getDeclarations() {
        return declarations;
    }

    public ProgramNode merge(ProgramNode other) {
        var newBindings = new ArrayList<Node>();
        newBindings.addAll(declarations);
        newBindings.addAll(other.getDeclarations());

        return new ProgramNode(new Span(getSpan().start(), other.getSpan().end()), newBindings);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
