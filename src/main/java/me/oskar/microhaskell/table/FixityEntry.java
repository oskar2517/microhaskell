package me.oskar.microhaskell.table;

public record FixityEntry(Associativity associativity, int precedence) implements Entry {

    public enum Associativity {
        LEFT, RIGHT, NONE
    }

    @Override
    public String toString() {
        return "(Fixity associativity=%s, precedence=%s)".formatted(associativity, precedence);
    }
}
