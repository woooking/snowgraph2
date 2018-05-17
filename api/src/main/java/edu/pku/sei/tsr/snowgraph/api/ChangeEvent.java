package edu.pku.sei.tsr.snowgraph.api;

import java.util.Objects;

public final class ChangeEvent<T> {
    public static enum Type {
        CREATED, MODIFIED, DELETED
    }

    private final Type type;
    private final T instance;

    public ChangeEvent(Type type, T instance) {
        this.type = type;
        this.instance = instance;
    }

    public Type getType() {
        return type;
    }

    public T getInstance() {
        return instance;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ChangeEvent)) return false;
        var other = (ChangeEvent) obj;
        return other.type == type && Objects.equals(other.instance, instance);
    }

    @Override
    public int hashCode() {
        int hash;
        hash = type.hashCode();
        hash = hash * 31 + instance.hashCode();
        return hash;
    }
}
