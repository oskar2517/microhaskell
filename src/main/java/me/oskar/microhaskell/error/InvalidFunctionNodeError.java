package me.oskar.microhaskell.error;

import me.oskar.microhaskell.lexer.Token;

public class InvalidFunctionNodeError extends CompileTimeError {

    private final Token token;

    protected InvalidFunctionNodeError(String code, String filename, Token token) {
        super(code, filename);

        this.token = token;
    }

    @Override
    public void printError() {
        printErrorHead(token.span(), "invalid function name");
        printCode(token.span(), "expected identifier or operator in parenthesis");
    }
}
