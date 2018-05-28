package edu.pku.sei.tsr.snowgraph.api.context;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;

public interface SnowGraphDBContext extends SnowGraphContext {
    GraphDatabaseBuilder getDatabaseBuilder();
}
