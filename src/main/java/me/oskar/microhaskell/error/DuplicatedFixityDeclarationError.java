package me.oskar.microhaskell.error;

import me.oskar.microhaskell.ast.FixityNode;

public class DuplicatedFixityDeclarationError extends CompileTimeError {

    private final FixityNode fixityNode;

    protected DuplicatedFixityDeclarationError(String code, String filename, FixityNode fixityNode) {
        super(code, filename);

        this.fixityNode = fixityNode;
    }

    @Override
    public void printError() {
        printErrorHead(fixityNode.getSpan(), "duplicated fixity declaration");
        printCode(fixityNode.getSpan(),
                "fixity for operator `%s` has already been declared".formatted(fixityNode.getOperatorName()));
    }
}
