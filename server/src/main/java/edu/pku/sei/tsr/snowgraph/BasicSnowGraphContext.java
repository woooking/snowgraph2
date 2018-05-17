package edu.pku.sei.tsr.snowgraph;

import edu.pku.sei.tsr.snowgraph.api.Neo4jServiceFactory;
import edu.pku.sei.tsr.snowgraph.api.SnowGraphContext;
import edu.pku.sei.tsr.snowgraph.neo4j.GenericNeo4jServiceFactory;
import org.apache.commons.io.FileUtils;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

public class BasicSnowGraphContext implements SnowGraphContext {
    private final Neo4jServiceFactory serviceFactory;
    private final SnowGraphPluginInfo pluginInfo;
    private final Logger logger;
    private final SnowGraph snowGraph;

    public BasicSnowGraphContext(SnowGraph snowGraph, SessionFactory sessionFactory, SnowGraphPluginInfo pluginInfo) {
        this.snowGraph = snowGraph;
        this.serviceFactory = new GenericNeo4jServiceFactory(sessionFactory);
        this.pluginInfo = pluginInfo;
        this.logger = LoggerFactory.getLogger(pluginInfo.getInstance().getClass());
    }

    @Override
    public Neo4jServiceFactory getNeo4jServiceFactory() {
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
