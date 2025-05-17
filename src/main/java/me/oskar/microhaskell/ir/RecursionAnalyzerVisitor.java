package me.oskar.microhaskell.ir;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.ast.visitor.BaseVisitor;
import me.oskar.microhaskell.table.BindingEntry;
import me.oskar.microhaskell.table.SymbolTable;

import java.util.*;

public class RecursionAnalyzerVisitor extends BaseVisitor<Void> {

    private final Map<Integer, Set<Integer>> applicationGraph;
    private final Map<Integer, BindingEntry> recursiveBindings;

    private final SymbolTable currentTable;
    private final Set<Integer> currentApplications; // nullable, only set during analysis of one function body

    public RecursionAnalyzerVisitor(SymbolTable symbolTable) {
        this(symbolTable, new HashMap<>(), new HashMap<>(), null);
    }

    private RecursionAnalyzerVisitor(SymbolTable symbolTable,
                                     Map<Integer, Set<Integer>> applicationGraph,
                                     Map<Integer, BindingEntry> recursiveBindings,
                                     Set<Integer> currentApplications) {
        this.currentTable = symbolTable;
        this.applicationGraph = applicationGraph;
        this.recursiveBindings = recursiveBindings;
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
        var entry = (BindingEntry) currentTable.lookup(functionDefinitionNode.getName());
        recursiveBindings.put(entry.getDispatchId(), entry);

        var functionApplications = new HashSet<Integer>();
        var localAnalyzer = new RecursionAnalyzerVisitor(entry.getLocalTable(), applicationGraph, recursiveBindings, functionApplications);

        functionDefinitionNode.getBody().accept(localAnalyzer);
        applicationGraph.put(entry.getDispatchId(), functionApplications);

        return null;
    }

    @Override
    public Void visit(LetNode letNode) {
        letNode.getExpression().accept(this);

        for (var b : letNode.getBindings()) {
            var entry = (BindingEntry) currentTable.lookup(b.getName());
            applicationGraph.putIfAbsent(entry.getDispatchId(), new HashSet<>());

            var functionApplications = new HashSet<Integer>();
            var localAnalyzer = new RecursionAnalyzerVisitor(entry.getLocalTable(), applicationGraph, recursiveBindings, functionApplications);

            b.getBody().accept(localAnalyzer);
            applicationGraph.put(entry.getDispatchId(), functionApplications);

            if (functionApplications.contains(entry.getDispatchId())) {
                recursiveBindings.put(entry.getDispatchId(), entry);
            }
        }

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
    public Void visit(IfNode ifNode) {
        ifNode.getCondition().accept(this);
        ifNode.getConsequence().accept(this);
        ifNode.getAlternative().accept(this);

        return null;
    }

    @Override
    public Void visit(IdentifierNode identifierNode) {
        if (currentApplications == null) return null; // Top-level or untracked context

        var entry = currentTable.lookup(identifierNode.getName());
        if (entry instanceof BindingEntry be) {
            currentApplications.add(be.getDispatchId());
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
            if (scc.size() > 1) {
                for (var fn : scc) {
                    var entry = recursiveBindings.get(fn);
                    if (entry != null) {
                        entry.setAppliedMutuallyRecursively(true);
                    }
                }
            } else {
                var fn = scc.iterator().next();
                if (applicationGraph.getOrDefault(fn, Set.of()).contains(fn)) {
                    var entry = recursiveBindings.get(fn);
                    if (entry != null) {
                        entry.setAppliedRecursively(true);
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
