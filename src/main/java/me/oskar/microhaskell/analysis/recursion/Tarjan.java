package me.oskar.microhaskell.analysis.recursion;

import java.util.*;

class Tarjan<T> {

    private int index = 0;
    private final Deque<T> stack = new ArrayDeque<>();
    private final Map<T, Integer> indexMap = new HashMap<>();
    private final Map<T, Integer> lowLinkMap = new HashMap<>();
    private final Set<Set<T>> sccs = new HashSet<>();

    private final Map<T, Set<T>> graph;

    Tarjan(Map<T, Set<T>> graph) {
        this.graph = graph;
    }

    Set<Set<T>> findSCCs() {
        for (var v : graph.keySet()) {
            if (!indexMap.containsKey(v)) {
                strongConnect(v);
            }
        }

        return sccs;
    }

    private void strongConnect(T v) {
        indexMap.put(v, index);
        lowLinkMap.put(v, index);
        index++;
        stack.push(v);

        for (var w : graph.getOrDefault(v, Set.of())) {
            if (!indexMap.containsKey(w)) {
                strongConnect(w);

                var vl = lowLinkMap.get(v);
                var wl = lowLinkMap.get(w);

                lowLinkMap.put(v, Math.min(vl, wl));
            } else if (stack.contains(w)) {
                var vl = lowLinkMap.get(v);
                var wi = indexMap.get(w);

                lowLinkMap.put(v, Math.min(vl, wi));
            }
        }

        if (Objects.equals(lowLinkMap.get(v), indexMap.get(v))) {
            var scc = new HashSet<T>();
            T w;
            do {
                w = stack.pop();
                scc.add(w);
            } while (w != v);
            sccs.add(scc);
        }
    }
}
