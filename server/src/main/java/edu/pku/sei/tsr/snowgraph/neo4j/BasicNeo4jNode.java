package edu.pku.sei.tsr.snowgraph.neo4j;

import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jNode;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

public class BasicNeo4jNode implements Neo4jNode {
    private final Node node;

    public BasicNeo4jNode(Node node) {
        this.node = node;
    }

    @Override
    public boolean hasLabel(String label) {
        return node.hasLabel(Label.label(label));
    }

    @Override
    public boolean hasProperty(String property) {
        return node.hasProperty(property);
    }

    @Override
    public Object getProperty(String property) {
        return node.getProperty(property);
    }

    @Override
    public void setProperty(String property, Object value) {
        node.setProperty(property, value);
    }

    @Override
    public void removeProperty(String property) {
        node.removeProperty(property);
    }
}
