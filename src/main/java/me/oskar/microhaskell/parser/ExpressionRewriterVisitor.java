package me.oskar.microhaskell.parser;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.ast.visitor.AstRewriterVisitor;
import me.oskar.microhaskell.error.Error;
import me.oskar.microhaskell.position.Span;
import me.oskar.microhaskell.table.FixityEntry;
import me.oskar.microhaskell.table.SymbolTable;

import java.util.*;

public class ExpressionRewriterVisitor extends AstRewriterVisitor {

    private record OperatorInfo(String name, int precedence, FixityEntry.Associativity associativity) {
    }

    private final Error error;

    public ExpressionRewriterVisitor(SymbolTable symbolTable, Error error) {
        super(symbolTable);

        this.error = error;
    }

    @Override
    protected AstRewriterVisitor createInstance(SymbolTable localTable) {
        return new ExpressionRewriterVisitor(localTable, error);
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
                var fixityEntry = symbolTable.lookupFixity(operatorName);
                var operatorInfo = new OperatorInfo(
                        operatorName,
                        fixityEntry.precedence(),
                        fixityEntry.associativity()
                );

                while (!operatorStack.isEmpty()
                        && hasPrecedence(operatorStack.peek(), operatorInfo, flatExpressionNode)) {
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

    private boolean hasPrecedence(OperatorInfo op1, OperatorInfo op2, ExpressionNode expression) {
        if (op1.precedence() > op2.precedence()) return true;
        if (op1.precedence() < op2.precedence()) return false;

        var assoc = op1.associativity();
        if (assoc == FixityEntry.Associativity.NONE) {
            throw error.nonAssociativeOperatorInInfixExpression(expression, op1.name);
        }

        return assoc == FixityEntry.Associativity.LEFT;
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