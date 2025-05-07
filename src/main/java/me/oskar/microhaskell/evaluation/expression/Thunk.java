package me.oskar.microhaskell.evaluation.expression;

import java.util.Map;

public class Thunk implements Expression {

    private Expression expression;
    private final Map<String, Expression> env;
    private Expression value;
    private boolean evaluated = false;

    public Thunk(Expression expression, Map<String, Expression> env) {
        this.expression = expression;
        this.env = env;
    }

    @Override
    public Expression evaluate(Map<String, Expression> outerEnv) {
        if (!evaluated) {
            value = expression.evaluate(env);
            evaluated = true;
            expression = null;
        }
        return value;
    }
}