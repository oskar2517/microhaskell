package me.oskar.microhaskell.evaluation;

import me.oskar.microhaskell.evaluation.expression.BuiltinFunction;
import me.oskar.microhaskell.evaluation.expression.Expression;
import me.oskar.microhaskell.evaluation.expression.IntLiteral;

import java.util.HashMap;
import java.util.Map;

public class Builtins {

    public static Map<String, Expression> initialEnv() {
        var env = new HashMap<String, Expression>();

        env.put("+", BuiltinFunction.of(2,
                args -> new IntLiteral(args.getFirst().value() + args.get(1).value())));

        env.put("-", BuiltinFunction.of(2,
                args -> new IntLiteral(args.getFirst().value() - args.get(1).value())));

        env.put("*", BuiltinFunction.of(2,
                args -> new IntLiteral(args.getFirst().value() * args.get(1).value())));

        env.put("/", BuiltinFunction.of(2,
                args -> new IntLiteral(args.getFirst().value() / args.get(1).value())));

        env.put("==", BuiltinFunction.of(2,
                args -> new IntLiteral(args.getFirst().value() == args.get(1).value() ? 1 : 0)));

        env.put("!=", BuiltinFunction.of(2,
                args -> new IntLiteral(args.getFirst().value() != args.get(1).value() ? 1 : 0)));

        env.put("<=", BuiltinFunction.of(2,
                args -> new IntLiteral(args.getFirst().value() <= args.get(1).value() ? 1 : 0)));

        env.put("<", BuiltinFunction.of(2,
                args -> new IntLiteral(args.getFirst().value() < args.get(1).value() ? 1 : 0)));

        env.put(">=", BuiltinFunction.of(2,
                args -> new IntLiteral(args.getFirst().value() >= args.get(1).value() ? 1 : 0)));

        env.put(">", BuiltinFunction.of(2,
                args -> new IntLiteral(args.getFirst().value() > args.get(1).value() ? 1 : 0)));

        return env;
    }
}
