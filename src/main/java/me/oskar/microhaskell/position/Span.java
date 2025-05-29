package me.oskar.microhaskell.position;

public record Span(Position start, Position end) {

    public static final Span BASE_SPAN = new Span(Position.BASE_POSITION, Position.BASE_POSITION);

    public boolean includesLine(int line) {
        return line >= start.line() && line <= end.line();
    }

    public boolean isMultiline() {
        return start.line() != end.line();
    }

    @Override
    public String toString() {
        return "(start=%s, end=%s)".formatted(start, end);
    }
}
