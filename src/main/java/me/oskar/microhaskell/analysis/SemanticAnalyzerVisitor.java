package me.oskar.microhaskell.analysis;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.ast.visitor.BaseVisitor;
import me.oskar.microhaskell.error.Error;
import me.oskar.microhaskell.table.FunctionEntry;
import me.oskar.microhaskell.table.SymbolTable;

public class SemanticAnalyzerVisitor extends BaseVisitor<Void> {

    private final SymbolTable symbolTable;
    private final Error error;

    public SemanticAnalyzerVisitor(SymbolTable symbolTable, Error error) {
        this.symbolTable = symbolTable;
        this.error = error;
    }

    @Override
    public Void visit(FixityNode fixityNode) {
        symbolTable.lookupFunction(fixityNode.getOperatorName(), () -> {
            throw error.fixitySignatureLacksBinding(fixityNode);
        });

        return null;
    }

    @Override
    public Void visit(IdentifierNode identifierNode) {
        if (symbolTable.isFunctionDefined(identifierNode.getName())) return null;

        throw error.useOfUndefinedSymbol(identifierNode);
    }

    @Override
    public Void visit(IfNode ifNode) {
        ifNode.getCondition().accept(this);
        ifNode.getConsequence().accept(this);
        ifNode.getAlternative().accept(this);

        return null;
    }

    @Override
    public Void visit(AnonymousFunctionNode anonymousFunctionNode) {
        var localSemanticAnalyzerVisitor = new SemanticAnalyzerVisitor(anonymousFunctionNode.getLocalTable(), error);

        anonymousFunctionNode.getBody().accept(localSemanticAnalyzerVisitor);

        return null;
    }

    @Override
    public Void visit(BindingNode bindingNode) {
        var entry = (FunctionEntry) symbolTable.lookupFunction(bindingNode.getName());

        var localSemanticAnalyzerVisitor = new SemanticAnalyzerVisitor(entry.getLocalTable(), error);

        bindingNode.getBody().accept(localSemanticAnalyzerVisitor);

        return null;
    }

    @Override
    public Void visit(LetNode letNode) {
        var localSemanticAnalyzerVisitor = new SemanticAnalyzerVisitor(letNode.getLocalTable(), error);

        for (var d : letNode.getDeclarations()) {
            d.accept(localSemanticAnalyzerVisitor);
        }

        letNode.getExpression().accept(localSemanticAnalyzerVisitor);

        return null;
    }

    @Override
    public Void visit(FunctionApplicationNode functionApplicationNode) {
        functionApplicationNode.getFunction().accept(this);
        functionApplicationNode.getArgument().accept(this);

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
