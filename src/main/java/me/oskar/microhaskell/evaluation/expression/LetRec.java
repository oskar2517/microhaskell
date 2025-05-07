package me.oskar.microhaskell.evaluation.expression;

import java.util.HashMap;
import java.util.Map;

public record LetRec(String name, Expression expr, Expression body) implements Expression {

    @Override
    public Expression evaluate(Map<String, Expression> env) {
        var extendedEnv = new HashMap<>(env);
        extendedEnv.put(name, null);

        var value = expr.evaluate(extendedEnv);
        if (value instanceof Closure closure) {
            extendedEnv.put(name, new Closure(closure.lambda(), extendedEnv));
        } else {
            extendedEnv.put(name, value);
        }

        return body.evaluate(extendedEnv);
    }

    @Override
    public String toString() {
        return "letrec %s = %s in %s".formatted(name, expr, body);
    }
}
