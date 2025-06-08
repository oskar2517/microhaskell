package me.oskar.microhaskell.table;

public class OperatorEntry implements Entry {

    public enum Associativity {
        LEFT, RIGHT, NONE
    }

    private final Associativity associativity;
    private final int precedence;

    public OperatorEntry(Associativity associativity, int precedence) {
        this.associativity = associativity;
        this.precedence = precedence;
    }

    public int getPrecedence() {
        return precedence;
    }

    public Associativity getAssociativity() {
        return associativity;
    }

    @Override
    public String toString() {
        return "(Operator associativity=%s, precedence=%s)".formatted(associativity, precedence);
    }
}
