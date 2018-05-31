package edu.pku.sei.tsr.snowgraph.api.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

public final class ChangeEvent<T> {
    private static Logger logger = LoggerFactory.getLogger(ChangeEvent.class);

    public enum Type {
        CREATED, MODIFIED, DELETED
    }

    public static Optional<Type> transform(ChangeEvent.Type old, ChangeEvent.Type next) {
        switch (old) {
            case CREATED:
                switch (next) {
                    case CREATED:
                        logger.warn("File created twice!");
                        return Optional.of(ChangeEvent.Type.CREATED);
                    case MODIFIED:
                        return Optional.of(ChangeEvent.Type.CREATED);
                    case DELETED:
                        return Optional.empty();
                }
            case MODIFIED:
                switch (next) {
                    case CREATED:
                        logger.warn("Creating an existed file!");
                        return Optional.of(ChangeEvent.Type.MODIFIED);
                    case MODIFIED:
                        return Optional.of(ChangeEvent.Type.MODIFIED);
                    case DELETED:
                        return Optional.of(ChangeEvent.Type.DELETED);
                }
            case DELETED:
                switch (next) {
                    case CREATED:
                        return Optional.of(ChangeEvent.Type.CREATED);
                    case MODIFIED:
                        logger.warn("Modifying a deleted file!");
                        return Optional.of(ChangeEvent.Type.DELETED);
                    case DELETED:
                        logger.warn("File deleted twice!");
                        return Optional.of(ChangeEvent.Type.DELETED);
                }
        }
        return Optional.empty();
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
