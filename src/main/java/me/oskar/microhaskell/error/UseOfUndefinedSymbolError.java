package me.oskar.microhaskell.error;

import me.oskar.microhaskell.ast.IdentifierNode;

public class UseOfUndefinedSymbolError extends CompileTimeError {

    private final IdentifierNode identifierNode;

    protected UseOfUndefinedSymbolError(String code, String filename, IdentifierNode identifierNode) {
        super(code, filename);

        this.identifierNode = identifierNode;
    }

    @Override
    public void printError() {
        printErrorHead(identifierNode.getSpan(), "use of undefined symbol");
        printCode(identifierNode.getSpan(), "is undefined");
    }
}
