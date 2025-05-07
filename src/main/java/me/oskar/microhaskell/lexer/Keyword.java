package me.oskar.microhaskell.lexer;

import java.util.Map;

public class Keyword {

    static final Map<String, TokenType> keywords = Map.ofEntries(
            Map.entry("if", TokenType.IF),
            Map.entry("then", TokenType.THEN),
            Map.entry("else", TokenType.ELSE),
            Map.entry("while", TokenType.THEN)
    );

    public static boolean isKeyword(final String literal) {
        return keywords.containsKey(literal);
    }

    public static TokenType resolve(final String literal) {
        return keywords.get(literal);
    }
}
