package me.oskar.microhaskell.error;

import me.oskar.microhaskell.ast.FunctionDefinitionNode;
import me.oskar.microhaskell.ast.IdentifierNode;

public class RedefinitionAsFunctionError extends CompileTimeError {

    private final FunctionDefinitionNode functionDefinitionNode;

    protected RedefinitionAsFunctionError(String code, String filename, FunctionDefinitionNode functionDefinitionNode) {
        super(code, filename);

        this.functionDefinitionNode = functionDefinitionNode;
    }

    @Override
    public void printError() {
        printErrorHead(functionDefinitionNode.getSpan(), "redefinition of symbol as function");
        printCode(functionDefinitionNode.getSpan(), "is already defined on this scope");
    }
}
