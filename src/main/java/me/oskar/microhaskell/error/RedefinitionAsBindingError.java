package me.oskar.microhaskell.error;

import me.oskar.microhaskell.ast.BindingNode;

public class RedefinitionAsBindingError extends CompileTimeError {

    private final BindingNode bindingNode;

    protected RedefinitionAsBindingError(String code, String filename, BindingNode bindingNode) {
        super(code, filename);

        this.bindingNode = bindingNode;
    }

    @Override
    public void printError() {
        printErrorHead(bindingNode.getSpan(), "redefinition of symbol as binding");
        printCode(bindingNode.getSpan(), "is already defined on this scope");
    }
}
