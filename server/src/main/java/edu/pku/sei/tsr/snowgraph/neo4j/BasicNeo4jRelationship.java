package edu.pku.sei.tsr.snowgraph.neo4j;

import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jNode;
import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jRelationship;
import org.neo4j.graphdb.*;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class BasicNeo4jRelationship implements Neo4jRelationship {
    protected final Relationship relationship;

    public BasicNeo4jRelationship(Relationship relationship) {
        this.relationship = relationship;
    }

    @Override
    public long getId() {
        return relationship.getId();
    }

    @Override
    public Neo4jNode getStartNode() {
        return new BasicNeo4jNode(relationship.getStartNode());
    }

}
