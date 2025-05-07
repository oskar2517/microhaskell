package me.oskar.microhaskell.evaluation.expression;

import java.util.Map;

public record Closure(Lambda lambda, Map<String, Expression> env) implements Expression {

    @Override
    public Expression evaluate(Map<String, Expression> env) {
        return this;
    }

    @Override
    public String toString() {
        return "<closure %s>".formatted(lambda);
    }
}
