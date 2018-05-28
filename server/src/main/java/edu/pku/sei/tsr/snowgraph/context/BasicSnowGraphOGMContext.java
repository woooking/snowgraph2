package edu.pku.sei.tsr.snowgraph.context;

import edu.pku.sei.tsr.snowgraph.SnowGraph;
import edu.pku.sei.tsr.snowgraph.SnowGraphPluginInfo;
import edu.pku.sei.tsr.snowgraph.api.Neo4jOGMServiceFactory;
import edu.pku.sei.tsr.snowgraph.api.context.SnowGraphOGMContext;
import edu.pku.sei.tsr.snowgraph.neo4j.GenericNeo4JOGMServiceFactory;
import org.apache.commons.io.FileUtils;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

public class BasicSnowGraphOGMContext implements SnowGraphOGMContext {
    private final Neo4jOGMServiceFactory serviceFactory;
    private final SnowGraphPluginInfo<SnowGraphOGMContext> pluginInfo;
    private final Logger logger;
    private final SnowGraph snowGraph;

    public BasicSnowGraphOGMContext(SnowGraph snowGraph, SnowGraphPluginInfo<SnowGraphOGMContext> pluginInfo, SessionFactory sessionFactory) {
        this.snowGraph = snowGraph;
        this.serviceFactory = new GenericNeo4JOGMServiceFactory(sessionFactory);
        this.pluginInfo = pluginInfo;
        this.logger = LoggerFactory.getLogger(pluginInfo.getInstance().getClass());
    }

    @Override
    public Neo4jOGMServiceFactory getNeo4jServiceFactory() {
        return serviceFactory;
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

}
