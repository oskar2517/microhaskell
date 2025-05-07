package me.oskar.microhaskell.ir;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.ast.visitor.Visitor;
import me.oskar.microhaskell.evaluation.expression.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class IRGeneratorVisitor implements Visitor<Expression> {

    private final Map<String, FunctionDefinitionNode> functions = new HashMap<>();
    private final HashSet<FunctionDefinitionNode> generatedFunctions = new HashSet<>();

    @Override
    public Expression visit(FunctionApplicationNode functionApplicationNode) {
        if (functionApplicationNode.getFunction() instanceof IdentifierNode i && functions.containsKey(i.getName())) {
            var function = functions.get(i.getName());

            if (!generatedFunctions.contains(function)) {
                var letrec = new LetRec(function.getName(), functionApplicationNode.getFunction().accept(this),
                        new Application(functionApplicationNode.getFunction().accept(this),
                                functionApplicationNode.getArgument().accept(this)));
                generatedFunctions.remove(function);
                return letrec;
            }
        }

        return new Application(functionApplicationNode.getFunction().accept(this),
                functionApplicationNode.getArgument().accept(this));
    }


    @Override
    public Expression visit(FunctionDefinitionNode functionDefinitionNode) {
        var parameters = functionDefinitionNode.getParameters();

        if (parameters.isEmpty()) {
            return new Lambda("_", functionDefinitionNode.getBody().accept(this));
        }

        var lambda = new Lambda(((IdentifierNode) parameters.getLast()).getName(),
                functionDefinitionNode.getBody().accept(this));
        for (var i = parameters.size() - 2; i >= 0; i--) {
            var p = (IdentifierNode) parameters.get(i);
            lambda = new Lambda(p.getName(), lambda);
        }

        return lambda;
    }

    @Override
    public Expression visit(IdentifierNode identifierNode) {
        if (!functions.containsKey(identifierNode.getName())) {
            return new Variable(identifierNode.getName());
        }

        var function = functions.get(identifierNode.getName());

        if (generatedFunctions.contains(function)) {
            return new Variable(identifierNode.getName());
        }

        if (function.getParameters().isEmpty()) {
            // TODO: This is incorrect for main = main
            return new LetRec(identifierNode.getName(), function.accept(this),
                    function.getBody().accept(this));
        } else {
            generatedFunctions.add(function);
            return function.accept(this);
        }
    }

    @Override
    public Expression visit(IfNode ifNode) {
        return new If(ifNode.getCondition().accept(this),
                ifNode.getConsequence().accept(this),
                ifNode.getAlternative().accept(this));
    }

    @Override
    public Expression visit(IntLiteralNode intLiteralNode) {
        return new IntLiteral(intLiteralNode.getValue());
    }

    @Override
    public Expression visit(ProgramNode programNode) {
        for (var d : programNode.getDefinitions()) {
            functions.put(d.getName(), d);
        }

        var main = functions.get("main");

        if (main == null) {
            throw new RuntimeException("Main function definition not found");
        }

        if (!main.getParameters().isEmpty()) {
            throw new RuntimeException("Main function does not have parameters");
        }

        return main.getBody().accept(this);
    }
}
