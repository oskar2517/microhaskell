package me.oskar.microhaskell.ir;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.ast.visitor.BaseVisitor;

import java.util.HashMap;
import java.util.Map;

class MutuallyRecursiveLambdaCollectorVisitor extends BaseVisitor<Void> {

    private final Map<String, Integer> lambdaIds = new HashMap<>();

    public Map<String, Integer> getLambdaIds() {
        return lambdaIds;
    }

    @Override
    public Void visit(LetNode letNode) {
        for (var b : letNode.getBindings()) {
            b.accept(this);
        }

        letNode.getExpression().accept(this);

        return null;
    }

    @Override
    public Void visit(AnonymousFunctionNode anonymousFunctionNode) {
        anonymousFunctionNode.getBody().accept(this);

        return null;
    }

    @Override
    public Void visit(FunctionApplicationNode functionApplicationNode) {
        functionApplicationNode.getFunction().accept(this);
        functionApplicationNode.getArgument().accept(this);

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
    public Void visit(FunctionDefinitionNode functionDefinitionNode) {
        if (functionDefinitionNode.isAppliedMutuallyRecursively()) {
            lambdaIds.put(functionDefinitionNode.getName(), functionDefinitionNode.getDispatchId());
        }

        functionDefinitionNode.getBody().accept(this);

        return null;
    }

    @Override
    public Void visit(ProgramNode programNode) {
        for (var d : programNode.getBindings()) {
            d.accept(this);
        }

        return null;
    }
}
