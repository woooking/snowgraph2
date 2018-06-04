package edu.pku.sei.tsr.snowgraph.api.neo4j;

public interface Neo4jRelationship {

    long getId();

    Neo4jNode getStartNode();
}
