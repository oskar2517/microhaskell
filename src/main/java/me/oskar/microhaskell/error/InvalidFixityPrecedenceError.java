package me.oskar.microhaskell.error;

import me.oskar.microhaskell.lexer.Token;

public class InvalidFixityPrecedenceError extends CompileTimeError {

    private final Token token;

    protected InvalidFixityPrecedenceError(String code, String filename, Token token) {
        super(code, filename);

        this.token = token;
    }

    @Override
    public void printError() {
        printErrorHead(token.span(), "invalid fixity precedence");
        printCode(token.span(), "must be an integer between 0 and 9");
    }
}
