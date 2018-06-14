package edu.pku.sei.tsr.snowgraph.neo4j;

import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jNode;
import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jRelationship;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class BasicNeo4jNode implements Neo4jNode {
    protected final Node node;

    public BasicNeo4jNode(Node node) {
        this.node = node;
    }

    @Override
    public long getId() {
        return node.getId();
    }

    @Override
    public void delete() {
        node.delete();
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

    @Override
    public boolean hasRelationship(String relationshipType, Direction direction) {
        return node.hasRelationship(RelationshipType.withName(relationshipType), direction);
    }

    @Override
    public Stream<Neo4jRelationship> getRelationships(String relationshipType, Direction direction) {
        return StreamSupport.stream(node.getRelationships(RelationshipType.withName(relationshipType), direction).spliterator(), false)
            .map(BasicNeo4jRelationship::new);
    }

    @Override
    public Stream<Neo4jRelationship> getRelationships() {
        return StreamSupport.stream(node.getRelationships().spliterator(), false)
            .map(BasicNeo4jRelationship::new);
    }
}
