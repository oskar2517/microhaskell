package me.oskar.microhaskell.ir;

import me.oskar.microhaskell.ast.*;
import me.oskar.microhaskell.ast.visitor.Visitor;
import me.oskar.microhaskell.evaluation.Builtins;

import java.util.*;
import java.util.stream.Collectors;

public class RecursionAnalyzerVisitor implements Visitor<Void> {

    private final Map<String, Set<String>> callGraph = new HashMap<>();
    private final Map<String, FunctionDefinitionNode> definitions = new HashMap<>();
    private final Deque<Set<String>> boundVariables = new ArrayDeque<>();

    private String currentFunction = null;

    @Override
    public Void visit(FunctionDefinitionNode functionDefinitionNode) {
        currentFunction = functionDefinitionNode.getName();
        callGraph.putIfAbsent(currentFunction, new HashSet<>());
        definitions.put(currentFunction, functionDefinitionNode);

        Set<String> params = functionDefinitionNode.getParameters().stream()
                .map(p -> ((IdentifierNode) p).getName())
                .collect(Collectors.toSet());
        boundVariables.push(params);

        functionDefinitionNode.getBody().accept(this);

        boundVariables.pop();
        currentFunction = null;
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
        if (currentFunction != null) {
            String name = identifierNode.getName();

            for (Set<String> scope : boundVariables) {
                if (scope.contains(name)) return null;
            }

            callGraph.computeIfAbsent(currentFunction, k -> new HashSet<>()).add(name);
            callGraph.putIfAbsent(name, new HashSet<>());
        }
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
    public Void visit(IntLiteralNode intLiteralNode) {
        return null;
    }

    @Override
    public Void visit(ProgramNode programNode) {
        Builtins.initialEnv().keySet().forEach(name -> callGraph.put(name, new HashSet<>()));

        for (var def : programNode.getDefinitions()) {
            def.accept(this);
        }

        Set<Set<String>> sccs = findSCCs();

        for (Set<String> scc : sccs) {
            if (scc.size() > 1 || scc.stream().anyMatch(name -> callGraph.get(name).contains(name))) {
                for (String recursiveName : scc) {
                    FunctionDefinitionNode def = definitions.get(recursiveName);
                    if (def != null) {
                        def.setAppliedRecursively();
                    }
                }
            }
        }

        return null;
    }

    private Set<Set<String>> findSCCs() {
        Map<String, Integer> indexMap = new HashMap<>();
        Map<String, Integer> lowLinkMap = new HashMap<>();
        Deque<String> stack = new ArrayDeque<>();
        Set<String> onStack = new HashSet<>();
        List<Set<String>> result = new ArrayList<>();
        int[] index = {0};

        for (String node : callGraph.keySet()) {
            if (!indexMap.containsKey(node)) {
                strongConnect(node, index, indexMap, lowLinkMap, stack, onStack, result);
            }
        }

        return new HashSet<>(result);
    }

    private void strongConnect(String node,
                               int[] index,
                               Map<String, Integer> indexMap,
                               Map<String, Integer> lowLinkMap,
                               Deque<String> stack,
                               Set<String> onStack,
                               List<Set<String>> result) {
        indexMap.put(node, index[0]);
        lowLinkMap.put(node, index[0]);
        index[0]++;
        stack.push(node);
        onStack.add(node);

        for (String neighbor : callGraph.getOrDefault(node, Set.of())) {
            if (!indexMap.containsKey(neighbor)) {
                strongConnect(neighbor, index, indexMap, lowLinkMap, stack, onStack, result);
                lowLinkMap.put(node, Math.min(lowLinkMap.get(node), lowLinkMap.get(neighbor)));
            } else if (onStack.contains(neighbor)) {
                lowLinkMap.put(node, Math.min(lowLinkMap.get(node), indexMap.get(neighbor)));
            }
        }

        if (lowLinkMap.get(node).equals(indexMap.get(node))) {
            Set<String> scc = new HashSet<>();
            String w;
            do {
                w = stack.pop();
                onStack.remove(w);
                scc.add(w);
            } while (!w.equals(node));
            result.add(scc);
        }
    }
}