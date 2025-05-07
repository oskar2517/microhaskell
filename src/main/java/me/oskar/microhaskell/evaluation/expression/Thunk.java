package me.oskar.microhaskell.evaluation.expression;

import java.util.Map;

public class Thunk implements Expression {

    private final Expression expression;
    private final Map<String, Expression> env;
    private Expression value = null;

    public Thunk(Expression expression, Map<String, Expression> env) {
        this.expression = expression;
        this.env = env;
    }
    
    @Override
    public Expression evaluate(Map<String, Expression> outerEnv) {
        if (value == null) {
            value = expression.evaluate(env);
        }
        return value;
    }
}