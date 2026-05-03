package me.oskar.microhaskell.ast;

import java.util.List;

public interface ParameterizedNode {

    ExpressionNode getBody();

    List<AtomicExpressionNode> getParameters();
}
