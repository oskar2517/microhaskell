package me.oskar.microhaskell.parser;

import me.oskar.microhaskell.ast.ExpressionNode;
import me.oskar.microhaskell.ast.FlatExpressionNode;
import me.oskar.microhaskell.ast.Node;
import me.oskar.microhaskell.ast.visitor.AstRewriterVisitor;
import me.oskar.microhaskell.table.OperatorEntry;
import me.oskar.microhaskell.table.SymbolTable;

public class ExpressionRewriterVisitor extends AstRewriterVisitor {

    public ExpressionRewriterVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    @Override
    protected AstRewriterVisitor createInstance(SymbolTable localTable) {
        return new ExpressionRewriterVisitor(localTable);
    }

    @Override
    public Node visit(FlatExpressionNode flatExpressionNode) {

    }
}
