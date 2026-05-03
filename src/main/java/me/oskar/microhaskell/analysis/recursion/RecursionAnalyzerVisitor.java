package me.oskar.microhaskell.analysis.recursion;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.ast.visitor.BaseVisitor;
import me.oskar.microhaskell.table.BindingEntry;
import me.oskar.microhaskell.table.SymbolTable;

import java.util.*;

public class RecursionAnalyzerVisitor extends BaseVisitor<Void> {

    private final SymbolTable symbolTable;
    private final Map<BindingEntry, Set<BindingEntry>> applicationGraph;
    private final Set<BindingEntry> currentApplications;

    public RecursionAnalyzerVisitor(SymbolTable symbolTable) {
        this(symbolTable, new HashMap<>(), null);
    }

    private RecursionAnalyzerVisitor(
            SymbolTable symbolTable,
            Map<BindingEntry, Set<BindingEntry>> applicationGraph,
            Set<BindingEntry> currentApplications) {
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
        var entry = (BindingEntry) symbolTable.lookupBinding(bindingNode.getName());

        var bindingApplications = new HashSet<BindingEntry>();
        var localAnalyzer = new RecursionAnalyzerVisitor(entry.getLocalTable(), applicationGraph, bindingApplications);

        bindingNode.getBody().accept(localAnalyzer);
        if (applicationGraph.put(entry, bindingApplications) != null) {
            throw new IllegalStateException("Duplicate binding in application graph");
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
    public Void visit(LambdaNode lambdaNode) {
        if (lambdaNode.getLocalTable() != symbolTable) {
            var localAnalyzer = new RecursionAnalyzerVisitor(lambdaNode.getLocalTable(), applicationGraph,
                    currentApplications);
            lambdaNode.accept(localAnalyzer);

            return null;
        }

        lambdaNode.getBody().accept(this);

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

        var entry = symbolTable.lookupBinding(identifierNode.getName());
        if (entry instanceof BindingEntry fe) {
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
            for (var binding : scc) {
                if (binding.getOwnerTable() != symbolTable) continue;

                if (scc.size() > 1) {
                    binding.setAppliedMutuallyRecursively(true);
                }
                if (applicationGraph.getOrDefault(binding, Set.of()).contains(binding)) {
                    binding.setAppliedSelfRecursively(true);
                }
            }
        }
    }
}
