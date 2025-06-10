package me.oskar.microhaskell.lexer;

public enum TokenType {

    IDENT("identifier"),
    OPERATOR("operator"),
    INT("integer"),

    INFIX("infix"),
    INFIX_L("infixl"),
    INFIX_R("infixr"),

    IF("if"),
    THEN("then"),
    ELSE("else"),
    LET("let"),
    IN("in"),

    L_PAREN("("),
    R_PAREN(")"),
    L_BRACK("["),
    R_BRACK("]"),

    BACKSLASH("\\"),
    ARROW("->"),

    SEMICOLON(";"),
    COMMA(","),
    BACKTICK("`"),

    DEFINE("="),
    EOF("EOF"),
    ILLEGAL("");

    public final String tokenName;

    TokenType(String tokenName) {
        this.tokenName = tokenName;
    }
}
