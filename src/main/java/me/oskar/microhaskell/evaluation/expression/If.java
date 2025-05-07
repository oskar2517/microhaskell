package me.oskar.microhaskell.evaluation.expression;

import java.util.Map;

public record If(Expression condition, Expression consequence, Expression alternative) implements Expression {

    @Override
    public Expression evaluate(Map<String, Expression> env) {
        var evaluatedCondition = condition.evaluate(env);
        if (!(evaluatedCondition instanceof IntLiteral(int value))) {
            throw new RuntimeException("Condition must evaluate to integer");
        }

        return (value != 0 ? consequence : alternative).evaluate(env);
    }

    @Override
    public String toString() {
        return "if %s then %s else %s".formatted(condition, consequence, alternative);
    }
}