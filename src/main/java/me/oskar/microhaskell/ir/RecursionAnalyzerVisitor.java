package me.oskar.microhaskell.ir;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.ast.visitor.BaseVisitor;

public class RecursionAnalyzerVisitor extends BaseVisitor<Void> {

    private String currentFunctionName = null;
    private boolean foundRecursiveCall = false;

    @Override
    public Void visit(FunctionDefinitionNode functionDefinitionNode) {
        currentFunctionName = functionDefinitionNode.getName();
        foundRecursiveCall = false;

        functionDefinitionNode.getBody().accept(this);

        functionDefinitionNode.setAppliedRecursively(foundRecursiveCall);

        return null;
    }

    @Override
    public Void visit(FunctionApplicationNode functionApplicationNode) {
        functionApplicationNode.getFunction().accept(this);
        functionApplicationNode.getArgument().accept(this);

        return null;
    }

    @Override
    public Void visit(IdentifierNode identifierNode) {
        if (identifierNode.getName().equals(currentFunctionName)) {
            foundRecursiveCall = true;
        }

        return null;
    }

    @Override
    public Void visit(IfNode ifNode) {
        ifNode.getCondition().accept(this);
        ifNode.getConsequence().accept(this);
        ifNode.getAlternative().accept(this);

        return null;
    }

    @Override
    public Void visit(ProgramNode programNode) {
        for (var d : programNode.getDefinitions()) {
            d.accept(this);
        }

        return null;
    }
}
