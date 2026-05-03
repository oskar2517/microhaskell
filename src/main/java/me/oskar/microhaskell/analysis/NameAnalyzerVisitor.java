package me.oskar.microhaskell.analysis;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.ast.visitor.BaseVisitor;
import me.oskar.microhaskell.error.Error;
import me.oskar.microhaskell.table.BindingEntry;
import me.oskar.microhaskell.table.FixityEntry;
import me.oskar.microhaskell.table.SymbolTable;
import me.oskar.microhaskell.table.VariableEntry;

public class NameAnalyzerVisitor extends BaseVisitor<Void> {

    private final SymbolTable symbolTable;
    private final Error error;

    public NameAnalyzerVisitor(SymbolTable symbolTable, Error error) {
        this.symbolTable = symbolTable;
        this.error = error;
    }

    @Override
    public Void visit(FixityNode fixityNode) {
        var entry = new FixityEntry(fixityNode.getAssociativity(), fixityNode.getPrecedence());

        symbolTable.enterFixity(fixityNode.getOperatorName(), entry, () -> {
            throw error.duplicateFixityDeclaration(fixityNode);
        });

        return null;
    }

    @Override
    public Void visit(FlatExpressionNode flatExpressionNode) {
        for (var e : flatExpressionNode.getElements()) {
            if (!(e instanceof Node n)) continue;

            n.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(BindingNode bindingNode) {
        var localTable = new SymbolTable(symbolTable);
        var localNameAnalyzerVisitor = new NameAnalyzerVisitor(localTable, error);

        for (var p : bindingNode.getParameters()) {
            localTable.enterBinding(((IdentifierNode) p).getName(), new VariableEntry(), () -> {
                throw error.redefinitionAsParameter(p);
            });
        }

        bindingNode.getBody().accept(localNameAnalyzerVisitor);

        var bindingEntry = new BindingEntry(symbolTable, localTable);

        symbolTable.enterBinding(bindingNode.getName(), bindingEntry, () -> {
            throw error.redefinitionAsBinding(bindingNode);
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
        var localTable = new SymbolTable(symbolTable);
        var localNameAnalyzerVisitor = new NameAnalyzerVisitor(localTable, error);

        for (var d : letNode.getDeclarations()) {
            d.accept(localNameAnalyzerVisitor);
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
    public Void visit(LambdaNode lambdaNode) {
        var localTable = new SymbolTable(symbolTable);
        var localNameAnalyzerVisitor = new NameAnalyzerVisitor(localTable, error);

        for (var p : lambdaNode.getParameters()) {
            localTable.enterBinding(((IdentifierNode) p).getName(), new VariableEntry(), () -> {
                throw error.redefinitionAsParameter(p);
            });
        }

        lambdaNode.getBody().accept(localNameAnalyzerVisitor);

        lambdaNode.setLocalTable(localTable);

        return null;
    }

    @Override
    public Void visit(ListLiteralNode listLiteralNode) {
        for (var v : listLiteralNode.getValue()) {
            v.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(ProgramNode programNode) {
        for (var d : programNode.getDeclarations()) {
            d.accept(this);
        }

        return null;
    }
}
