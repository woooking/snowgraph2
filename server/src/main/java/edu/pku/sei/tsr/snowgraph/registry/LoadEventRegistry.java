package edu.pku.sei.tsr.snowgraph.registry;

import edu.pku.sei.tsr.snowgraph.api.event.LoadEvent;
import edu.pku.sei.tsr.snowgraph.api.plugin.SnowGraphPlugin;

import java.nio.file.Path;

public class LoadEventRegistry implements LifeCycleRegistry<LoadEvent> {
    private final Path graphConfigDirPath;

    public LoadEventRegistry(Path graphConfigDirPath) {
        this.graphConfigDirPath = graphConfigDirPath;
    }

    @Override
    public LoadEvent viewFor(SnowGraphPlugin plugin) {
        return new LoadEvent() {
            @Override
            public Path getConfigDirPath() {
                return graphConfigDirPath.resolve(plugin.getClass().getName());
            }
        };
    }
}
