package me.oskar.microhaskell.ast;

import java.util.List;

public interface FunctionNode {

    ExpressionNode getBody();

    List<AtomicExpressionNode> getParameters();
}
