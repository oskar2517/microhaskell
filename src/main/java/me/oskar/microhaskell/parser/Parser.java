package me.oskar.microhaskell.parser;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.lexer.Lexer;
import me.oskar.microhaskell.lexer.Token;
import me.oskar.microhaskell.lexer.TokenType;

import java.util.ArrayList;

public class Parser {

    private final Lexer lexer;
    private Token currentToken;

    public Parser(Lexer lexer) {
        this.lexer = lexer;

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

        throw new RuntimeException("Unexpected token: %s".formatted(currentToken));
    }

    public ProgramNode parse() {
        var definitions = new ArrayList<FunctionDefinitionNode>();

        while (currentToken.type() != TokenType.EOF) {
            definitions.add(parseFunctionDefinition());
        }

        return new ProgramNode(definitions);
    }

    private FunctionDefinitionNode parseFunctionDefinition() {
        var name = eatToken(TokenType.IDENT).lexeme();

        var parameters = new ArrayList<AtomicExpressionNode>();
        while (currentToken.type() != TokenType.DEFINE && currentToken.type() != TokenType.EOF) {
            parameters.add(parseAtomicExpression());
        }

        eatToken(TokenType.DEFINE);

        var expression = parseExpression();

        eatToken(TokenType.SEMICOLON);

        return new FunctionDefinitionNode(name, parameters, expression);
    }

    private ExpressionNode parseExpression() {
        return parseComparison();
    }

    private ExpressionNode parseComparison() {
        var left = parseNumeric();

        while (true) {
            var functionName = new IdentifierNode(currentToken.lexeme());

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
            final var right = parseNumeric();
            left = new FunctionApplicationNode(new FunctionApplicationNode(functionName, left), right);
        }
    }

    private ExpressionNode parseNumeric() {
        var left = parseTerm();

        while (true) {
            var functionName = new IdentifierNode(currentToken.lexeme());

            switch (currentToken.type()) {
                case PLUS:
                case MINUS:
                    break;
                default:
                    return left;
            }

            nextToken();
            final var right = parseTerm();
            left = new FunctionApplicationNode(new FunctionApplicationNode(functionName, left), right);
        }
    }

    private ExpressionNode parseTerm() {
        var left = parseApplication();

        while (true) {
            var functionName = new IdentifierNode(currentToken.lexeme());

            switch (currentToken.type()) {
                case ASTERISK:
                case SLASH:
                    break;
                default:
                    return left;
            }

            nextToken();
            final var right = parseApplication();
            left = new FunctionApplicationNode(new FunctionApplicationNode(functionName, left), right);
        }
    }

    private ExpressionNode parseApplication() {
        var left = parseFactor();

        while (currentToken.type() == TokenType.INT || currentToken.type() == TokenType.IDENT
                || currentToken.type() == TokenType.L_PAREN) {
            var argument = parseFactor();
            left = new FunctionApplicationNode(left, argument);
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
            default -> parseAtomicExpression();
        };
    }

    private AtomicExpressionNode parseAtomicExpression() {
        return switch (currentToken.type()) {
            case IDENT -> new IdentifierNode(eatToken(TokenType.IDENT).lexeme());
            case PLUS -> new IdentifierNode(eatToken(TokenType.PLUS).lexeme());
            case MINUS -> new IdentifierNode(eatToken(TokenType.MINUS).lexeme());
            case ASTERISK -> new IdentifierNode(eatToken(TokenType.ASTERISK).lexeme());
            case SLASH -> new IdentifierNode(eatToken(TokenType.SLASH).lexeme());
            case LESS_THAN -> new IdentifierNode(eatToken(TokenType.LESS_THAN).lexeme());
            case LESS_THAN_EQUAL -> new IdentifierNode(eatToken(TokenType.LESS_THAN_EQUAL).lexeme());
            case GREATER_THAN -> new IdentifierNode(eatToken(TokenType.GREATER_THAN).lexeme());
            case GREATER_THAN_EQUAL -> new IdentifierNode(eatToken(TokenType.GREATER_THAN_EQUAL).lexeme());
            case INT -> new IntLiteralNode(Integer.parseInt(eatToken(TokenType.INT).lexeme()));
            default -> throw new RuntimeException("Unexpected token: %s".formatted(currentToken));
        };
    }

    private IfNode parseIf() {
        eatToken(TokenType.IF);
        var condition = parseExpression();
        eatToken(TokenType.THEN);
        var consequence = parseExpression();
        eatToken(TokenType.ELSE);
        var alternative = parseExpression();

        return new IfNode(condition, consequence, alternative);
    }
}
