package edu.pku.sei.tsr.snowgraph.api.context;

import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;

import java.io.File;
import java.util.Collection;

public interface SnowGraphContext {
    Collection<File> getData();

    Logger getLogger();

    GraphDatabaseService getDatabaseService();
}
