package me.oskar.microhaskell.ast.visitor;

import me.oskar.microhaskell.ast.*;

public class BaseVisitor<T> implements Visitor<T> {

    @Override
    public T visit(AnonymousFunctionNode anonymousFunctionNode) {
        return null;
    }

    @Override
    public T visit(FixityNode fixityNode) {
        return null;
    }

    @Override
    public T visit(FlatExpressionNode flatExpressionNode) {
        return null;
    }

    @Override
    public T visit(FunctionApplicationNode functionApplicationNode) {
        return null;
    }

    @Override
    public T visit(FunctionDefinitionNode functionDefinitionNode) {
        return null;
    }

    @Override
    public T visit(IdentifierNode identifierNode) {
        return null;
    }

    @Override
    public T visit(IfNode ifNode) {
        return null;
    }

    @Override
    public T visit(IntLiteralNode intLiteralNode) {
        return null;
    }

    @Override
    public T visit(LetNode letNode) {
        return null;
    }

    @Override
    public T visit(ProgramNode programNode) {
        return null;
    }
}
