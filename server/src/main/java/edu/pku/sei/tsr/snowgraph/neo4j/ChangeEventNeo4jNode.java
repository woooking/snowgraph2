package edu.pku.sei.tsr.snowgraph.neo4j;

import edu.pku.sei.tsr.snowgraph.api.event.ChangeEvent;
import edu.pku.sei.tsr.snowgraph.api.event.ChangeEventManager;
import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jNode;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

public class ChangeEventNeo4jNode implements Neo4jNode {
    private final ChangeEventManager<Long> changedNodes;
    private final Node node;

    public ChangeEventNeo4jNode(ChangeEventManager<Long> changedNodes, Node node) {
        this.changedNodes = changedNodes;
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
        changedNodes.addEvent(node.getId(), ChangeEvent.Type.MODIFIED);
    }

    @Override
    public void removeProperty(String property) {
        node.removeProperty(property);
        changedNodes.addEvent(node.getId(), ChangeEvent.Type.MODIFIED);
    }
}
