package me.oskar.microhaskell.ast;

import me.oskar.microhaskell.ast.visitor.Visitor;

import java.util.List;

public class ProgramNode extends Node{

    private final List<FunctionDefinitionNode> bindings;

    public ProgramNode(List<FunctionDefinitionNode> bindings) {
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
