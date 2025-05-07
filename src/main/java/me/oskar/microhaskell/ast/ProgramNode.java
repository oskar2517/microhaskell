package me.oskar.microhaskell.ast;

import me.oskar.microhaskell.ast.visitor.Visitor;

import java.util.List;

public class ProgramNode extends Node{

    private final List<FunctionDefinitionNode> definitions;

    public ProgramNode(List<FunctionDefinitionNode> definitions) {
        this.definitions = definitions;
    }

    public List<FunctionDefinitionNode> getDefinitions() {
        return definitions;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
