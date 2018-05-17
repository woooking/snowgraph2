package edu.pku.sei.tsr.snowgraph.exception;

import edu.pku.sei.tsr.snowgraph.api.plugin.SnowGraphPlugin;

public class DependenceException extends RuntimeException {
    private DependenceException(String message) {
        super(message);
    }

    public static DependenceException initializeError(String path) {
        return new DependenceException(String.format("Could not construct instance of %s!", path));
    }

    public static DependenceException notSatisfied(SnowGraphPlugin plugin, String dependency) {
        return new DependenceException(String.format("Plugin %s requires %s, but not found!", plugin.getClass().getName(), dependency));
    }

    public static DependenceException cycledDependence() {
        return new DependenceException("Cycled dependence found!");
    }
}
