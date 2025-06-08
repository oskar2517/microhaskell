package me.oskar.microhaskell.ast;

import me.oskar.microhaskell.ast.visitor.Visitor;
import me.oskar.microhaskell.position.Span;
import me.oskar.microhaskell.table.OperatorEntry;

public class FixityNode extends Node {

    private final OperatorEntry.Associativity associativity;
    private final int precedence;
    private final String operatorName;

    public FixityNode(Span span, OperatorEntry.Associativity associativity, int precedence,String operatorName) {
        super(span);

        this.associativity = associativity;
        this.precedence = precedence;
        this.operatorName = operatorName;
    }

    public OperatorEntry.Associativity getAssociativity() {
        return associativity;
    }

    public int getPrecedence() {
        return precedence;
    }

    public String getOperatorName() {
        return operatorName;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return null;
    }
}
