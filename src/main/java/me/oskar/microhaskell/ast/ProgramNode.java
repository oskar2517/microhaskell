package me.oskar.microhaskell.ast;

import me.oskar.microhaskell.ast.visitor.Visitor;
import me.oskar.microhaskell.position.Span;

import java.util.List;

public class ProgramNode extends Node{

    private final List<FunctionDefinitionNode> bindings;

    public ProgramNode(Span span, List<FunctionDefinitionNode> bindings) {
        super(span);

        this.bindings = bindings;
    }

    public List<FunctionDefinitionNode> getBindings() {
        return bindings;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
