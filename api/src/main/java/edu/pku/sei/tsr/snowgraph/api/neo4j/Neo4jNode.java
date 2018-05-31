package edu.pku.sei.tsr.snowgraph.api.neo4j;

public interface Neo4jNode {
    boolean hasLabel(String label);

    boolean hasProperty(String property);

    Object getProperty(String property);

    void setProperty(String property, Object value);

    void removeProperty(String property);
}
