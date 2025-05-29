package me.oskar.microhaskell.table;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    private final SymbolTable parent;
    private final Map<String, Entry> symbols = new HashMap<>();
    private final Map<Integer, BindingEntry> bindings = new HashMap<>();

    public SymbolTable(SymbolTable parent) {
        this.parent = parent;
    }

    public SymbolTable() {
        this(null);
    }

    public void enter(String name, Entry entry) {
        enter(name, entry, () -> {});
    }

    public void enter(String name, Entry entry, Runnable error) {
        if (name.equals("_")) return;

        if (symbols.containsKey(name)) {
            error.run();
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

    public Entry lookup(String name, Runnable error) {
        var entry = lookup(name);

        if (entry == null) {
            error.run();
        }

        return entry;
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

    public boolean isDefined(String name) {
        return lookup(name) != null;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();

        sb.append("SymbolTable:%n".formatted());

        for (var e : symbols.entrySet()) {
            sb.append("%s -> %s%n".formatted(e.getKey(), e.getValue()));
        }

        if (parent != null) {
            sb.append(parent);
        }

        return sb.toString();
    }
}
