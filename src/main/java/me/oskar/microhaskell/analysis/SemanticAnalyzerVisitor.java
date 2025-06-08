package me.oskar.microhaskell.analysis;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.ast.visitor.BaseVisitor;
import me.oskar.microhaskell.error.CompileTimeError;
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
        symbolTable.lookup(fixityNode.getOperatorName(), () -> {
            error.fixitySignatureLacksBinding(fixityNode);
            throw new CompileTimeError();
        });

        return null;
    }

    @Override
    public Void visit(IdentifierNode identifierNode) {
        if (symbolTable.isDefined(identifierNode.getName())) return null;

        error.useOfUndefinedSymbol(identifierNode);
        throw new CompileTimeError();
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
    public Void visit(FunctionDefinitionNode functionDefinitionNode) {
        var entry = (FunctionEntry) symbolTable.lookup(functionDefinitionNode.getName());

        var localSemanticAnalyzerVisitor = new SemanticAnalyzerVisitor(entry.getLocalTable(), error);

        functionDefinitionNode.getBody().accept(localSemanticAnalyzerVisitor);

        return null;
    }

    @Override
    public Void visit(LetNode letNode) {
        var localSemanticAnalyzerVisitor = new SemanticAnalyzerVisitor(letNode.getLocalTable(), error);

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
