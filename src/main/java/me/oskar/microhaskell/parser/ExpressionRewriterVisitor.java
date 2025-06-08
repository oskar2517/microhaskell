package me.oskar.microhaskell.parser;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.ast.visitor.AstRewriterVisitor;
import me.oskar.microhaskell.position.Span;
import me.oskar.microhaskell.table.OperatorEntry;
import me.oskar.microhaskell.table.SymbolTable;

import java.util.*;

public class ExpressionRewriterVisitor extends AstRewriterVisitor {

    private record OperatorInfo(String name, int precedence, OperatorEntry.Associativity associativity) {
    }

    public ExpressionRewriterVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    @Override
    protected AstRewriterVisitor createInstance(SymbolTable localTable) {
        return new ExpressionRewriterVisitor(localTable);
    }

    @Override
    public Node visit(FlatExpressionNode flatExpressionNode) {
        if (flatExpressionNode.getElements().isEmpty()) {
            throw new IllegalStateException("FlatExpressionNode cannot be empty");
        }

        var operandStack = new ArrayDeque<Node>();
        var operatorStack = new ArrayDeque<OperatorInfo>();

        for (var e : flatExpressionNode.getElements()) {
            if (e instanceof Node n) {
                operandStack.push(n.accept(this));
            } else {
                var operatorName = ((FlatExpressionNode.Operator) e).name();
                var operatorEntry = symbolTable.lookupOperator(operatorName);
                var operatorInfo = new OperatorInfo(
                        operatorName,
                        operatorEntry.getPrecedence(),
                        operatorEntry.getAssociativity()
                );

                while (!operatorStack.isEmpty() && hasPrecedence(operatorStack.peek(), operatorInfo)) {
                    reduce(operandStack, operatorStack.pop());
                }
                operatorStack.push(operatorInfo);
            }
        }

        while (!operatorStack.isEmpty()) {
            reduce(operandStack, operatorStack.pop());
        }

        return operandStack.pop();
    }

    private boolean hasPrecedence(OperatorInfo op1, OperatorInfo op2) {
        if (op1.precedence() > op2.precedence()) return true;
        if (op1.precedence() < op2.precedence()) return false;

        var assoc = op1.associativity();
        if (assoc == OperatorEntry.Associativity.NONE) {
            throw new IllegalStateException("Operator has no associativity: %s".formatted(op1.name));
        }

        return assoc == OperatorEntry.Associativity.LEFT;
    }

    private void reduce(Deque<Node> operandStack, OperatorInfo opEntry) {
        var right = (ExpressionNode) operandStack.pop();
        var left = (ExpressionNode) operandStack.pop();

        var operator = new IdentifierNode(Span.BASE_SPAN, opEntry.name());
        var leftApplication = new FunctionApplicationNode(left.getSpan(), operator, left);
        var fullApplication = new FunctionApplicationNode(new Span(left.getSpan().start(), right.getSpan().end()),
                leftApplication, right);

        operandStack.push(fullApplication);
    }
}