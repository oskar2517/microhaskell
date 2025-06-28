package me.oskar.microhaskell.error;

import me.oskar.microhaskell.ast.AtomicExpressionNode;
import me.oskar.microhaskell.ast.FixityNode;
import me.oskar.microhaskell.ast.FunctionDefinitionNode;
import me.oskar.microhaskell.ast.IdentifierNode;
import me.oskar.microhaskell.lexer.Token;

public class Error {

    private final String code;
    private final String filename;

    public Error(String code, String filename) {
        this.code = code;
        this.filename = filename;
    }

    public UnexpectedTokenError unexpectedToken(Token token, String expected) {
        return new UnexpectedTokenError(code, filename, token, expected);
    }

    public InvalidFunctionNodeError invalidFunctionName(Token token) {
        return new InvalidFunctionNodeError(code, filename, token);
    }

    public InvalidOperatorPrecedenceError invalidOperatorPrecedence(Token token) {
        return new InvalidOperatorPrecedenceError(code, filename, token);
    }

    public FixitySignatureLacksBindingError fixitySignatureLacksBinding(FixityNode fixityNode) {
        return new FixitySignatureLacksBindingError(code, filename, fixityNode);
    }

    public UseOfUndefinedSymbolError useOfUndefinedSymbol(IdentifierNode identifierNode) {
        return new UseOfUndefinedSymbolError(code, filename, identifierNode);
    }

    public RedefinitionAsParameterError redefinitionAsParameter(AtomicExpressionNode atomicExpressionNode) {
        return new RedefinitionAsParameterError(code, filename, atomicExpressionNode);
    }

    public RedefinitionAsFunctionError redefinitionAsFunction(FunctionDefinitionNode identifierNode) {
        return new RedefinitionAsFunctionError(code, filename, identifierNode);
    }

    public DuplicatedFixityDeclarationError duplicatedFixityDeclaration(FixityNode fixityNode) {
        return new DuplicatedFixityDeclarationError(code, filename, fixityNode);
    }

    public MainFunctionMissingError mainFunctionMissing() {
        return new MainFunctionMissingError(code, filename);
    }
}
