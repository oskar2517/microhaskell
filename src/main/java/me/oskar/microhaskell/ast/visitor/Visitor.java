package me.oskar.microhaskell.ast.visitor;

import me.oskar.microhaskell.ast.*;

public interface Visitor<T> {

    T visit(AnonymousFunctionNode anonymousFunctionNode);

    T visit(FunctionApplicationNode functionApplicationNode);

    T visit(FunctionDefinitionNode functionDefinitionNode);

    T visit(IdentifierNode identifierNode);

    T visit(IfNode ifNode);

    T visit(IntLiteralNode intLiteralNode);

    T visit(LetNode letNode);

    T visit(ProgramNode programNode);
}
