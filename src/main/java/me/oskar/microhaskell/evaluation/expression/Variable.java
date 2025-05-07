package me.oskar.microhaskell.evaluation.expression;

import java.util.Map;

public record Variable(String name) implements Expression {

    @Override
    public Expression evaluate(Map<String, Expression> env) {
        var value = env.get(name);
        if (value == null) {
            throw new RuntimeException("Unbound variable: %s".formatted(name));
        }
        return value.evaluate(env);
    }

    @Override
    public String toString() {
        return name;
    }
}