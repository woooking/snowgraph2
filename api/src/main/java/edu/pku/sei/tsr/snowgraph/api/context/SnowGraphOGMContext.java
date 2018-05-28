package edu.pku.sei.tsr.snowgraph.api.context;

import edu.pku.sei.tsr.snowgraph.api.Neo4jOGMServiceFactory;

public interface SnowGraphOGMContext extends SnowGraphContext {
    Neo4jOGMServiceFactory getNeo4jServiceFactory();
}
