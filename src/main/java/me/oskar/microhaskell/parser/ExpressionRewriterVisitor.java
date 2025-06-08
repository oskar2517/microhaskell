package me.oskar.microhaskell.parser;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.ast.visitor.AstRewriterVisitor;
import me.oskar.microhaskell.position.Span;
import me.oskar.microhaskell.table.OperatorEntry;
import me.oskar.microhaskell.table.SymbolTable;

import java.util.*;

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
        List<FlatExpressionElement> elements = flatExpressionNode.getElements();
        if (elements.isEmpty()) return null;

        Deque<Node> operandStack = new ArrayDeque<>();
        Deque<OperatorEntry> operatorStack = new ArrayDeque<>();

        for (FlatExpressionElement elem : elements) {
            if (elem instanceof Node n) {
                operandStack.push(n.accept(this));
            } else {
                String opName = ((FlatExpressionNode.Operator) elem).name();
                OperatorEntry entry = symbolTable.lookupOperator(opName);
                if (entry == null) throw new RuntimeException("Unknown operator: " + opName);

                while (!operatorStack.isEmpty() && hasPrecedence(operatorStack.peek(), entry)) {
                    reduce(operandStack, operatorStack.pop());
                }
                operatorStack.push(entry);
            }
        }
        while (!operatorStack.isEmpty()) {
            reduce(operandStack, operatorStack.pop());
        }
        return operandStack.pop();
    }

    private boolean hasPrecedence(OperatorEntry op1, OperatorEntry op2) {
        if (op1.getPrecedence() > op2.getPrecedence()) return true;
        if (op1.getPrecedence() < op2.getPrecedence()) return false;
        // If same precedence, check associativity
        return op1.getAssociativity() == OperatorEntry.Associativity.LEFT;
    }

    private void reduce(Deque<Node> operandStack, OperatorEntry opEntry) {
        var right = (ExpressionNode) operandStack.pop();
        var left = (ExpressionNode) operandStack.pop();
        var opNode = new IdentifierNode(Span.BASE_SPAN, opEntry.getOperatorName());
        var leftApp = new FunctionApplicationNode(Span.BASE_SPAN, opNode, left);
        Node fullApp = new FunctionApplicationNode(Span.BASE_SPAN, leftApp, right);
        operandStack.push(fullApp);
    }
}