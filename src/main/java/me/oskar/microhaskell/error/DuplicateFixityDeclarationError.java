package me.oskar.microhaskell.error;

import me.oskar.microhaskell.ast.FixityNode;

public class DuplicateFixityDeclarationError extends CompileTimeError {

    private final FixityNode fixityNode;

    protected DuplicateFixityDeclarationError(String code, String filename, FixityNode fixityNode) {
        super(code, filename);

        this.fixityNode = fixityNode;
    }

    @Override
    public void printError() {
        printErrorHead(fixityNode.getSpan(), "duplicate fixity declaration");
        printCode(fixityNode.getSpan(),
                "fixity for operator `%s` has already been declared".formatted(fixityNode.getOperatorName()));
    }
}
