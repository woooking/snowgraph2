package edu.pku.sei.tsr.snowgraph.registry;

import edu.pku.sei.tsr.snowgraph.SnowGraph;
import edu.pku.sei.tsr.snowgraph.SnowGraphPluginInfo;
import edu.pku.sei.tsr.snowgraph.api.event.LoadEvent;
import edu.pku.sei.tsr.snowgraph.api.event.ShutDownEvent;
import edu.pku.sei.tsr.snowgraph.api.plugin.SnowGraphPlugin;

import java.nio.file.Path;
import java.util.Map;

public class ShutDownEventRegistry implements LifeCycleRegistry<ShutDownEvent> {

    private final Path graphConfigDirPath;
    
    public ShutDownEventRegistry(Path graphConfigDirPath) {
        this.graphConfigDirPath = graphConfigDirPath;
    }

    @Override
    public ShutDownEvent viewFor(SnowGraphPlugin plugin) {
        return new ShutDownEvent() {
            @Override
            public Path getConfigDirPath() {
                return graphConfigDirPath.resolve(plugin.getClass().getName());
            }
        };
    }
}
