package edu.pku.sei.tsr.snowgraph.neo4j;

import edu.pku.sei.tsr.snowgraph.api.event.ChangeEvent;
import edu.pku.sei.tsr.snowgraph.api.event.ChangeEventManager;
import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jNode;
import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jRelationship;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ChangeEventNeo4jNode extends BasicNeo4jNode implements Neo4jNode {
    private final ChangeEventManager<Long> changedNodes;
    private final ChangeEventManager<Long> changedRelationships;

    public ChangeEventNeo4jNode(Node node, ChangeEventManager<Long> changedNodes, ChangeEventManager<Long> changedRelationships) {
        super(node);
        this.changedNodes = changedNodes;
        this.changedRelationships = changedRelationships;
    }

    @Override
    public void delete() {
        changedNodes.addEvent(node.getId(), ChangeEvent.Type.DELETED);
        super.delete();
    }

    @Override
    public void setProperty(String property, Object value) {
        super.setProperty(property, value);
        changedNodes.addEvent(node.getId(), ChangeEvent.Type.MODIFIED);
    }

    @Override
    public void removeProperty(String property) {
        super.removeProperty(property);
        changedNodes.addEvent(node.getId(), ChangeEvent.Type.MODIFIED);
    }

    @Override
    public Stream<Neo4jRelationship> getRelationships(String relationshipType, Direction direction) {
        return StreamSupport.stream(node.getRelationships(RelationshipType.withName(relationshipType), direction).spliterator(), false)
            .map(r -> new ChangeEventNeo4jRelationship(r, changedNodes, changedRelationships));
    }

    @Override
    public Stream<Neo4jRelationship> getRelationships() {
        return StreamSupport.stream(node.getRelationships().spliterator(), false)
            .map(r -> new ChangeEventNeo4jRelationship(r, changedNodes, changedRelationships));
    }
}
