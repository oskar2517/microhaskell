package me.oskar.microhaskell.table;

import me.oskar.microhaskell.ast.FunctionDefinitionNode;

public class BindingEntry implements Entry {

    private boolean appliedRecursively = false;
    private boolean appliedMutuallyRecursively = false;
    private final int dispatchId;
    private FunctionDefinitionNode node;

    private final SymbolTable localTable;

    public BindingEntry(SymbolTable localTable, int dispatchId) {
        this.localTable = localTable;
        this.dispatchId = dispatchId;
    }

    public SymbolTable getLocalTable() {
        return localTable;
    }

    public int getDispatchId() {
        return dispatchId;
    }

    public boolean isAppliedRecursively() {
        return appliedRecursively;
    }

    public boolean isAppliedMutuallyRecursively() {
        return appliedMutuallyRecursively;
    }

    public void setAppliedRecursively(boolean appliedRecursively) {
        this.appliedRecursively = appliedRecursively;
    }

    public void setAppliedMutuallyRecursively(boolean appliedMutuallyRecursively) {
        this.appliedMutuallyRecursively = appliedMutuallyRecursively;
    }

    public FunctionDefinitionNode getNode() {
        return node;
    }

    public void setNode(FunctionDefinitionNode node) {
        this.node = node;
    }

    @Override
    public String toString() {
        return "(Binding dispatchId=%s, appliedRecursively=%s, appliedMutuallyRecursively=%s)"
                .formatted(dispatchId, appliedRecursively, appliedMutuallyRecursively);
    }
}
