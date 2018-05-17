package edu.pku.sei.tsr.snowgraph.api;

import org.slf4j.Logger;

import java.io.File;
import java.util.Collection;

public interface SnowGraphContext {
    Neo4jServiceFactory getNeo4jServiceFactory();

    Collection<File> getData();

    Logger getLogger();
}
