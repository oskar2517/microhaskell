package me.oskar.microhaskell.analysis.recursion;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.ast.visitor.BaseVisitor;
import me.oskar.microhaskell.table.FunctionEntry;
import me.oskar.microhaskell.table.SymbolTable;

import java.util.*;

public class RecursionAnalyzerVisitor extends BaseVisitor<Void> {

    private final SymbolTable symbolTable;
    private final Map<FunctionEntry, Set<FunctionEntry>> applicationGraph;
    private final Set<FunctionEntry> currentApplications;

    public RecursionAnalyzerVisitor(SymbolTable symbolTable) {
        this(symbolTable, new HashMap<>(), null);
    }

    private RecursionAnalyzerVisitor(
            SymbolTable symbolTable,
            Map<FunctionEntry, Set<FunctionEntry>> applicationGraph,
            Set<FunctionEntry> currentApplications) {
        this.symbolTable = symbolTable;
        this.applicationGraph = applicationGraph;
        this.currentApplications = currentApplications;
    }

    @Override
    public Void visit(ProgramNode programNode) {
        for (var d : programNode.getDeclarations()) {
            d.accept(this);
        }

        detectRecursionInCurrentScope();

        return null;
    }

    @Override
    public Void visit(BindingNode bindingNode) {
        var entry = (FunctionEntry) symbolTable.lookupFunction(bindingNode.getName());

        var functionApplications = new HashSet<FunctionEntry>();
        var localAnalyzer = new RecursionAnalyzerVisitor(entry.getLocalTable(), applicationGraph, functionApplications);

        bindingNode.getBody().accept(localAnalyzer);
        if (applicationGraph.put(entry, functionApplications) != null) {
            throw new IllegalStateException("Duplicated function in application graph");
        }

        return null;
    }

    @Override
    public Void visit(LetNode letNode) {
        if (letNode.getLocalTable() != symbolTable) {
            var localAnalyzer = new RecursionAnalyzerVisitor(letNode.getLocalTable(),
                    applicationGraph, currentApplications);
            letNode.accept(localAnalyzer);

            return null;
        }

        letNode.getExpression().accept(this);

        for (var d : letNode.getDeclarations()) {
            d.accept(this);
        }

        detectRecursionInCurrentScope();

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
        if (anonymousFunctionNode.getLocalTable() != symbolTable) {
            var localAnalyzer = new RecursionAnalyzerVisitor(anonymousFunctionNode.getLocalTable(), applicationGraph,
                    currentApplications);
            anonymousFunctionNode.accept(localAnalyzer);

            return null;
        }

        anonymousFunctionNode.getBody().accept(this);

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
    public Void visit(IdentifierNode identifierNode) {
        if (currentApplications == null) return null; // Top-level or untracked context

        var entry = symbolTable.lookupFunction(identifierNode.getName());
        if (entry instanceof FunctionEntry fe) {
            currentApplications.add(fe);
        }

        return null;
    }

    @Override
    public Void visit(ListLiteralNode listLiteralNode) {
        for (var v : listLiteralNode.getValue()) {
            v.accept(this);
        }

        return null;
    }

    private void detectRecursionInCurrentScope() {
        var tarjan = new Tarjan<>(applicationGraph);
        var sccs = tarjan.findSCCs();

        for (var scc : sccs) {
            for (var fn : scc) {
                if (fn.getOwnerTable() != symbolTable) continue;

                if (scc.size() > 1) {
                    fn.setAppliedMutuallyRecursively(true);
                }
                if (applicationGraph.getOrDefault(fn, Set.of()).contains(fn)) {
                    fn.setAppliedSelfRecursively(true);
                }
            }
        }
    }
}
