package edu.pku.sei.tsr.snowgraph.api.context;

import edu.pku.sei.tsr.snowgraph.api.Neo4jService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;

import java.io.File;
import java.util.Collection;

public interface SnowGraphContext {
    Collection<File> getData();

    Logger getLogger();

    Neo4jService getNeo4jService();
}
