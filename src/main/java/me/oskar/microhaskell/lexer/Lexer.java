package me.oskar.microhaskell.lexer;

import me.oskar.microhaskell.position.Position;
import me.oskar.microhaskell.position.Span;

public class Lexer {

    private static final char EOF = '\0';

    private final String code;
    private int position = 0;
    private int line = 1;
    private int lineOffset = -1;
    private char currentChar = EOF;

    public Lexer(String code) {
        this.code = code;
    }

    private boolean isAlphanumeric(char c) {
        return Character.isLetterOrDigit(c);
    }

    private boolean isValidIdentifierChar(char c) {
        return isAlphanumeric(c) || c == '_' || c == '\'';
    }

    private boolean isValidOperatorChar(char c) {
        return "!#$%&*+./<=>?@\\\\^|-~:".indexOf(c) >= 0;
    }

    private char readChar() {
        if (position < code.length()) {
            return code.charAt(position);
        } else {
            return EOF;
        }
    }

    private void updateLinePosition() {
        if (currentChar == '\n') {
            line++;
            lineOffset = -1;
        } else {
            lineOffset++;
        }
    }

    private void nextChar() {
        currentChar = readChar();
        updateLinePosition();
        position++;
    }

    private String readIdent() {
        var startPosition = position;
        while (isValidIdentifierChar(readChar())) {
            nextChar();
        }

        return code.substring(startPosition - 1, position);
    }

    private String readOperator() {
        var startPosition = position;
        while (isValidOperatorChar(readChar())) {
            nextChar();
        }

        return code.substring(startPosition - 1, position);
    }

    private String readIntegerLiteral() {
        var startPosition = position;
        while (Character.isDigit(readChar())) {
            nextChar();
        }

        return code.substring(startPosition - 1, position);
    }

    private void eatWhitespace() {
        while (Character.isWhitespace(currentChar)) {
            nextChar();
        }
    }

    private void eatComment() {
        if (currentChar != '-' || readChar() != '-') {
            return;
        }

        while (currentChar != '\n' && currentChar != EOF) {
            nextChar();
        }

        eatWhitespace();
        eatComment();
    }

    public void printTokens() {
        while (true) {
            var token = nextToken();
            System.out.println(token);
            if (token.type() == TokenType.EOF) {
                break;
            }
        }
    }

    private Span span(Position startPosition, int length) {
        return new Span(startPosition, new Position(startPosition.line(),
                startPosition.lineOffset() + length));
    }

    public Token nextToken() {
        nextChar();
        eatWhitespace();
        eatComment();

        var startPosition = new Position(line, lineOffset);

        return switch (currentChar) {
            case ';' -> new Token(TokenType.SEMICOLON, ";", span(startPosition, 1));
            case '(' -> new Token(TokenType.L_PAREN, "(", span(startPosition, 1));
            case ')' -> new Token(TokenType.R_PAREN, ")", span(startPosition, 1));
            case EOF -> new Token(TokenType.EOF, String.valueOf(EOF), span(startPosition, 1));
            default -> {
                if (Character.isDigit(currentChar)) {
                    var literal = readIntegerLiteral();
                    yield new Token(TokenType.INT, literal, span(startPosition, literal.length()));
                }

                if (isAlphanumeric(currentChar) || currentChar == '_') {
                    var ident = readIdent();
                    if (Keyword.isKeyword(ident)) {
                        yield new Token(Keyword.resolve(ident), ident, span(startPosition, ident.length()));
                    } else {
                        yield new Token(TokenType.IDENT, ident, span(startPosition, ident.length()));
                    }
                }

                if (currentChar == '-' && readChar() == '>') {
                    nextChar();
                    yield new Token(TokenType.ARROW, "->", span(startPosition, 2));
                }

                if (currentChar == '=' && !isValidOperatorChar(readChar())) {
                    yield new Token(TokenType.DEFINE, "=", span(startPosition, 1));
                }

                if (currentChar == '\\' && !isValidOperatorChar(readChar())) {
                    yield new Token(TokenType.BACKSLASH, "\\", span(startPosition, 1));
                }

                if (isValidOperatorChar(currentChar)) {
                    var operator = readOperator();
                    yield new Token(TokenType.OPERATOR, operator, span(startPosition, operator.length()));
                }

                yield new Token(TokenType.ILLEGAL, String.valueOf(currentChar), span(startPosition, 1));
            }
        };
    }
}
