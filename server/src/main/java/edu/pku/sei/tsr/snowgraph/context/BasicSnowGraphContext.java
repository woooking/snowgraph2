package edu.pku.sei.tsr.snowgraph.context;

import edu.pku.sei.tsr.snowgraph.SnowGraph;
import edu.pku.sei.tsr.snowgraph.SnowGraphPluginInfo;
import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jService;
import edu.pku.sei.tsr.snowgraph.api.context.SnowGraphContext;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

public class BasicSnowGraphContext implements SnowGraphContext, AutoCloseable {
    private final SnowGraphPluginInfo pluginInfo;
    private final SnowGraph snowGraph;
    private final Neo4jService neo4jService;

    public BasicSnowGraphContext(SnowGraph snowGraph, SnowGraphPluginInfo pluginInfo, Neo4jService neo4jService) {
        this.snowGraph = snowGraph;
        this.pluginInfo = pluginInfo;
        this.neo4jService = neo4jService;
    }

    @Override
    public Collection<File> getData() {
        Path dataRoot = Paths.get(snowGraph.getDataDir());

        return this.pluginInfo.getDataPaths().stream()
            .map(dataRoot::resolve)
            .map(Path::toFile)
            .collect(Collectors.toList());
    }

    @Override
    public Neo4jService getNeo4jService() {
        return neo4jService;
    }

    @Override
    public void close() {
        neo4jService.close();
    }
}
