package me.oskar.microhaskell.lexer;

public enum TokenType {

    IDENT("identifier"),
    INT("integer"),

    IF("if"),
    THEN("then"),
    ELSE("else"),
    LET("let"),
    IN("in"),

    L_PAREN("("),
    R_PAREN(")"),

    EQUAL("=="),
    NOT_EQUAL("!="),
    LESS_THAN("<"),
    LESS_THAN_EQUAL("<="),
    GREATER_THAN(">"),
    GREATER_THAN_EQUAL(">="),
    PLUS("+"),
    MINUS("-"),
    ASTERISK("*"),
    SLASH("/"),

    BACKSLASH("\\"),
    ARROW("->"),

    SEMICOLON(";"),

    DEFINE("="),
    EOF("EOF"),
    ILLEGAL("");

    public final String tokenName;

    TokenType(String tokenName) {
        this.tokenName = tokenName;
    }
}
