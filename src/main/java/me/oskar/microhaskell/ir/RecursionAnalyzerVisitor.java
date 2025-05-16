package me.oskar.microhaskell.ir;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.ast.visitor.BaseVisitor;

import java.util.*;

public class RecursionAnalyzerVisitor extends BaseVisitor<Void> {

    private final Map<String, Set<String>> callGraph = new HashMap<>();
    private final Map<String, FunctionDefinitionNode> nameToNode = new HashMap<>();

    private String currentFunctionName = null;
    private Set<String> currentFunctionCalls = new HashSet<>();

    @Override
    public Void visit(ProgramNode programNode) {
        for (var binding : programNode.getBindings()) {
            if (binding instanceof FunctionDefinitionNode fn) {
                nameToNode.put(fn.getName(), fn);
                fn.accept(this);
                callGraph.put(fn.getName(), currentFunctionCalls);
            }
        }

        detectRecursionViaSCC();

        return null;
    }

    @Override
    public Void visit(FunctionDefinitionNode functionDefinitionNode) {
        currentFunctionName = functionDefinitionNode.getName();
        currentFunctionCalls = new HashSet<>();
        functionDefinitionNode.getBody().accept(this);

        return null;
    }

    @Override
    public Void visit(FunctionApplicationNode functionApplicationNode) {
        functionApplicationNode.getFunction().accept(this);
        functionApplicationNode.getArgument().accept(this);

        return null;
    }

    @Override
    public Void visit(IdentifierNode identifierNode) {
        currentFunctionCalls.add(identifierNode.getName());

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
    public Void visit(LetNode letNode) {
        letNode.getExpression().accept(this);

        for (var b : letNode.getBindings()) {
            nameToNode.put(b.getName(), b);
            currentFunctionName = b.getName();
            currentFunctionCalls = new HashSet<>();
            b.accept(this);
            callGraph.put(currentFunctionName, currentFunctionCalls);
        }

        return null;
    }


    private void detectRecursionViaSCC() {
        var indexMap = new HashMap<String, Integer>();
        var lowLinkMap = new HashMap<String, Integer>();
        var stack = new ArrayDeque<String>();
        var onStack = new HashSet<String>();
        var sccs = new ArrayList<Set<String>>();

        var index = new int[]{0};

        for (var function : callGraph.keySet()) {
            if (indexMap.containsKey(function)) continue;

            strongConnect(function, index, indexMap, lowLinkMap, stack, onStack, sccs);
        }

        for (Set<String> scc : sccs) {
            if (scc.size() > 1) {
                for (String fn : scc) {
                    nameToNode.get(fn).setAppliedMutuallyRecursively(true);
                }
            } else {
                var fn = scc.iterator().next();
                if (callGraph.getOrDefault(fn, Set.of()).contains(fn)) {
                    nameToNode.get(fn).setAppliedRecursively(true);
                }
            }
        }
    }

    private void strongConnect(
            String function,
            int[] index,
            Map<String, Integer> indexMap,
            Map<String, Integer> lowLinkMap,
            Deque<String> stack,
            Set<String> onStack,
            List<Set<String>> sccs) {

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
            var scc = new HashSet<String>();
            String fn;
            do {
                fn = stack.pop();
                onStack.remove(fn);
                scc.add(fn);
            } while (!fn.equals(function));
            sccs.add(scc);
        }
    }
}
