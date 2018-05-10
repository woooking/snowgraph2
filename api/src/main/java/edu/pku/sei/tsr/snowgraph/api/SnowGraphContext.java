package edu.pku.sei.tsr.snowgraph.api;

import org.slf4j.Logger;

public interface SnowGraphContext {
    Neo4jServiceFactory getNeo4jServiceFactory();

    Logger getLogger();
}
