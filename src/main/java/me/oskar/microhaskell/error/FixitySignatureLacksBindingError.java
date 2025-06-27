package me.oskar.microhaskell.error;

import me.oskar.microhaskell.ast.FixityNode;

public class FixitySignatureLacksBindingError extends CompileTimeError {

    private final FixityNode fixityNode;

    protected FixitySignatureLacksBindingError(String code, String filename, FixityNode fixityNode) {
        super(code, filename);

        this.fixityNode = fixityNode;
    }

    @Override
    public void printError() {
        printErrorHead(fixityNode.getSpan(), "fixity signature lacks an accompanying binding");
        printCode(fixityNode.getSpan(), "`%s` is not bound`".formatted(fixityNode.getOperatorName()));
    }
}
