package me.oskar.microhaskell.repl;

import me.oskar.microhaskell.Main;
import me.oskar.microhaskell.ast.FunctionDefinitionNode;
import me.oskar.microhaskell.ast.ProgramNode;
import me.oskar.microhaskell.error.CompileTimeError;
import me.oskar.microhaskell.error.Error;
import me.oskar.microhaskell.error.MainFunctionMissingError;
import me.oskar.microhaskell.evaluation.Builtins;
import me.oskar.microhaskell.evaluation.expression.Expression;
import me.oskar.microhaskell.ir.IrGeneratorVisitor;
import me.oskar.microhaskell.lexer.Lexer;
import me.oskar.microhaskell.prelude.Prelude;
import me.oskar.microhaskell.table.SymbolTable;

import java.util.Map;
import java.util.Scanner;

public class Repl {

    private final SymbolTable symbolTable = new SymbolTable();
    private final Map<String, Expression> env = Builtins.initialEnv(symbolTable);
    private ProgramNode program = Prelude.readPrelude(symbolTable);

    public void start() {
        System.out.println("Welcome to the Micro Haskell REPL!");

        while (true) {
            var code = read();
            var result = evaluate(code);
            print(result);
        }
    }

    private String read() {
        System.out.print("> ");
        var scanner = new Scanner(System.in);

        return scanner.nextLine();
    }

    private Expression evaluate(String code) {
        var error = new Error(code, "repl");
        var lexer = new Lexer(code);

        try {
            var ast = Main.process(symbolTable, error, lexer);
            program = program.merge(ast);

            var irGenerator = new IrGeneratorVisitor(symbolTable, error);
            var ir = program.accept(irGenerator);

            return ir.evaluate(env);
        } catch (MainFunctionMissingError e) {
            return null;
        } catch (CompileTimeError e) {
            e.printError();
            return null;
        } finally {
            var mainFunction = program.getBindings().stream().filter(b -> {
                if (b instanceof FunctionDefinitionNode fd) {
                    return fd.getName().equals("main");
                }

                return false;
            }).findFirst();
            mainFunction.ifPresent(node -> program.getBindings().remove(node));
            symbolTable.remove("main");
        }
    }

    private void print(Expression expr) {
        if (expr == null) return;

        System.out.println(expr);
    }
}
