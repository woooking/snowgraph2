package edu.pku.sei.tsr.snowgraph;

import edu.pku.sei.tsr.snowgraph.api.SnowGraphPlugin;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SnowGraphPluginInfo {
    private final SnowGraphPluginConfig config;
    private SnowGraphPlugin instance;
    private final List<Path> watchPaths = new ArrayList<>();

    public SnowGraphPluginInfo(SnowGraphPluginConfig config) {
        this.config = config;
    }

    public SnowGraphPlugin getInstance() {
        return instance;
    }

    public void setInstance(SnowGraphPlugin instance) {
        this.instance = instance;
    }

    public SnowGraphPluginConfig getConfig() {
        return config;
    }

    public void addWatchPath(String path) {
        watchPaths.add(Paths.get(path));
    }
}
