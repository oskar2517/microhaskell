package me.oskar.microhaskell.lexer;

public class Lexer {

    private static final char EOF = '\0';

    private final String code;
    private int position = 0;
    private char currentChar = EOF;

    public Lexer(String code) {
        this.code = code;
    }

    private boolean isAlphanumeric(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private char readChar() {
        if (position < code.length()) {
            return code.charAt(position);
        } else {
            return EOF;
        }
    }

    private void nextChar() {
        currentChar = readChar();
        position++;
    }

    private String readIdent() {
        var startPosition = position;
        while (isAlphanumeric(readChar())) {
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

    public Token peekToken() {
        var oldPosition = position;
        var oldCurrentChar = currentChar;

        var token = nextToken();

        position = oldPosition;
        currentChar = oldCurrentChar;

        return token;
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

    public Token nextToken() {
        nextChar();
        eatWhitespace();
        eatComment();

        return switch (currentChar) {
            case ';' -> new Token(TokenType.SEMICOLON, ";");
            case '(' -> new Token(TokenType.L_PAREN, "(");
            case ')' -> new Token(TokenType.R_PAREN, ")");
            case '+' -> new Token(TokenType.PLUS, "+");
            case '-' -> new Token(TokenType.MINUS, "-");
            case '*' -> new Token(TokenType.ASTERISK, "*");
            case '/' -> new Token(TokenType.SLASH, "/");
            case '=' -> {
                if (readChar() == '=') {
                    nextChar();
                    yield new Token(TokenType.EQUAL, "==");
                }
                yield new Token(TokenType.DEFINE, "0");
            }
            case '!' -> {
                if (readChar() == '=') {
                    yield new Token(TokenType.NOT_EQUAL, "!=");
                }
                yield new Token(TokenType.ILLEGAL, String.valueOf(currentChar));
            }
            case '<' -> {
                if (readChar() == '=') {
                    nextChar();
                    yield new Token(TokenType.LESS_THAN_EQUAL, "<=");
                } else {
                    yield new Token(TokenType.LESS_THAN, "<");
                }
            }
            case '>' -> {
                if (readChar() == '=') {
                    nextChar();
                    yield new Token(TokenType.GREATER_THAN_EQUAL, ">=");
                } else {
                    yield new Token(TokenType.GREATER_THAN, ">");
                }
            }
            case EOF -> new Token(TokenType.EOF, String.valueOf(EOF));
            default -> {
                if (Character.isDigit(currentChar)) {
                    yield new Token(TokenType.INT, readIntegerLiteral());
                } else if (isAlphanumeric(currentChar)) {
                    var ident = readIdent();
                    if (Keyword.isKeyword(ident)) {
                        yield new Token(Keyword.resolve(ident), ident);
                    } else {
                        yield new Token(TokenType.IDENT, ident);
                    }
                } else {
                    yield new Token(TokenType.ILLEGAL, String.valueOf(currentChar));
                }
            }
        };
    }
}
