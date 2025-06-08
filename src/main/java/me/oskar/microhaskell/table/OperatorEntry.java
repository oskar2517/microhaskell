package me.oskar.microhaskell.table;

public class OperatorEntry implements Entry {

    public enum Associativity {
        LEFT, RIGHT, NONE
    }

    private final Associativity associativity;
    private final int precedence;
    private final String operatorName;

    public OperatorEntry(Associativity associativity, int precedence, String operatorName) {
        this.associativity = associativity;
        this.precedence = precedence;
        this.operatorName = operatorName;
    }

    public int getPrecedence() {
        return precedence;
    }

    public Associativity getAssociativity() {
        return associativity;
    }

    public String getOperatorName() {
        return operatorName;
    }

    @Override
    public String toString() {
        return "(Operator associativity=%s, precedence=%s)".formatted(associativity, precedence);
    }
}
