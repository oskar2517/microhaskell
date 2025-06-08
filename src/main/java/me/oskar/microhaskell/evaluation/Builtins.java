package me.oskar.microhaskell.evaluation;

import me.oskar.microhaskell.evaluation.expression.BuiltinFunction;
import me.oskar.microhaskell.evaluation.expression.Expression;
import me.oskar.microhaskell.evaluation.expression.IntLiteral;
import me.oskar.microhaskell.table.OperatorEntry;
import me.oskar.microhaskell.table.SymbolTable;
import me.oskar.microhaskell.table.VariableEntry;

import java.util.HashMap;
import java.util.Map;

public class Builtins {

    public static Map<String, Expression> initialEnv(SymbolTable symbolTable) {
        var env = new HashMap<String, Expression>();

        env.put("+", BuiltinFunction.of(2, args -> {
            var arg1 = (IntLiteral) args.getFirst().evaluate(env);
            var arg2 = (IntLiteral) args.get(1).evaluate(env);

            return new IntLiteral(arg1.value() + arg2.value());
        }));
        symbolTable.enter("+", new OperatorEntry(OperatorEntry.Associativity.LEFT, 6));

        env.put("-", BuiltinFunction.of(2, args -> {
            var arg1 = (IntLiteral) args.getFirst().evaluate(env);
            var arg2 = (IntLiteral) args.get(1).evaluate(env);

            return new IntLiteral(arg1.value() - arg2.value());
        }));
        symbolTable.enter("-", new OperatorEntry(OperatorEntry.Associativity.LEFT, 6));

        env.put("*", BuiltinFunction.of(2, args -> {
            var arg1 = (IntLiteral) args.getFirst().evaluate(env);
            var arg2 = (IntLiteral) args.get(1).evaluate(env);

            return new IntLiteral(arg1.value() * arg2.value());
        }));
        symbolTable.enter("*", new OperatorEntry(OperatorEntry.Associativity.LEFT, 7));

        env.put("/", BuiltinFunction.of(2, args -> {
            var arg1 = (IntLiteral) args.getFirst().evaluate(env);
            var arg2 = (IntLiteral) args.get(1).evaluate(env);

            return new IntLiteral(arg1.value() / arg2.value());
        }));
        symbolTable.enter("/", new OperatorEntry(OperatorEntry.Associativity.LEFT, 7));

        env.put("==", BuiltinFunction.of(2, args -> {
            var arg1 = (IntLiteral) args.getFirst().evaluate(env);
            var arg2 = (IntLiteral) args.get(1).evaluate(env);

            return new IntLiteral(arg1.value() == arg2.value() ? 1 : 0);
        }));
        symbolTable.enter("==", new OperatorEntry(OperatorEntry.Associativity.LEFT, 4));

        env.put("/=", BuiltinFunction.of(2, args -> {
            var arg1 = (IntLiteral) args.getFirst().evaluate(env);
            var arg2 = (IntLiteral) args.get(1).evaluate(env);

            return new IntLiteral(arg1.value() != arg2.value() ? 1 : 0);
        }));
        symbolTable.enter("/=", new OperatorEntry(OperatorEntry.Associativity.LEFT, 4));

        env.put("<=", BuiltinFunction.of(2, args -> {
            var arg1 = (IntLiteral) args.getFirst().evaluate(env);
            var arg2 = (IntLiteral) args.get(1).evaluate(env);

            return new IntLiteral(arg1.value() <= arg2.value() ? 1 : 0);
        }));
        symbolTable.enter("<=", new OperatorEntry(OperatorEntry.Associativity.LEFT, 4));

        env.put("<", BuiltinFunction.of(2, args -> {
            var arg1 = (IntLiteral) args.getFirst().evaluate(env);
            var arg2 = (IntLiteral) args.get(1).evaluate(env);

            return new IntLiteral(arg1.value() < arg2.value() ? 1 : 0);
        }));
        symbolTable.enter("<", new OperatorEntry(OperatorEntry.Associativity.LEFT, 4));

        env.put(">=", BuiltinFunction.of(2, args -> {
            var arg1 = (IntLiteral) args.getFirst().evaluate(env);
            var arg2 = (IntLiteral) args.get(1).evaluate(env);

            return new IntLiteral(arg1.value() >= arg2.value() ? 1 : 0);
        }));
        symbolTable.enter(">=", new OperatorEntry(OperatorEntry.Associativity.LEFT, 4));

        env.put(">", BuiltinFunction.of(2, args -> {
            var arg1 = (IntLiteral) args.getFirst().evaluate(env);
            var arg2 = (IntLiteral) args.get(1).evaluate(env);

            return new IntLiteral(arg1.value() > arg2.value() ? 1 : 0);
        }));
        symbolTable.enter(">", new OperatorEntry(OperatorEntry.Associativity.LEFT, 4));

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
        symbolTable.enter("if", new VariableEntry());

        return env;
    }
}
