package me.oskar.microhaskell.error;

import me.oskar.microhaskell.ast.AtomicExpressionNode;
import me.oskar.microhaskell.ast.FixityNode;
import me.oskar.microhaskell.ast.BindingNode;
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

    public InvalidBindingNameError invalidBindingName(Token token) {
        return new InvalidBindingNameError(code, filename, token);
    }

    public InvalidFixityPrecedenceError invalidFixityPrecedence(Token token) {
        return new InvalidFixityPrecedenceError(code, filename, token);
    }

    public FixityDeclarationLacksBindingError fixityDeclarationLacksBinding(FixityNode fixityNode) {
        return new FixityDeclarationLacksBindingError(code, filename, fixityNode);
    }

    public UseOfUndefinedSymbolError useOfUndefinedSymbol(IdentifierNode identifierNode) {
        return new UseOfUndefinedSymbolError(code, filename, identifierNode);
    }

    public RedefinitionAsParameterError redefinitionAsParameter(AtomicExpressionNode atomicExpressionNode) {
        return new RedefinitionAsParameterError(code, filename, atomicExpressionNode);
    }

    public RedefinitionAsBindingError redefinitionAsBinding(BindingNode identifierNode) {
        return new RedefinitionAsBindingError(code, filename, identifierNode);
    }

    public DuplicateFixityDeclarationError duplicateFixityDeclaration(FixityNode fixityNode) {
        return new DuplicateFixityDeclarationError(code, filename, fixityNode);
    }

    public MainBindingMissingError mainBindingMissing() {
        return new MainBindingMissingError(code, filename);
    }
}
