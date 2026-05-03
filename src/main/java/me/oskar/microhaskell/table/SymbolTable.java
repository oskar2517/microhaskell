package me.oskar.microhaskell.table;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    private final SymbolTable parent;
    private final Map<String, Entry> functions = new HashMap<>();
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

    public void enterFunction(String name, Entry entry) {
        enterFunction(name, entry, () -> {});
    }

    public void enterFunction(String name, Entry entry, Runnable error) {
        if (name.equals("_")) return;

        if (functions.containsKey(name)) {
            error.run();
        }

        functions.put(name, entry);
    }

    public void removeFunction(String name) {
        if (functions.containsKey(name)) {
            functions.remove(name);
        } else if (parent != null) {
            parent.removeFunction(name);
        }
    }

    public Entry lookupFunction(String name) {
        if (functions.containsKey(name)) {
            return functions.get(name);
        }

        if (parent != null) {
            return parent.lookupFunction(name);
        }

        return null;
    }

    public Entry lookupFunction(String name, Runnable error) {
        var entry = lookupFunction(name);

        if (entry == null) {
            error.run();
        }

        return entry;
    }

    public boolean isFunctionDefined(String name) {
        return lookupFunction(name) != null;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();

        sb.append("SymbolTable:%n".formatted());

        for (var e : functions.entrySet()) {
            sb.append("%s -> %s%n".formatted(e.getKey(), e.getValue()));
        }

        if (parent != null) {
            sb.append(parent);
        }

        return sb.toString();
    }
}
