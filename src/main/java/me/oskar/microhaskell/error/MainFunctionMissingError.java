package me.oskar.microhaskell.error;

import me.oskar.microhaskell.position.Span;

public class MainFunctionMissingError extends CompileTimeError {

    protected MainFunctionMissingError(String code, String filename) {
        super(code, filename);
    }

    @Override
    public void printError() {
        printErrorHead(Span.BASE_SPAN, "main function missing");
    }
}
