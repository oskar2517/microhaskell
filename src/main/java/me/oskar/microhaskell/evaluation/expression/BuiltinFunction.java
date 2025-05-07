package me.oskar.microhaskell.evaluation.expression;


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

    abstract Expression applyFully(List<Expression> args);

    public Expression apply(List<Expression> newArgs, Map<String, Expression> env) {
        var combinedArguments = new java.util.ArrayList<>(partialArguments);
        combinedArguments.addAll(newArgs);

        if (combinedArguments.size() < arity) {
            return new CurriedBuiltinFunction(arity, combinedArguments, this::applyFully);
        } else if (combinedArguments.size() == arity) {
            return applyFully(combinedArguments);
        } else {
            throw new RuntimeException("Too many arguments to builtin function");
        }
    }

    public static BuiltinFunction of(int arity, Function<List<Expression>, Expression> op) {
        return new CurriedBuiltinFunction(arity, List.of(), op);
    }

    private static class CurriedBuiltinFunction extends BuiltinFunction {
        private final Function<List<Expression>, Expression> operation;

        public CurriedBuiltinFunction(int arity, List<Expression> argsSoFar, Function<List<Expression>, Expression> op) {
            super(arity, argsSoFar);
            this.operation = op;
        }

        @Override
        Expression applyFully(List<Expression> args) {
            return operation.apply(args);
        }

        @Override
        public Expression evaluate(Map<String, Expression> env) {
            return this;
        }
    }
}
