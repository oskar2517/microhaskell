package me.oskar.microhaskell.repl;

import me.oskar.microhaskell.lexer.Lexer;
import me.oskar.microhaskell.lexer.Token;
import me.oskar.microhaskell.lexer.TokenType;

class Highlighter {

    private static final String KEYWORD1 = "\u001B[38;5;69m";   // #5499D2
    private static final String KEYWORD2 = "\u001B[38;5;139m";  // #C586C0
    private static final String IDENT = "\u001B[38;5;117m";     // #9CDCFE
    private static final String NUMBER = "\u001B[38;5;151m";    // #B5CEA8
    private static final String DEFAULT = "\u001B[38;5;250m";   // #D4D4D4
    private static final String RESET = "\u001B[0m";

    private final String code;
    private final Lexer lexer;

    Highlighter(String code) {
        this.code = code;
        this.lexer = new Lexer(code);
    }

    public void highlight() {
        var highlightedLine = new StringBuilder();

        while (true) {
            var token = lexer.nextToken();

            if (token.type() == TokenType.EOF) break;

            var highlightedToken = switch (token.type()) {
                case IDENT -> highlightToken(token, IDENT);
                case INT -> highlightToken(token, NUMBER);
                case INFIX, INFIX_L, INFIX_R, LET, IN -> highlightToken(token, KEYWORD1);
                case IF, THEN, ELSE -> highlightToken(token, KEYWORD2);
                default -> highlightToken(token, DEFAULT);
            };

            highlightedLine.append(highlightedToken);
        }

        System.out.print("\033[1A");
        System.out.print("\033[2K");
        System.out.print("mhs> ");
        System.out.println(highlightedLine);
    }

    private String highlightToken(Token token, String color) {
        var currentSpan = token.span();
        var peekSpan = lexer.peekToken().span();

        return color
                + code.substring(currentSpan.start().lineOffset(), currentSpan.end().lineOffset())
                + code.substring(currentSpan.end().lineOffset(), peekSpan.start().lineOffset())
                + RESET;
    }
}
