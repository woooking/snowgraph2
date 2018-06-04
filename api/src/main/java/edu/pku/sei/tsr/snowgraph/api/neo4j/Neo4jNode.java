package edu.pku.sei.tsr.snowgraph.api.neo4j;

import org.neo4j.graphdb.Direction;

import java.util.stream.Stream;

public interface Neo4jNode {

    long getId();

    boolean hasLabel(String label);

    boolean hasProperty(String property);

    Object getProperty(String property);

    void setProperty(String property, Object value);

    void removeProperty(String property);

    boolean hasRelationship(String relationshipType, Direction direction);

    Stream<Neo4jRelationship> getRelationships(String relationshipType, Direction direction);
}
