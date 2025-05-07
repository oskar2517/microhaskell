package me.oskar.microhaskell.evaluation.expression;

import java.util.Map;

public record IntLiteral(int value) implements Expression {

    @Override
    public Expression evaluate(Map<String, Expression> env) {
        return this;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
