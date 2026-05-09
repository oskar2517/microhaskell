package me.oskar.microhaskell.table;

public class BindingEntry implements Entry {

    private static int dispatchIdCounter = 0;

    private boolean appliedSelfRecursively = false;
    private boolean appliedMutuallyRecursively = false;

    private final SymbolTable ownerTable;
    private final SymbolTable localTable;
    private final int dispatchId;

    public BindingEntry(SymbolTable ownerTable, SymbolTable localTable) {
        this.ownerTable = ownerTable;
        this.localTable = localTable;

        dispatchId = dispatchIdCounter++;
    }

    public SymbolTable getOwnerTable() {
        return ownerTable;
    }

    public SymbolTable getLocalTable() {
        return localTable;
    }

    public int getDispatchId() {
        return dispatchId;
    }

    public boolean isAppliedSelfRecursively() {
        return appliedSelfRecursively;
    }

    public boolean isAppliedMutuallyRecursively() {
        return appliedMutuallyRecursively;
    }

    public void setAppliedSelfRecursively(boolean appliedSelfRecursively) {
        this.appliedSelfRecursively = appliedSelfRecursively;
    }

    public void setAppliedMutuallyRecursively(boolean appliedMutuallyRecursively) {
        this.appliedMutuallyRecursively = appliedMutuallyRecursively;
    }

    @Override
    public String toString() {
        return "(Binding dispatchId=%s, appliedRecursively=%s, appliedMutuallyRecursively=%s)"
                .formatted(dispatchId, appliedSelfRecursively, appliedMutuallyRecursively);
    }
}
