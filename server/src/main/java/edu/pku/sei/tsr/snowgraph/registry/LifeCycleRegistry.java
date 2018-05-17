package edu.pku.sei.tsr.snowgraph.registry;

import edu.pku.sei.tsr.snowgraph.api.plugin.SnowGraphPlugin;

public interface LifeCycleRegistry<T> {
    T viewFor(SnowGraphPlugin plugin);
}
