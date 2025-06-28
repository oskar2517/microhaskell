package me.oskar.microhaskell.error;

import me.oskar.microhaskell.lexer.Token;

public class InvalidOperatorPrecedenceError extends CompileTimeError {

    private final Token token;

    protected InvalidOperatorPrecedenceError(String code, String filename, Token token) {
        super(code, filename);

        this.token = token;
    }

    @Override
    public void printError() {
        printErrorHead(token.span(), "invalid operator precedence");
        printCode(token.span(), "has to be an integer between 0 and 9");
    }
}
