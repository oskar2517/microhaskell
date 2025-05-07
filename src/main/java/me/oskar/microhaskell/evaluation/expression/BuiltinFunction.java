package me.oskar.microhaskell.evaluation.expression;


import com.sun.jdi.Value;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class BuiltinFunction implements Expression {

    protected final List<Expression> partialArguments;
    protected final int arity;

    protected BuiltinFunction(int arity, List<Expression> partialArguments) {
        this.arity = arity;
        this.partialArguments = partialArguments;
    }

    @Override
    public String toString() {
        return "<builtin function>";
    }

    abstract IntLiteral applyFully(List<IntLiteral> args);

    public Expression apply(List<Expression> newArgs) {
        var combinedArguments = new java.util.ArrayList<>(partialArguments);
        combinedArguments.addAll(newArgs);

        if (combinedArguments.size() < arity) {
            return new CurriedBuiltinFunction(arity, combinedArguments, this::applyFully);
        } else if (combinedArguments.size() == arity) {
            var integerArguments = combinedArguments.stream()
                    .map(v -> {
                        if (v instanceof IntLiteral i) {
                            return i;
                        }
                        throw new RuntimeException("Expected integer but got: %s".formatted(v));
                    })
                    .toList();
            return applyFully(integerArguments);
        } else {
            throw new RuntimeException("Too many arguments to builtin function");
        }
    }

    public static BuiltinFunction of(int arity, Function<List<IntLiteral>, IntLiteral> op) {
        return new CurriedBuiltinFunction(arity, List.of(), op);
    }

    private static class CurriedBuiltinFunction extends BuiltinFunction {
        private final Function<List<IntLiteral>, IntLiteral> operation;

        public CurriedBuiltinFunction(int arity, List<Expression> argsSoFar, Function<List<IntLiteral>, IntLiteral> op) {
            super(arity, argsSoFar);
            this.operation = op;
        }

        @Override
        IntLiteral applyFully(List<IntLiteral> args) {
            return operation.apply(args);
        }

        @Override
        public Expression evaluate(Map<String, Expression> env) {
            return this;
        }
    }
}
