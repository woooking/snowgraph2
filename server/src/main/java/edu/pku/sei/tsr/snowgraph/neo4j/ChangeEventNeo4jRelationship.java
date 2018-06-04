package edu.pku.sei.tsr.snowgraph.neo4j;

import edu.pku.sei.tsr.snowgraph.api.event.ChangeEventManager;
import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jNode;
import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jRelationship;
import org.neo4j.graphdb.Relationship;

public class ChangeEventNeo4jRelationship extends BasicNeo4jRelationship{
    private final ChangeEventManager<Long> changedNodes;

    public ChangeEventNeo4jRelationship(ChangeEventManager<Long> changedNodes, Relationship relationship) {
        super(relationship);
        this.changedNodes = changedNodes;
    }

    @Override
    public Neo4jNode getStartNode() {
        return new ChangeEventNeo4jNode(changedNodes, relationship.getStartNode());
    }

}
