package me.oskar.microhaskell.evaluation.expression;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class BuiltinFunction implements Expression {

    protected final List<Thunk> partialArguments;
    protected final int arity;

    protected BuiltinFunction(int arity, List<Thunk> partialArguments) {
        this.arity = arity;
        this.partialArguments = partialArguments;
    }

    @Override
    public String toString() {
        return "<builtin function>";
    }

    abstract Expression applyFully(List<Thunk> args);

    public Expression apply(List<Thunk> newArgs, Map<String, Expression> env) {
        var combinedArguments = new ArrayList<>(partialArguments);
        combinedArguments.addAll(newArgs);

        if (combinedArguments.size() < arity) {
            return new CurriedBuiltinFunction(arity, combinedArguments, this::applyFully);
        } else if (combinedArguments.size() == arity) {
            return applyFully(combinedArguments);
        } else {
            throw new RuntimeException("Too many arguments to builtin function");
        }
    }

    public static BuiltinFunction of(int arity, Function<List<Thunk>, Expression> op) {
        return new CurriedBuiltinFunction(arity, List.of(), op);
    }

    private static class CurriedBuiltinFunction extends BuiltinFunction {
        private final Function<List<Thunk>, Expression> operation;

        public CurriedBuiltinFunction(int arity, List<Thunk> argsSoFar, Function<List<Thunk>, Expression> op) {
            super(arity, argsSoFar);
            this.operation = op;
        }

        @Override
        Expression applyFully(List<Thunk> args) {
            return operation.apply(args);
        }

        @Override
        public Expression evaluate(Map<String, Expression> env) {
            return this;
        }
    }
}
