package edu.pku.sei.tsr.snowgraph.registry;

import edu.pku.sei.tsr.snowgraph.SnowGraphPluginInfo;
import edu.pku.sei.tsr.snowgraph.api.PreInitRegistry;
import edu.pku.sei.tsr.snowgraph.api.SnowGraphPlugin;

import java.util.Map;

public class SnowGraphPreInitRegistry implements LifeCycleRegistry<PreInitRegistry> {
    private final Map<String, SnowGraphPluginInfo> plugins;

    public SnowGraphPreInitRegistry(Map<String, SnowGraphPluginInfo> plugins) {
        this.plugins = plugins;
    }

    @Override
    public PreInitRegistry viewFor(SnowGraphPlugin plugin) {
        return new PreInitRegistry() {
        };
    }
}
