package me.oskar.microhaskell.evaluation;

import me.oskar.microhaskell.evaluation.expression.BuiltinFunction;
import me.oskar.microhaskell.evaluation.expression.Expression;
import me.oskar.microhaskell.evaluation.expression.IntLiteral;

import java.util.HashMap;
import java.util.Map;

public class Builtins {

    public static Map<String, Expression> initialEnv() {
        var env = new HashMap<String, Expression>();

        env.put("+", BuiltinFunction.of(2, args -> {
            var arg1 = (IntLiteral) args.getFirst().evaluate(env);
            var arg2 = (IntLiteral) args.get(1).evaluate(env);

            return new IntLiteral(arg1.value() + arg2.value());
        }));

        env.put("-", BuiltinFunction.of(2, args -> {
            var arg1 = (IntLiteral) args.getFirst().evaluate(env);
            var arg2 = (IntLiteral) args.get(1).evaluate(env);

            return new IntLiteral(arg1.value() - arg2.value());
        }));

        env.put("*", BuiltinFunction.of(2, args -> {
            var arg1 = (IntLiteral) args.getFirst().evaluate(env);
            var arg2 = (IntLiteral) args.get(1).evaluate(env);

            return new IntLiteral(arg1.value() * arg2.value());
        }));

        env.put("/", BuiltinFunction.of(2, args -> {
            var arg1 = (IntLiteral) args.getFirst().evaluate(env);
            var arg2 = (IntLiteral) args.get(1).evaluate(env);

            return new IntLiteral(arg1.value() / arg2.value());
        }));

        env.put("==", BuiltinFunction.of(2, args -> {
            var arg1 = (IntLiteral) args.getFirst().evaluate(env);
            var arg2 = (IntLiteral) args.get(1).evaluate(env);

            return new IntLiteral(arg1.value() == arg2.value() ? 1 : 0);
        }));

        env.put("!=", BuiltinFunction.of(2, args -> {
            var arg1 = (IntLiteral) args.getFirst().evaluate(env);
            var arg2 = (IntLiteral) args.get(1).evaluate(env);

            return new IntLiteral(arg1.value() != arg2.value() ? 1 : 0);
        }));

        env.put("<=", BuiltinFunction.of(2, args -> {
            var arg1 = (IntLiteral) args.getFirst().evaluate(env);
            var arg2 = (IntLiteral) args.get(1).evaluate(env);

            return new IntLiteral(arg1.value() <= arg2.value() ? 1 : 0);
        }));

        env.put("<", BuiltinFunction.of(2, args -> {
            var arg1 = (IntLiteral) args.getFirst().evaluate(env);
            var arg2 = (IntLiteral) args.get(1).evaluate(env);

            return new IntLiteral(arg1.value() < arg2.value() ? 1 : 0);
        }));

        env.put(">=", BuiltinFunction.of(2, args -> {
            var arg1 = (IntLiteral) args.getFirst().evaluate(env);
            var arg2 = (IntLiteral) args.get(1).evaluate(env);

            return new IntLiteral(arg1.value() >= arg2.value() ? 1 : 0);
        }));

        env.put(">", BuiltinFunction.of(2, args -> {
            var arg1 = (IntLiteral) args.getFirst().evaluate(env);
            var arg2 = (IntLiteral) args.get(1).evaluate(env);

            return new IntLiteral(arg1.value() > arg2.value() ? 1 : 0);
        }));

        env.put("if", BuiltinFunction.of(3, args -> {
            var condition = (IntLiteral) args.getFirst().evaluate(env);
            if (condition.value() == 1) {
                var consequence = args.get(1);
                return consequence.evaluate(env);
            } else {
                var alternative = args.get(2);
                return alternative.evaluate(env);
            }
        }));

        return env;
    }
}
