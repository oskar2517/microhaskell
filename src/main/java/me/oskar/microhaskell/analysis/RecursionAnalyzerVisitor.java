package me.oskar.microhaskell.analysis;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.ast.visitor.BaseVisitor;
import me.oskar.microhaskell.table.FunctionEntry;
import me.oskar.microhaskell.table.SymbolTable;

import java.util.*;

public class RecursionAnalyzerVisitor extends BaseVisitor<Void> {

    private final Map<Integer, Set<Integer>> applicationGraph;

    private final SymbolTable symbolTable;
    private final Set<Integer> currentApplications;

    public RecursionAnalyzerVisitor(SymbolTable symbolTable) {
        this(symbolTable, new HashMap<>(), null);
    }

    private RecursionAnalyzerVisitor(SymbolTable symbolTable,
                                     Map<Integer, Set<Integer>> applicationGraph,
                                     Set<Integer> currentApplications) {
        this.symbolTable = symbolTable;
        this.applicationGraph = applicationGraph;
        this.currentApplications = currentApplications;
    }

    @Override
    public Void visit(ProgramNode programNode) {
        for (var b : programNode.getBindings()) {
            b.accept(this);
        }

        detectRecursionViaSCC();

        return null;
    }

    @Override
    public Void visit(FunctionDefinitionNode functionDefinitionNode) {
        var entry = (FunctionEntry) symbolTable.lookup(functionDefinitionNode.getName());

        var functionApplications = new HashSet<Integer>();
        var localAnalyzer = new RecursionAnalyzerVisitor(entry.getLocalTable(), applicationGraph, functionApplications);

        functionDefinitionNode.getBody().accept(localAnalyzer);
        applicationGraph.put(entry.getDispatchId(), functionApplications);

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

        for (var b : letNode.getBindings()) {
            if (!(b instanceof FunctionDefinitionNode fd)) continue;

            var entry = (FunctionEntry) letNode.getLocalTable().lookup(fd.getName());
            applicationGraph.putIfAbsent(entry.getDispatchId(), new HashSet<>());
        }

        for (var b : letNode.getBindings()) {
            b.accept(this);
        }

        detectRecursionViaSCC();

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

        var entry = symbolTable.lookup(identifierNode.getName());
        if (entry instanceof FunctionEntry fe) {
            currentApplications.add(fe.getDispatchId());
        }

        return null;
    }


    private void detectRecursionViaSCC() {
        var indexMap = new HashMap<Integer, Integer>();
        var lowLinkMap = new HashMap<Integer, Integer>();
        var stack = new ArrayDeque<Integer>();
        var onStack = new HashSet<Integer>();
        var sccs = new ArrayList<Set<Integer>>();

        var index = new int[]{0};

        for (var function : applicationGraph.keySet()) {
            if (!indexMap.containsKey(function)) {
                strongConnect(function, index, indexMap, lowLinkMap, stack, onStack, sccs);
            }
        }

        for (var scc : sccs) {
            for (var fn : scc) {
                var entry = symbolTable.lookupFunctionByDispatchId(fn);
                if (entry != null) {
                    if (scc.size() > 1) {
                        entry.setAppliedMutuallyRecursively(true);
                    }
                    if (applicationGraph.getOrDefault(fn, Set.of()).contains(fn)) {
                        entry.setAppliedSelfRecursively(true);
                    }
                }
            }
        }
    }


    private void strongConnect(
            Integer function,
            int[] index,
            Map<Integer, Integer> indexMap,
            Map<Integer, Integer> lowLinkMap,
            Deque<Integer> stack,
            Set<Integer> onStack,
            List<Set<Integer>> sccs) {

        indexMap.put(function, index[0]);
        lowLinkMap.put(function, index[0]);
        index[0]++;
        stack.push(function);
        onStack.add(function);

        for (var target : applicationGraph.getOrDefault(function, Set.of())) {
            if (!indexMap.containsKey(target)) {
                strongConnect(target, index, indexMap, lowLinkMap, stack, onStack, sccs);
                lowLinkMap.put(function, Math.min(lowLinkMap.get(function), lowLinkMap.get(target)));
            } else if (onStack.contains(target)) {
                lowLinkMap.put(function, Math.min(lowLinkMap.get(function), indexMap.get(target)));
            }
        }

        if (lowLinkMap.get(function).equals(indexMap.get(function))) {
            var scc = new HashSet<Integer>();
            int fn;
            do {
                fn = stack.pop();
                onStack.remove(fn);
                scc.add(fn);
            } while (fn != function);
            sccs.add(scc);
        }
    }
}
