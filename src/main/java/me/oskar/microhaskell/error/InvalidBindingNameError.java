package me.oskar.microhaskell.error;

import me.oskar.microhaskell.lexer.Token;

public class InvalidBindingNameError extends CompileTimeError {

    private final Token token;

    protected InvalidBindingNameError(String code, String filename, Token token) {
        super(code, filename);

        this.token = token;
    }

    @Override
    public void printError() {
        printErrorHead(token.span(), "invalid binding name");
        printCode(token.span(), "expected identifier or operator in parentheses");
    }
}
