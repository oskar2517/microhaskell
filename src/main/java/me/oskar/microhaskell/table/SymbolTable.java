package me.oskar.microhaskell.table;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    private final SymbolTable parent;
    private final Map<String, Entry> bindings = new HashMap<>();
    private final Map<String, FixityEntry> fixities = new HashMap<>();

    public SymbolTable(SymbolTable parent) {
        this.parent = parent;
    }

    public SymbolTable() {
        this(null);
    }

    public void enterFixity(String name, FixityEntry entry, Runnable error) {
        if (fixities.containsKey(name)) {
            error.run();
        }

        fixities.put(name, entry);
    }

    public void enterFixity(String name, FixityEntry entry) {
        enterFixity(name, entry, () -> {});
    }

    public FixityEntry lookupFixity(String name) {
        if (fixities.containsKey(name)) {
            return fixities.get(name);
        }

        if (parent != null) {
            return parent.lookupFixity(name);
        }

        return new FixityEntry(FixityEntry.Associativity.LEFT, 9);
    }

    public void enterBinding(String name, Entry entry) {
        enterBinding(name, entry, () -> {});
    }

    public void enterBinding(String name, Entry entry, Runnable error) {
        if (name.equals("_")) return;

        if (bindings.containsKey(name)) {
            error.run();
        }

        bindings.put(name, entry);
    }

    public void removeBinding(String name) {
        if (bindings.containsKey(name)) {
            bindings.remove(name);
        } else if (parent != null) {
            parent.removeBinding(name);
        }
    }

    public Entry lookupBinding(String name) {
        if (bindings.containsKey(name)) {
            return bindings.get(name);
        }

        if (parent != null) {
            return parent.lookupBinding(name);
        }

        return null;
    }

    public Entry lookupBinding(String name, Runnable error) {
        var entry = lookupBinding(name);

        if (entry == null) {
            error.run();
        }

        return entry;
    }

    public boolean isBindingDefined(String name) {
        return lookupBinding(name) != null;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();

        sb.append("SymbolTable:%n".formatted());

        for (var e : bindings.entrySet()) {
            sb.append("%s -> %s%n".formatted(e.getKey(), e.getValue()));
        }

        if (parent != null) {
            sb.append(parent);
        }

        return sb.toString();
    }
}
