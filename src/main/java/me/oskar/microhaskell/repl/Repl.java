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

    private static final String COMMAND_PREFIX = ":";

    private SymbolTable symbolTable;
    private Map<String, Expression> env;
    private ProgramNode program;

    public void start() {
        printPrefixedLine("Welcome to the Micro Haskell REPL");
        printPrefixedLine("Type :help for more information");
        System.out.println();

        initialize();

        while (true) {
            var line = read();
            if (processCommand(line)) continue;
            var result = evaluate(line);
            print(result);
        }
    }

    private void initialize() {
        symbolTable = new SymbolTable();
        env = Builtins.initialEnv(symbolTable);
        program = Prelude.readPrelude(symbolTable);
    }

    private void printPrefixedLine(String s) {
        System.out.print("|  ");
        System.out.println(s);
    }

    private String read() {
        System.out.print("mhs> ");
        var scanner = new Scanner(System.in);

        var line = scanner.nextLine();

        if (!line.startsWith(COMMAND_PREFIX)) {
            var highlighter = new Highlighter(line);
            highlighter.highlight();
        }

        return line;
    }

    private boolean processCommand(String line) {
        if (!line.startsWith(COMMAND_PREFIX)) return false;

        var command = line.substring(1).split(" ");

        if (command.length == 0) return false;

        return switch (command[0]) {
            case "reset" -> {
                initialize();
                printPrefixedLine("Reset REPL state");

                yield true;
            }
            case "help" -> {
                printPrefixedLine("Micro Haskell REPL - Available commands:");
                printPrefixedLine("  :reset   Resets the REPL state");
                printPrefixedLine("  :help    Shows this help message");
                printPrefixedLine("  :exit    Exits the REPL");

                yield true;
            }
            case "exit" -> {
                printPrefixedLine("Goodbye!");
                System.exit(0);

                yield true;
            }
            default -> false;
        };
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

        printPrefixedLine(expr.toString());
    }
}
