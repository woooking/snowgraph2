package edu.pku.sei.tsr.snowgraph.registry;

import edu.pku.sei.tsr.snowgraph.SnowGraphPluginInfo;
import edu.pku.sei.tsr.snowgraph.api.PostInitRegistry;
import edu.pku.sei.tsr.snowgraph.api.SnowGraphPlugin;

import java.util.Map;

public class SnowGraphPostInitRegistry implements LifeCycleRegistry<PostInitRegistry> {
    private final Map<String, SnowGraphPluginInfo> plugins;

    public SnowGraphPostInitRegistry(Map<String, SnowGraphPluginInfo> plugins) {
        this.plugins = plugins;
    }

    @Override
    public PostInitRegistry viewFor(SnowGraphPlugin plugin) {
        return new PostInitRegistry() {
            @Override
            public void registerWatchPath(String path) {
                plugins.get(plugin.getClass().getName()).addWatchPath(path);
            }
        };
    }
}
