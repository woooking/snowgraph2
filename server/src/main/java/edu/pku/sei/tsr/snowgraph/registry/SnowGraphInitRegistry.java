package edu.pku.sei.tsr.snowgraph.registry;

import edu.pku.sei.tsr.snowgraph.SnowGraphPluginInfo;
import edu.pku.sei.tsr.snowgraph.api.InitRegistry;
import edu.pku.sei.tsr.snowgraph.api.plugin.SnowGraphPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class SnowGraphInitRegistry implements LifeCycleRegistry<InitRegistry> {
    private final Map<String, SnowGraphPluginInfo> plugins;

    public SnowGraphInitRegistry(Map<String, SnowGraphPluginInfo> plugins) {
        this.plugins = plugins;
    }

    @Override
    public InitRegistry viewFor(SnowGraphPlugin plugin) {
        return new InitRegistry() {
            @Override
            public List<String> getArgs() {
                return plugins.get(plugin.getClass().getName()).getConfig().getArgs();
            }

            @Override
            public Logger getLogger() {
                return LoggerFactory.getLogger(plugin.getClass());
            }
        };
    }
}
