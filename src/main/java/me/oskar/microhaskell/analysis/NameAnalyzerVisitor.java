package me.oskar.microhaskell.analysis;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.ast.visitor.BaseVisitor;
import me.oskar.microhaskell.error.CompileTimeError;
import me.oskar.microhaskell.error.Error;
import me.oskar.microhaskell.table.BindingEntry;
import me.oskar.microhaskell.table.SymbolTable;
import me.oskar.microhaskell.table.VariableEntry;

public class NameAnalyzerVisitor extends BaseVisitor<Void> {

    private final SymbolTable currentTable;
    private final Error error;

    public NameAnalyzerVisitor(SymbolTable currentTable, Error error) {
        this.currentTable = currentTable;
        this.error = error;
    }

    @Override
    public Void visit(FunctionDefinitionNode functionDefinitionNode) {
        var localTable = new SymbolTable(currentTable);
        var localNameAnalyzerVisitor = new NameAnalyzerVisitor(localTable, error);

        for (var p : functionDefinitionNode.getParameters()) {
            localTable.enter(((IdentifierNode) p).getName(), new VariableEntry(), () -> {
                error.redefinitionAsParameter(p);
                throw new CompileTimeError();
            });
        }

        functionDefinitionNode.getBody().accept(localNameAnalyzerVisitor);

        var bindingEntry = new BindingEntry(localTable, functionDefinitionNode.getDispatchId());

        currentTable.enter(functionDefinitionNode.getName(), bindingEntry, () -> {
            error.redefinitionAsFunction(functionDefinitionNode);
            throw new CompileTimeError();
        });

        return null;
    }

    @Override
    public Void visit(IfNode ifNode) {
        ifNode.getCondition().accept(this);
        ifNode.getConsequence().accept(this);
        ifNode.getAlternative().accept(this);

        return null;
    }

    @Override
    public Void visit(LetNode letNode) {
        var localTable = new SymbolTable(currentTable);
        var localNameAnalyzerVisitor = new NameAnalyzerVisitor(localTable, error);

        for (var b : letNode.getBindings()) {
            b.accept(localNameAnalyzerVisitor);
        }

        letNode.getExpression().accept(localNameAnalyzerVisitor);

        letNode.setLocalTable(localTable);

        return null;
    }

    @Override
    public Void visit(FunctionApplicationNode functionApplicationNode) {
        functionApplicationNode.getFunction().accept(this);
        functionApplicationNode.getArgument().accept(this);

        return null;
    }

    @Override
    public Void visit(AnonymousFunctionNode anonymousFunctionNode) {
        var localTable = new SymbolTable(currentTable);
        var localNameAnalyzerVisitor = new NameAnalyzerVisitor(localTable, error);

        for (var p : anonymousFunctionNode.getParameters()) {
            localTable.enter(((IdentifierNode) p).getName(), new VariableEntry(), () -> {
                error.redefinitionAsParameter(p);

                throw new CompileTimeError();
            });
        }

        anonymousFunctionNode.getBody().accept(localNameAnalyzerVisitor);

        anonymousFunctionNode.setLocalTable(localTable);

        return null;
    }

    @Override
    public Void visit(ProgramNode programNode) {
        for (var b : programNode.getBindings()) {
            b.accept(this);
        }

        return null;
    }
}
