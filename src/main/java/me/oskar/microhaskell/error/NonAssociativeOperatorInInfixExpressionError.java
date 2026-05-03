package me.oskar.microhaskell.error;

import me.oskar.microhaskell.ast.ExpressionNode;

public class NonAssociativeOperatorInInfixExpressionError extends CompileTimeError {

    private final ExpressionNode expressionNode;
    private final String operatorName;

    protected NonAssociativeOperatorInInfixExpressionError(String code, String filename, ExpressionNode expressionNode,
                                                        String operatorName) {
        super(code, filename);

        this.expressionNode = expressionNode;
        this.operatorName = operatorName;
    }

    @Override
    public void printError() {
        printErrorHead(expressionNode.getSpan(), "non-associative operator used in an infix expression");
        printCode(expressionNode.getSpan(), "`%s` is non-associative".formatted(operatorName));
    }
}
