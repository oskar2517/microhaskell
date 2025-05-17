package me.oskar.microhaskell.ir;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.ast.visitor.BaseVisitor;
import me.oskar.microhaskell.table.BindingEntry;
import me.oskar.microhaskell.table.SymbolTable;

public class NameAnalyzerVisitor extends BaseVisitor<Void> {

    private final SymbolTable currentTable;

    public NameAnalyzerVisitor(SymbolTable currentTable) {
        this.currentTable = currentTable;
    }

    @Override
    public Void visit(FunctionDefinitionNode functionDefinitionNode) {
        var localTable = new SymbolTable(functionDefinitionNode.getName(), currentTable);
        var localNameAnalyzerVisitor = new NameAnalyzerVisitor(localTable);

        functionDefinitionNode.getBody().accept(localNameAnalyzerVisitor);

        var bindingEntry = new BindingEntry(localTable, functionDefinitionNode.getDispatchId());

        currentTable.enter(functionDefinitionNode.getName(), bindingEntry);

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
        for (var b : letNode.getBindings()) {
            b.accept(this);
        }

        letNode.getExpression().accept(this);

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
        anonymousFunctionNode.getBody().accept(this);

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
