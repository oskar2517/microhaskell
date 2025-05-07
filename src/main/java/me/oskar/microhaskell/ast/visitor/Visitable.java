package me.oskar.microhaskell.ast.visitor;

public interface Visitable {

    <T> T accept(Visitor<T> visitor);
}
