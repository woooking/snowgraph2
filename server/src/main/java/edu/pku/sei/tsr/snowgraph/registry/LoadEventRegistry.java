package edu.pku.sei.tsr.snowgraph.registry;

import edu.pku.sei.tsr.snowgraph.SnowGraphPluginInfo;
import edu.pku.sei.tsr.snowgraph.api.InitRegistry;
import edu.pku.sei.tsr.snowgraph.api.event.LoadEvent;
import edu.pku.sei.tsr.snowgraph.api.plugin.SnowGraphPlugin;

import java.util.List;
import java.util.Map;

public class LoadEventRegistry implements LifeCycleRegistry<LoadEvent> {
    private final Map<String, SnowGraphPluginInfo> plugins;

    public LoadEventRegistry(Map<String, SnowGraphPluginInfo> plugins) {
        this.plugins = plugins;
    }

    @Override
    public LoadEvent viewFor(SnowGraphPlugin plugin) {
        return new LoadEvent() {
        };
    }
}
