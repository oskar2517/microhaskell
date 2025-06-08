package me.oskar.microhaskell;

import me.oskar.microhaskell.ast.ProgramNode;
import me.oskar.microhaskell.error.Error;
import me.oskar.microhaskell.error.CompileTimeError;
import me.oskar.microhaskell.evaluation.Builtins;
import me.oskar.microhaskell.ir.IrGeneratorVisitor;
import me.oskar.microhaskell.analysis.NameAnalyzerVisitor;
import me.oskar.microhaskell.analysis.RecursionAnalyzerVisitor;
import me.oskar.microhaskell.analysis.SemanticAnalyzerVisitor;
import me.oskar.microhaskell.lexer.Lexer;
import me.oskar.microhaskell.parser.ExpressionRewriterVisitor;
import me.oskar.microhaskell.parser.Parser;
import me.oskar.microhaskell.table.SymbolTable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: mhs <input file>");
            System.exit(1);
        }

        var filename = args[0];
        var code = "";
        try {
            code = Files.readString(Path.of(filename));
        } catch (IOException e) {
            System.err.printf("Error reading file: %s%n", filename);
            System.exit(1);
        }

        var error = new Error(code, filename);

        var lexer = new Lexer(code);

        try {
            var ast = new Parser(lexer, error).parse();

            var globalSymbolTable = new SymbolTable();
            var env = Builtins.initialEnv(globalSymbolTable);

            var nameAnalyzer = new NameAnalyzerVisitor(globalSymbolTable, error);
            ast.accept(nameAnalyzer);

            var astRewriterVisitor = new ExpressionRewriterVisitor(globalSymbolTable);
            ast = (ProgramNode) ast.accept(astRewriterVisitor);

            var semanticAnalyzer = new SemanticAnalyzerVisitor(globalSymbolTable, error);
            ast.accept(semanticAnalyzer);

            var recursionAnalyzer = new RecursionAnalyzerVisitor(globalSymbolTable);
            ast.accept(recursionAnalyzer);

            var irGenerator = new IrGeneratorVisitor(globalSymbolTable, error);
            var ir = ast.accept(irGenerator);

            System.out.println(ir);
            System.out.println(ir.evaluate(env));
        } catch (CompileTimeError e) {
            System.exit(1);
        }
    }
}