package me.oskar.microhaskell.evaluation.expression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Application(Expression function, Expression argument) implements Expression {

    @Override
    public Expression evaluate(Map<String, Expression> env) {
        var evaluatedFunction = function.evaluate(env);
        var lazyArgument = new Thunk(argument, env);

        if (evaluatedFunction instanceof Closure(Lambda lambda, Map<String, Expression> enclosedEnv)) {
            var extendedEnv = new HashMap<>(enclosedEnv);
            extendedEnv.put(lambda.parameter(), lazyArgument);
            return lambda.body().evaluate(extendedEnv);
        } else if (evaluatedFunction instanceof BuiltinFunction bf) {
            return bf.apply(List.of(lazyArgument), env);
        } else {
            throw new RuntimeException("Not a function: %s".formatted(evaluatedFunction));
        }
    }

    @Override
    public String toString() {
        return "(%s %s)".formatted(function, argument);
    }

}
