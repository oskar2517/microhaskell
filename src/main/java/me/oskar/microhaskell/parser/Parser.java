package me.oskar.microhaskell.parser;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.error.Error;
import me.oskar.microhaskell.error.ParsingError;
import me.oskar.microhaskell.lexer.Lexer;
import me.oskar.microhaskell.lexer.Token;
import me.oskar.microhaskell.lexer.TokenType;
import me.oskar.microhaskell.position.Span;

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

        throw new ParsingError();
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

        var bindings = new ArrayList<FunctionDefinitionNode>();

        while (currentToken.type() != TokenType.EOF) {
            bindings.add(parseFunctionDefinition());
            eatToken(TokenType.SEMICOLON);
        }

        return new ProgramNode(new Span(startPosition, currentToken.span().end()), bindings);
    }

    private FunctionDefinitionNode parseFunctionDefinition() {
        var startPosition = currentToken.span().start();

        var name = eatToken(TokenType.IDENT).lexeme();

        var parameters = new ArrayList<AtomicExpressionNode>();
        while (currentToken.type() != TokenType.DEFINE && currentToken.type() != TokenType.EOF) {
            parameters.add(parseAtomicExpression());
        }

        eatToken(TokenType.DEFINE);

        var expression = parseExpression();

        return new FunctionDefinitionNode(new Span(startPosition, expression.getSpan().end()), name,
                parameters, expression);
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
        return parseLet();
    }

    private ExpressionNode parseLet() {
        if (currentToken.type() != TokenType.LET) {
            return parseComparison();
        }

        var startPosition = currentToken.span().start();

        eatToken(TokenType.LET);

        var bindings = new ArrayList<FunctionDefinitionNode>();

        do {
            bindings.add(parseFunctionDefinition());
        } while (matchToken(TokenType.SEMICOLON));

        eatToken(TokenType.IN);

        var expression = parseExpression();

        return new LetNode(new Span(startPosition, expression.getSpan().end()), bindings, expression);
    }

    private ExpressionNode parseComparison() {
        var left = parseNumeric();

        while (true) {
            var functionName = new IdentifierNode(currentToken.span(), currentToken.lexeme());

            switch (currentToken.type()) {
                case LESS_THAN:
                case LESS_THAN_EQUAL:
                case GREATER_THAN:
                case GREATER_THAN_EQUAL:
                case EQUAL:
                case NOT_EQUAL:
                    break;
                default:
                    return left;
            }

            nextToken();
            var right = parseNumeric();
            var span = new Span(left.getSpan().end(), right.getSpan().end());
            left = new FunctionApplicationNode(span, new FunctionApplicationNode(span, functionName, left), right);
        }
    }

    private ExpressionNode parseNumeric() {
        var left = parseTerm();

        while (true) {
            var functionName = new IdentifierNode(currentToken.span(), currentToken.lexeme());

            switch (currentToken.type()) {
                case PLUS:
                case MINUS:
                    break;
                default:
                    return left;
            }

            nextToken();
            var right = parseTerm();
            var span = new Span(left.getSpan().end(), right.getSpan().end());
            left = new FunctionApplicationNode(span, new FunctionApplicationNode(span, functionName, left), right);
        }
    }

    private ExpressionNode parseTerm() {
        var left = parseApplication();

        while (true) {
            var functionName = new IdentifierNode(currentToken.span(), currentToken.lexeme());

            switch (currentToken.type()) {
                case ASTERISK:
                case SLASH:
                    break;
                default:
                    return left;
            }

            nextToken();
            var right = parseApplication();
            var span = new Span(left.getSpan().end(), right.getSpan().end());
            left = new FunctionApplicationNode(span, new FunctionApplicationNode(span, functionName, left), right);
        }
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
        return switch (currentToken.type()) {
            case L_PAREN -> {
                eatToken(TokenType.L_PAREN);
                final var expression = parseExpression();
                eatToken(TokenType.R_PAREN);

                yield expression;
            }
            case IF -> parseIf();
            case BACKSLASH -> parseAnonymousFunction();
            default -> parseAtomicExpression();
        };
    }

    private AtomicExpressionNode parseAtomicExpression() {
        var span = currentToken.span();

        return switch (currentToken.type()) {
            case IDENT -> new IdentifierNode(span, eatToken(TokenType.IDENT).lexeme());
            case PLUS -> new IdentifierNode(span, eatToken(TokenType.PLUS).lexeme());
            case MINUS -> new IdentifierNode(span, eatToken(TokenType.MINUS).lexeme());
            case ASTERISK -> new IdentifierNode(span, eatToken(TokenType.ASTERISK).lexeme());
            case SLASH -> new IdentifierNode(span, eatToken(TokenType.SLASH).lexeme());
            case LESS_THAN -> new IdentifierNode(span, eatToken(TokenType.LESS_THAN).lexeme());
            case LESS_THAN_EQUAL -> new IdentifierNode(span, eatToken(TokenType.LESS_THAN_EQUAL).lexeme());
            case GREATER_THAN -> new IdentifierNode(span, eatToken(TokenType.GREATER_THAN).lexeme());
            case GREATER_THAN_EQUAL -> new IdentifierNode(span, eatToken(TokenType.GREATER_THAN_EQUAL).lexeme());
            case INT -> new IntLiteralNode(span, Integer.parseInt(eatToken(TokenType.INT).lexeme()));
            default -> {
                error.unexpectedToken(currentToken, "expression");
                throw new ParsingError();
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
