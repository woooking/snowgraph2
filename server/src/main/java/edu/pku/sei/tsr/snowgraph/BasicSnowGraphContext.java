package edu.pku.sei.tsr.snowgraph;

import edu.pku.sei.tsr.snowgraph.api.Neo4jServiceFactory;
import edu.pku.sei.tsr.snowgraph.api.SnowGraphContext;
import edu.pku.sei.tsr.snowgraph.neo4j.GenericNeo4jServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicSnowGraphContext implements SnowGraphContext {
    private final Neo4jServiceFactory serviceFactory = new GenericNeo4jServiceFactory();
    private final Class<?> pluginClass;
    private final Logger logger;

    public BasicSnowGraphContext(Class<?> pluginClass) {
        this.pluginClass = pluginClass;
        this.logger = LoggerFactory.getLogger(pluginClass);
    }

    @Override
    public Neo4jServiceFactory getNeo4jServiceFactory() {
        return serviceFactory;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
