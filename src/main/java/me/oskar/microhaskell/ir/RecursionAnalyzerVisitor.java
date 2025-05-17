package me.oskar.microhaskell.ir;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.ast.visitor.BaseVisitor;
import me.oskar.microhaskell.table.BindingEntry;
import me.oskar.microhaskell.table.SymbolTable;

import java.util.*;

public class RecursionAnalyzerVisitor extends BaseVisitor<Void> {

    private final Map<Integer, Set<Integer>> callGraph;
    private final Map<Integer, FunctionDefinitionNode> nameToNode;
    private final Map<Integer, BindingEntry> seenEntries;

    private final Integer currentFunctionName;
    private final SymbolTable currentTable;

    public RecursionAnalyzerVisitor(SymbolTable symbolTable) {
        this(symbolTable, null, new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    private RecursionAnalyzerVisitor(SymbolTable symbolTable, Integer currentFunctionName,
                                     Map<Integer, Set<Integer>> callGraph,
                                     Map<Integer, FunctionDefinitionNode> nameToNode,
                                     Map<Integer, BindingEntry> seenEntries) {
        this.currentTable = symbolTable;
        this.currentFunctionName = currentFunctionName;
        this.callGraph = callGraph;
        this.nameToNode = nameToNode;
        this.seenEntries = seenEntries;
    }

    @Override
    public Void visit(ProgramNode programNode) {
        for (var binding : programNode.getBindings()) {
            if (binding instanceof FunctionDefinitionNode fn) {
                var entry = (BindingEntry) currentTable.lookup(fn.getName());
                nameToNode.put(entry.getDispatchId(), fn);
                fn.accept(this);
            }
        }

        detectRecursionViaSCC();
        return null;
    }

    @Override
    public Void visit(FunctionDefinitionNode functionDefinitionNode) {
        var entry = (BindingEntry) currentTable.lookup(functionDefinitionNode.getName());

        Set<Integer> functionCalls = new HashSet<>();

        seenEntries.put(entry.getDispatchId(), entry);

        var localVisitor = new RecursionAnalyzerVisitor(entry.getLocalTable(), entry.getDispatchId(), callGraph, nameToNode, seenEntries) {
            @Override
            public Void visit(IdentifierNode identifierNode) {
                var idEntry = (BindingEntry) currentTable.lookup(identifierNode.getName());
                if (idEntry != null) {
                    functionCalls.add(idEntry.getDispatchId());
                }
                return null;
            }
        };

        functionDefinitionNode.getBody().accept(localVisitor);
        callGraph.put(entry.getDispatchId(), functionCalls);

        return null;
    }

    @Override
    public Void visit(LetNode letNode) {
        letNode.getExpression().accept(this);

        for (var b : letNode.getBindings()) {
            var entry = (BindingEntry) currentTable.lookup(b.getName());
            nameToNode.put(b.getDispatchId(), b);
            callGraph.putIfAbsent(entry.getDispatchId(), new HashSet<>());
        }

        for (var b : letNode.getBindings()) {
            var entry = (BindingEntry) currentTable.lookup(b.getName());

            if (entry == null) {
                continue;
            }

            seenEntries.put(b.getDispatchId(), entry);

            Set<Integer> calls = new HashSet<>();

            var localVisitor = new RecursionAnalyzerVisitor(entry.getLocalTable(), entry.getDispatchId(), callGraph, nameToNode, seenEntries) {
                @Override
                public Void visit(IdentifierNode identifierNode) {
                    var idEntry = (BindingEntry) currentTable.lookup(identifierNode.getName());
                    if (idEntry != null) {
                        calls.add(idEntry.getDispatchId());
                    }
                    return null;
                }
            };

            b.getBody().accept(localVisitor);
            callGraph.put(entry.getDispatchId(), calls);
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

    private void detectRecursionViaSCC() {
        var indexMap = new HashMap<Integer, Integer>();
        var lowLinkMap = new HashMap<Integer, Integer>();
        var stack = new ArrayDeque<Integer>();
        var onStack = new HashSet<Integer>();
        var sccs = new ArrayList<Set<Integer>>();

        var index = new int[]{0};

        for (var function : callGraph.keySet()) {
            if (!indexMap.containsKey(function)) {
                strongConnect(function, index, indexMap, lowLinkMap, stack, onStack, sccs);
            }
        }

        for (var scc : sccs) {
            if (scc.size() > 1) {
                for (var fn : scc) {
                    var entry = seenEntries.get(fn);
                    entry.setAppliedMutuallyRecursively(true);
                }
            } else {
                var fn = scc.iterator().next();
                if (callGraph.getOrDefault(fn, Set.of()).contains(fn)) {
                    var entry = seenEntries.get(fn);
                    entry.setAppliedRecursively(true);
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

        for (var callee : callGraph.getOrDefault(function, Set.of())) {
            if (!indexMap.containsKey(callee)) {
                strongConnect(callee, index, indexMap, lowLinkMap, stack, onStack, sccs);
                lowLinkMap.put(function, Math.min(lowLinkMap.get(function), lowLinkMap.get(callee)));
            } else if (onStack.contains(callee)) {
                lowLinkMap.put(function, Math.min(lowLinkMap.get(function), indexMap.get(callee)));
            }
        }

        if (lowLinkMap.get(function).equals(indexMap.get(function))) {
            var scc = new HashSet<Integer>();
            Integer fn;
            do {
                fn = stack.pop();
                onStack.remove(fn);
                scc.add(fn);
            } while (!fn.equals(function));
            sccs.add(scc);
        }
    }
}
