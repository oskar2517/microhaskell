package me.oskar.microhaskell.ir;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.ast.visitor.Visitor;
import me.oskar.microhaskell.evaluation.expression.*;

import java.util.*;

public class IRGeneratorVisitor implements Visitor<Expression> {

    private final Map<String, FunctionDefinitionNode> functions = new LinkedHashMap<>();
    private String currentFunction = null;
    private String recursiveAlias = null;

    private final Expression yCombinator = new Lambda("f",
            new Application(
                    new Lambda("x",
                            new Application(
                                    new Variable("f"),
                                    new Application(new Variable("x"), new Variable("x"))
                            )
                    ),
                    new Lambda("x",
                            new Application(
                                    new Variable("f"),
                                    new Application(new Variable("x"), new Variable("x"))
                            )
                    )
            )
    );

    @Override
    public Expression visit(FunctionApplicationNode functionApplicationNode) {
        return new Application(
                functionApplicationNode.getFunction().accept(this),
                functionApplicationNode.getArgument().accept(this)
        );
    }

    @Override
    public Expression visit(FunctionDefinitionNode functionDefinitionNode) {
        var previousFunction = currentFunction;
        var previousAlias = recursiveAlias;

        currentFunction = functionDefinitionNode.getName();
        recursiveAlias = functionDefinitionNode.isAppliedRecursively() ? "__rec" : currentFunction;

        Expression body = functionDefinitionNode.getBody().accept(this);

        // Wrap parameters
        for (int i = functionDefinitionNode.getParameters().size() - 1; i >= 0; i--) {
            var param = (IdentifierNode) functionDefinitionNode.getParameters().get(i);
            body = new Lambda(param.getName(), body);
        }

        // Wrap in Y combinator if recursive
        if (functionDefinitionNode.isAppliedRecursively()) {
            body = new Lambda(recursiveAlias, body);
            body = new Application(yCombinator, body);
        }

        currentFunction = previousFunction;
        recursiveAlias = previousAlias;
        return body;
    }

    @Override
    public Expression visit(IdentifierNode identifierNode) {
        var name = identifierNode.getName();

        if (name.equals(currentFunction) &&
                recursiveAlias != null &&
                !name.equals(recursiveAlias)) {
            return new Variable(recursiveAlias);
        }

        return new Variable(name);
    }

    @Override
    public Expression visit(IfNode ifNode) {
        return new Application(
                new Application(
                        new Application(
                                new Variable("if"),
                                ifNode.getCondition().accept(this)
                        ),
                        ifNode.getConsequence().accept(this)
                ),
                ifNode.getAlternative().accept(this)
        );
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

        Map<String, Expression> functionIRs = new LinkedHashMap<>();
        for (var d : programNode.getDefinitions()) {
            functionIRs.put(d.getName(), d.accept(this));
        }

        Expression body = new Variable("main");

        List<String> names = new ArrayList<>(functionIRs.keySet());
        Collections.reverse(names);

        for (String name : names) {
            Expression expr = functionIRs.get(name);
            body = new Application(new Lambda(name, body), expr);
        }

        return body;
    }


}
