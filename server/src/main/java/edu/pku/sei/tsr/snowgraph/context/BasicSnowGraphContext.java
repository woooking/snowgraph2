package edu.pku.sei.tsr.snowgraph.context;

import edu.pku.sei.tsr.snowgraph.SnowGraph;
import edu.pku.sei.tsr.snowgraph.SnowGraphPluginInfo;
import edu.pku.sei.tsr.snowgraph.api.context.SnowGraphContext;
import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

public class BasicSnowGraphContext implements SnowGraphContext, AutoCloseable {
    private final SnowGraphPluginInfo pluginInfo;
    private final Logger logger;
    private final SnowGraph snowGraph;
    private final GraphDatabaseService databaseService;

    public BasicSnowGraphContext(SnowGraph snowGraph, SnowGraphPluginInfo pluginInfo, GraphDatabaseBuilder databaseBuilder) {
        this.snowGraph = snowGraph;
        this.pluginInfo = pluginInfo;
        this.logger = LoggerFactory.getLogger(pluginInfo.getInstance().getClass());
        this.databaseService = databaseBuilder.newGraphDatabase();
    }

    @Override
    public Collection<File> getData() {
        Path dataRoot = Paths.get(snowGraph.getDataDir());

        return this.pluginInfo.getDataPaths().stream()
            .map(dataRoot::resolve)
            .flatMap(p -> FileUtils.listFiles(p.toFile(), null, true).stream())
            .collect(Collectors.toList());
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public GraphDatabaseService getDatabaseService() {
        return databaseService;
    }

    @Override
    public void close() {
        databaseService.shutdown();
    }
}
