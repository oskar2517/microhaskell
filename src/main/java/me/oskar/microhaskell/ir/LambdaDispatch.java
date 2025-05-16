package me.oskar.microhaskell.ir;

import me.oskar.microhaskell.evaluation.expression.Expression;

public record LambdaDispatch(int id, Expression lambda) {
}
