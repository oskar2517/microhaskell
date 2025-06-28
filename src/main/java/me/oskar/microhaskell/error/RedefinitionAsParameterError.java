package me.oskar.microhaskell.error;

import me.oskar.microhaskell.ast.AtomicExpressionNode;

public class RedefinitionAsParameterError extends CompileTimeError {

    private final AtomicExpressionNode atomicExpressionNode;

    protected RedefinitionAsParameterError(String code, String filename, AtomicExpressionNode atomicExpressionNode) {
        super(code, filename);

        this.atomicExpressionNode = atomicExpressionNode;
    }

    @Override
    public void printError() {
        printErrorHead(atomicExpressionNode.getSpan(), "redefinition of symbol as parameter");
        printCode(atomicExpressionNode.getSpan(), "is already defined on this scope");
    }
}
