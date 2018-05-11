package edu.pku.sei.tsr.snowgraph.registry;

import edu.pku.sei.tsr.snowgraph.api.SnowGraphPlugin;

public interface LifeCycleRegistry<T> {
    T viewFor(SnowGraphPlugin plugin);
}
