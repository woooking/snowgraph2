package edu.pku.sei.tsr.snowgraph.api.neo4j;

public interface Neo4jRelationship {

    long getId();

    void delete();

    Neo4jNode getStartNode();
}
