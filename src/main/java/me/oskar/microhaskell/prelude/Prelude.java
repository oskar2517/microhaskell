package me.oskar.microhaskell.prelude;

import me.oskar.microhaskell.ast.ProgramNode;
import me.oskar.microhaskell.error.Error;
import me.oskar.microhaskell.lexer.Lexer;
import me.oskar.microhaskell.parser.Parser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Prelude {

    private static String readResourceAsString(String resourceName) {
        try (var inputStream = Prelude.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourceName);
            }
            try (var scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
                scanner.useDelimiter("\\A");
                return scanner.hasNext() ? scanner.next() : "";
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading resource", e);
        }
    }

    public static ProgramNode readPrelude() {
        var prelude = readResourceAsString("prelude.mhs");

        var error = new Error(prelude, "prelude.mhs");
        var lexer = new Lexer(prelude);
        var parser = new Parser(lexer, error);

        return parser.parse();
    }
}
