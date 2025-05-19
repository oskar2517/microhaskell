package me.oskar.microhaskell.ir;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.ast.visitor.BaseVisitor;
import me.oskar.microhaskell.table.BindingEntry;
import me.oskar.microhaskell.table.SymbolTable;

public class SemanticAnalyzerVisitor extends BaseVisitor<Void> {

    private final SymbolTable symbolTable;

    public SemanticAnalyzerVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    @Override
    public Void visit(IdentifierNode identifierNode) {
        if (symbolTable.isDefined(identifierNode.getName())) return null;

        throw new RuntimeException("Symbol %s is not defined".formatted(identifierNode.getName()));
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
        var localSemanticAnalyzerVisitor = new SemanticAnalyzerVisitor(anonymousFunctionNode.getLocalTable());

        anonymousFunctionNode.getBody().accept(localSemanticAnalyzerVisitor);

        return null;
    }

    @Override
    public Void visit(FunctionDefinitionNode functionDefinitionNode) {
        var entry = (BindingEntry) symbolTable.lookup(functionDefinitionNode.getName());

        var localSemanticAnalyzerVisitor = new SemanticAnalyzerVisitor(entry.getLocalTable());

        functionDefinitionNode.getBody().accept(localSemanticAnalyzerVisitor);

        return null;
    }

    @Override
    public Void visit(LetNode letNode) {
        var localSemanticAnalyzerVisitor = new SemanticAnalyzerVisitor(letNode.getLocalTable());

        for (var b : letNode.getBindings()) {
            b.accept(localSemanticAnalyzerVisitor);
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
    public Void visit(ProgramNode programNode) {
        for (var b : programNode.getBindings()) {
            b.accept(this);
        }

        return null;
    }
}
