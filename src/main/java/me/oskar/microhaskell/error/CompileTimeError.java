package me.oskar.microhaskell.error;

import me.oskar.microhaskell.position.Span;

import java.util.List;

public abstract class CompileTimeError extends RuntimeException {

    protected static final String ANSI_RESET = "\u001B[0m";
    protected static final String ANSI_BOLD = "\u001B[1m";
    protected static final String ANSI_BLACK = "\u001B[30m";
    protected static final String ANSI_RED = "\u001B[31m";
    protected static final String ANSI_GREEN = "\u001B[32m";
    protected static final String ANSI_YELLOW = "\u001B[33m";
    protected static final String ANSI_BLUE = "\u001B[34m";
    protected static final String ANSI_PURPLE = "\u001B[35m";
    protected static final String ANSI_CYAN = "\u001B[36m";
    protected static final String ANSI_WHITE = "\u001B[37m";

    private final List<String> code;
    private final String filename;

    protected CompileTimeError(String code, String filename) {
        this.code = code.lines().toList();
        this.filename = filename;
    }

    protected static String padLeft(String s, int n) {
        return String.format("%" + n + "s", s);
    }

    protected void printUnderline(int offset, int length, String underlineMessage) {
        var s = ANSI_RED + "      " + " ".repeat(offset) + "^".repeat(length) + " " + underlineMessage + ANSI_RESET;

        System.out.println(s);
    }

    protected void printErrorLine(int startOffset, int endOffset, String lineCount, String codeLine) {
        var startPart = codeLine.substring(0, startOffset);
        var errorPart = codeLine.substring(startOffset, endOffset);
        var endPart = codeLine.substring(endOffset);

        var s = startPart + ANSI_RED + errorPart + ANSI_RESET + endPart;

        System.out.printf("   %s | %s%n", lineCount, s);
    }

    protected void printCode(Span span, String underlineMessage) {
        var codePreviewStart = Math.max(span.start().line() - 2, 1);
        var codePreviewEnd = Math.min(span.end().line() + 3, code.size());

        var lineCountWidth = String.valueOf(span.end().line() + 3).length();

        for (var i = codePreviewStart; i <= codePreviewEnd; i++) {
            var lineCount = padLeft(String.valueOf(i), lineCountWidth);
            var codeLine = code.get(i - 1);

            if (span.includesLine(i)) {
                if (span.isMultiline()) {
                    if (i == span.start().line()) {
                        var startOffset = span.start().lineOffset();
                        var endOffset = codeLine.length();
                        printErrorLine(startOffset, endOffset, lineCount, codeLine);
                    } else if (i == span.end().line()) {
                        var startOffset = 0;
                        var endOffset = span.end().lineOffset();
                        printErrorLine(startOffset, endOffset, lineCount, codeLine);
                    } else {
                        printErrorLine(0, codeLine.length(), lineCount, codeLine);
                    }
                } else {
                    printErrorLine(span.start().lineOffset(), span.end().lineOffset(), lineCount, codeLine);
                }
            } else {
                System.out.printf("   %s | %s%n", lineCount, codeLine);
            }

            if (span.isMultiline()) {
                if (i == span.start().line()) {
                    var offset = lineCountWidth + span.start().lineOffset();
                    printUnderline(offset, codeLine.length() - span.start().lineOffset(), "");
                } else if (i == span.end().line()) {
                    printUnderline(lineCountWidth, span.end().lineOffset(), underlineMessage);
                } else if (span.includesLine(i)) {
                    printUnderline(lineCountWidth, codeLine.length(), "");
                }
            } else if (i == span.start().line()) {
                var offset = lineCountWidth + span.start().lineOffset();
                var length = span.end().lineOffset() - span.start().lineOffset();
                printUnderline(offset, length, underlineMessage);
            }
        }
    }

    protected void printErrorHead(Span span, String message) {
        System.out.printf("%s%s:%s%s %serror:%s %s%n", ANSI_BOLD, filename, span.start().line(), ANSI_RESET, ANSI_RED,
                ANSI_RESET, message);
    }

    public abstract void printError();
}
