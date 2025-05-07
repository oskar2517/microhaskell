package me.oskar.microhaskell;

import me.oskar.microhaskell.evaluation.Builtins;
import me.oskar.microhaskell.evaluation.expression.*;
import me.oskar.microhaskell.ir.IRGeneratorVisitor;
import me.oskar.microhaskell.ir.RecursionAnalyzerVisitor;
import me.oskar.microhaskell.lexer.Lexer;
import me.oskar.microhaskell.parser.Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: mhs <input file>");
            System.exit(1);
        }

        var code = "";
        try {
            code = Files.readString(Path.of(args[0]));
        } catch (IOException e) {
            System.err.printf("Error reading file: %s%n", args[0]);
            System.exit(1);
        }

        var lexer = new Lexer(code);
        var ast = new Parser(lexer).parse();

        var recursionAnalyzer = new RecursionAnalyzerVisitor();
        ast.accept(recursionAnalyzer);

        for (var d : ast.getDefinitions()) {
            System.out.println(d.getName() + " " + d.isAppliedRecursively());
        }

        var irGenerator  = new IRGeneratorVisitor();
        var ir = ast.accept(irGenerator);

        System.out.println(ir);

        System.out.println(ir.evaluate(Builtins.initialEnv()));
    }
}