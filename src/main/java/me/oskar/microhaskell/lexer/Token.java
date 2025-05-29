package me.oskar.microhaskell.lexer;

import me.oskar.microhaskell.position.Span;

public record Token(TokenType type, String lexeme, Span span) {
}
