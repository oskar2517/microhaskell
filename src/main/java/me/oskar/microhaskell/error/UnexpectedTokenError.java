package me.oskar.microhaskell.error;

import me.oskar.microhaskell.lexer.Token;
import me.oskar.microhaskell.lexer.TokenType;

public class UnexpectedTokenError extends CompileTimeError {

    private final Token token;
    private final String expected;

    public UnexpectedTokenError(String code, String filename, Token token, String expected) {
        super(code, filename);

        this.token = token;
        this.expected = expected;
    }

    @Override
    public void printError() {
        if (token.type() == TokenType.EOF) {
            printErrorHead(token.span(), "unexpected end of file");
        } else {
            printErrorHead(token.span(), "unexpected token");
            printCode(token.span(), String.format("found `%s`, expected %s", token.type().tokenName, expected));
        }
    }
}
