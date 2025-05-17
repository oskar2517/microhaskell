package me.oskar.microhaskell.table;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    private final String name;
    private final SymbolTable parent;
    private final Map<String, Entry> symbols = new HashMap<>();
    private final Map<Integer, BindingEntry> bindings = new HashMap<>();

    public SymbolTable(String name, SymbolTable parent) {
        this.name = name;
        this.parent = parent;
    }

    public SymbolTable(String name) {
        this(name, null);
    }

    public void enter(String name, Entry entry) {
        if (symbols.containsKey(name)) {
            throw new RuntimeException("Symbol table already contains symbol %s".formatted(name));
        }

        symbols.put(name, entry);

        if (entry instanceof BindingEntry be) {
            bindings.put(be.getDispatchId(), be);
        }
    }

    public Entry lookup(String name) {
        if (symbols.containsKey(name)) {
            return symbols.get(name);
        }

        if (parent != null) {
            return parent.lookup(name);
        }

        return null;
    }

    public BindingEntry lookupBindingByDispatchId(int dispatchId) {
        if (bindings.containsKey(dispatchId)) {
            return bindings.get(dispatchId);
        }

        if (parent != null) {
            return parent.lookupBindingByDispatchId(dispatchId);
        }

        return null;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();

        sb.append("SymbolTable %s:%n".formatted(name));

        for (var e : symbols.entrySet()) {
            sb.append("%s -> %s%n".formatted(e.getKey(), e.getValue()));
        }

        if (parent != null) {
            sb.append(parent);
        }

        return sb.toString();
    }
}
