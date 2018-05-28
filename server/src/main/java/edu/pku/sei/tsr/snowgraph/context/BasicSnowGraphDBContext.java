package edu.pku.sei.tsr.snowgraph.context;

import edu.pku.sei.tsr.snowgraph.SnowGraph;
import edu.pku.sei.tsr.snowgraph.SnowGraphPluginInfo;
import edu.pku.sei.tsr.snowgraph.api.Neo4jOGMServiceFactory;
import edu.pku.sei.tsr.snowgraph.api.context.SnowGraphDBContext;
import edu.pku.sei.tsr.snowgraph.api.context.SnowGraphOGMContext;
import edu.pku.sei.tsr.snowgraph.neo4j.GenericNeo4JOGMServiceFactory;
import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

public class BasicSnowGraphDBContext implements SnowGraphDBContext {
    private final SnowGraphPluginInfo<SnowGraphDBContext> pluginInfo;
    private final Logger logger;
    private final SnowGraph snowGraph;
    private final GraphDatabaseBuilder databaseBuilder;

    public BasicSnowGraphDBContext(SnowGraph snowGraph, SnowGraphPluginInfo<SnowGraphDBContext> pluginInfo, GraphDatabaseBuilder databaseBuilder) {
        this.snowGraph = snowGraph;
        this.pluginInfo = pluginInfo;
        this.logger = LoggerFactory.getLogger(pluginInfo.getInstance().getClass());
        this.databaseBuilder = databaseBuilder;
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
    public GraphDatabaseBuilder getDatabaseBuilder() {
        return databaseBuilder;
    }
}
