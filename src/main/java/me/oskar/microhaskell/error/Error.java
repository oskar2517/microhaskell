package me.oskar.microhaskell.error;

import me.oskar.microhaskell.ast.AtomicExpressionNode;
import me.oskar.microhaskell.ast.FixityNode;
import me.oskar.microhaskell.ast.FunctionDefinitionNode;
import me.oskar.microhaskell.ast.IdentifierNode;
import me.oskar.microhaskell.lexer.Token;
import me.oskar.microhaskell.lexer.TokenType;
import me.oskar.microhaskell.position.Span;

import java.util.List;

public class Error {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BOLD = "\u001B[1m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";

    private final List<String> code;
    private final String filename;

    public Error(String code, String filename) {
        this.code = code.lines().toList();
        this.filename = filename;
    }

    private static String padLeft(String s, int n) {
        return String.format("%" + n + "s", s);
    }


    private void printUnderline(int offset, int length, String underlineMessage) {
        var s = ANSI_RED + "      " + " ".repeat(offset) + "^".repeat(length) + " " + underlineMessage + ANSI_RESET;

        System.out.println(s);
    }

    private void printCode(Span span, String underlineMessage) {
        var codePreviewStart = Math.max(span.start().line() - 2, 1);
        var codePreviewEnd = Math.min(span.end().line() + 3, code.size());

        var lineCountWidth = String.valueOf(span.end().line() + 3).length();

        for (var i = codePreviewStart; i <= codePreviewEnd; i++) {
            var lineCount = padLeft(String.valueOf(i), lineCountWidth);
            var codeLine = code.get(i - 1);

            System.out.printf("   %s | %s%n", lineCount, codeLine);

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

    private void printErrorHead(Span span, String message) {
        System.out.printf("%s%s:%s%s %serror:%s %s%n", ANSI_BOLD, filename, span.start().line(), ANSI_RESET, ANSI_RED,
                ANSI_RESET, message);
    }

    public void unexpectedToken(Token token, String expected) {
        if (token.type() == TokenType.EOF) {
            printErrorHead(token.span(), "unexpected end of file");
        } else {
            printErrorHead(token.span(), "unexpected token");
            printCode(token.span(), String.format("found `%s`, expected %s", token.type().tokenName, expected));
        }
    }

    public void invalidFunctionName(Token token) {
        printErrorHead(token.span(), "invalid function name");
        printCode(token.span(), "expected identifier or operator in parenthesis");
    }

    public void invalidOperatorPrecedence(Token token) {
        printErrorHead(token.span(), "invalid operator precedence");
        printCode(token.span(), "has to be an integer between 0 and 9");
    }

    public void fixitySignatureLacksBinding(FixityNode fixityNode) {
        printErrorHead(fixityNode.getSpan(), "fixity signature lacks an accompanying binding");
        printCode(fixityNode.getSpan(), "`%s` is not bound`".formatted(fixityNode.getOperatorName()));
    }

    public void useOfUndefinedSymbol(IdentifierNode identifierNode) {
        printErrorHead(identifierNode.getSpan(), "use of undefined symbol");
        printCode(identifierNode.getSpan(), "is undefined");
    }

    public void redefinitionAsParameter(AtomicExpressionNode atomicExpressionNode) {
        printErrorHead(atomicExpressionNode.getSpan(), "redefinition of symbol as parameter");
        printCode(atomicExpressionNode.getSpan(), "is already defined on this scope");
    }

    public void redefinitionAsFunction(FunctionDefinitionNode identifierNode) {
        printErrorHead(identifierNode.getSpan(), "redefinition of symbol as function");
        printCode(identifierNode.getSpan(), "is already defined on this scope");
    }

    public void duplicatedFixityDeclaration(FixityNode fixityNode) {
        printErrorHead(fixityNode.getSpan(), "duplicated fixity declaration");
        printCode(fixityNode.getSpan(),
                "fixity for operator `%s` has already been declared".formatted(fixityNode.getOperatorName()));
    }

    public void mainFunctionMissing() {
        printErrorHead(Span.BASE_SPAN, "main function missing");
    }
}
