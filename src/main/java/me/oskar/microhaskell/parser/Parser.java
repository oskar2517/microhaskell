package me.oskar.microhaskell.parser;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.error.Error;
import me.oskar.microhaskell.error.CompileTimeError;
import me.oskar.microhaskell.lexer.Lexer;
import me.oskar.microhaskell.lexer.Token;
import me.oskar.microhaskell.lexer.TokenType;
import me.oskar.microhaskell.position.Span;
import me.oskar.microhaskell.table.OperatorEntry;

import java.util.ArrayList;

public class Parser {

    private final Lexer lexer;
    private final Error error;
    private Token currentToken;

    public Parser(Lexer lexer, Error error) {
        this.lexer = lexer;
        this.error = error;

        nextToken();
    }

    private void nextToken() {
        currentToken = lexer.nextToken();
    }

    private Token eatToken(TokenType type) {
        if (currentToken.type() == type) {
            var t = currentToken;
            nextToken();
            return t;
        }

        error.unexpectedToken(currentToken, "`%s`".formatted(type));

        throw new CompileTimeError();
    }

    private boolean matchToken(TokenType type) {
        if (currentToken.type() == type) {
            eatToken(type);

            return true;
        }

        return false;
    }

    public ProgramNode parse() {
        var startPosition = currentToken.span().start();

        var bindings = new ArrayList<Node>();

        while (currentToken.type() != TokenType.EOF) {
            bindings.add(parseBinding());
            eatToken(TokenType.SEMICOLON);
        }

        return new ProgramNode(new Span(startPosition, currentToken.span().end()), bindings);
    }

    private Node parseBinding() {
        if (currentToken.type() == TokenType.INFIX
                || currentToken.type() == TokenType.INFIX_L
                || currentToken.type() == TokenType.INFIX_R) {
            return parseFixity();
        }

        return parseFunctionDefinition();
    }

    private FixityNode parseFixity() {
        var startPosition = currentToken.span().start();

        var associativity = switch (currentToken.type()) {
            case INFIX -> OperatorEntry.Associativity.NONE;
            case INFIX_L -> OperatorEntry.Associativity.LEFT;
            case INFIX_R -> OperatorEntry.Associativity.RIGHT;
            default -> {
                error.unexpectedToken(currentToken, "`%s`".formatted(currentToken));
                yield null;
            }
        };

        nextToken();

        var precedence = 0;
        var precedenceToken = currentToken;
        try {
            precedence = Integer.parseInt(eatToken(TokenType.INT).lexeme());
            if (precedence < 0 || precedence > 9) {
                error.invalidOperatorPrecedence(precedenceToken);
            }
        } catch (NumberFormatException e) {
            error.invalidOperatorPrecedence(precedenceToken);
        }

        var operatorToken = currentToken;
        var operatorName = eatToken(TokenType.OPERATOR).lexeme();

        return new FixityNode(new Span(startPosition, operatorToken.span().end()),
                associativity, precedence, operatorName);
    }

    private FunctionDefinitionNode parseFunctionDefinition() {
        var startPosition = currentToken.span().start();

        var name = parseFunctionName();

        var parameters = new ArrayList<AtomicExpressionNode>();
        while (currentToken.type() != TokenType.DEFINE && currentToken.type() != TokenType.EOF) {
            parameters.add(parseAtomicExpression());
        }

        eatToken(TokenType.DEFINE);

        var expression = parseExpression();

        return new FunctionDefinitionNode(new Span(startPosition, expression.getSpan().end()), name,
                parameters, expression);
    }

    private String parseFunctionName() {
        if (currentToken.type() == TokenType.IDENT) {
            return eatToken(TokenType.IDENT).lexeme();
        }

        if (currentToken.type() == TokenType.L_PAREN) {
            eatToken(TokenType.L_PAREN);
            var name = eatToken(TokenType.OPERATOR).lexeme();
            eatToken(TokenType.R_PAREN);

            return name;
        }

        error.invalidFunctionName(currentToken);
        throw new CompileTimeError();
    }

