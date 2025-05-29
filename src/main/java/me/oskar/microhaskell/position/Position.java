package me.oskar.microhaskell.position;

public record Position(int line, int lineOffset) {

    public static final Position BASE_POSITION = new Position(1, 0);

    @Override
    public String toString() {
        return "(%s, %s)".formatted(line, lineOffset);
    }
}
