package me.oskar.microhaskell.evaluation.expression;

import java.util.Map;

public interface Expression {
    Expression evaluate(Map<String, Expression> env);
}