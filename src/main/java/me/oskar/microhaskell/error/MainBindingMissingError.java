package me.oskar.microhaskell.error;

import me.oskar.microhaskell.position.Span;

public class MainBindingMissingError extends CompileTimeError {

    protected MainBindingMissingError(String code, String filename) {
        super(code, filename);
    }

    @Override
    public void printError() {
        printErrorHead(Span.BASE_SPAN, "main binding missing");
    }
}