    private AnonymousFunctionNode parseAnonymousFunction() {
        var startPosition = currentToken.span().start();

        eatToken(TokenType.BACKSLASH);

        var parameters = new ArrayList<AtomicExpressionNode>();
        do {
            parameters.add(parseAtomicExpression());
        } while (currentToken.type() != TokenType.ARROW && currentToken.type() != TokenType.EOF);

        eatToken(TokenType.ARROW);

        var expression = parseExpression();

        return new AnonymousFunctionNode(new Span(startPosition, expression.getSpan().end()), parameters, expression);
    }

    private ExpressionNode parseExpression() {
        if (currentToken.type() == TokenType.LET) {
            return parseLet();
        }

        var startPosition = currentToken.span().start();

        var elements = new ArrayList<FlatExpressionElement>();

        var lastElement = parseApplication();
        elements.add(lastElement);

        while (currentToken.type() == TokenType.OPERATOR) {
            elements.add(new FlatExpressionNode.Operator(eatToken(TokenType.OPERATOR).lexeme()));
            lastElement = parseApplication();
            elements.add(lastElement);
        }

        return new FlatExpressionNode(new Span(startPosition, lastElement.getSpan().end()), elements);
    }

    private ExpressionNode parseLet() {
        var startPosition = currentToken.span().start();

        eatToken(TokenType.LET);

        var bindings = new ArrayList<Node>();

        do {
            bindings.add(parseBinding());
        } while (matchToken(TokenType.SEMICOLON));

        eatToken(TokenType.IN);

        var expression = parseExpression();

        return new LetNode(new Span(startPosition, expression.getSpan().end()), bindings, expression);
    }

    private ExpressionNode parseApplication() {
        var left = parseFactor();

        while (currentToken.type() == TokenType.INT || currentToken.type() == TokenType.IDENT
                || currentToken.type() == TokenType.L_PAREN) {
            var argument = parseFactor();
            left = new FunctionApplicationNode(new Span(left.getSpan().start(), argument.getSpan().end()),
                    left, argument);
        }

        return left;
    }

    private ExpressionNode parseFactor() {
        var span = currentToken.span();

        return switch (currentToken.type()) {
            case L_PAREN -> {
                eatToken(TokenType.L_PAREN);

                ExpressionNode expression;
                if (currentToken.type() == TokenType.OPERATOR) {
                    expression = new IdentifierNode(span, eatToken(TokenType.OPERATOR).lexeme());
                } else {
                    expression = parseExpression();
                }

                eatToken(TokenType.R_PAREN);

                yield expression;
            }
            case IF -> parseIf();
            case BACKSLASH -> parseAnonymousFunction();
            case IDENT -> new IdentifierNode(span, eatToken(TokenType.IDENT).lexeme());
            case INT -> new IntLiteralNode(span, Integer.parseInt(eatToken(TokenType.INT).lexeme()));
            default -> {
                error.unexpectedToken(currentToken, "expression");
                throw new CompileTimeError();
            }
        };
    }

    private AtomicExpressionNode parseAtomicExpression() {
        var span = currentToken.span();

        return switch (currentToken.type()) {
            case IDENT -> new IdentifierNode(span, eatToken(TokenType.IDENT).lexeme());
            case INT -> new IntLiteralNode(span, Integer.parseInt(eatToken(TokenType.INT).lexeme()));
            default -> {
                error.unexpectedToken(currentToken, "atomic expression");
                throw new CompileTimeError();
            }
        };
    }

    private IfNode parseIf() {
        var startPosition = currentToken.span().start();

        eatToken(TokenType.IF);
        var condition = parseExpression();
        eatToken(TokenType.THEN);
        var consequence = parseExpression();
        eatToken(TokenType.ELSE);
        var alternative = parseExpression();

        return new IfNode(new Span(startPosition, alternative.getSpan().end()), condition, consequence, alternative);
    }
}
