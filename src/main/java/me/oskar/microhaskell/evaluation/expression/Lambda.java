package me.oskar.microhaskell.evaluation.expression;

import java.util.HashMap;
import java.util.Map;

public record Lambda(String parameter, Expression body) implements Expression {

    @Override
    public Expression evaluate(Map<String, Expression> env) {
        return new Closure(this, new HashMap<>(env));
    }

    @Override
    public String toString() {
        return "(\\%s. %s)".formatted(parameter, body);
    }

}
