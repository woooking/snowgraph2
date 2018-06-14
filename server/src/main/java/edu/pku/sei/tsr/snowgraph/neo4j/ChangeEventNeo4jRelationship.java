package edu.pku.sei.tsr.snowgraph.neo4j;

import edu.pku.sei.tsr.snowgraph.api.event.ChangeEvent;
import edu.pku.sei.tsr.snowgraph.api.event.ChangeEventManager;
import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jNode;
import org.neo4j.graphdb.Relationship;

public class ChangeEventNeo4jRelationship extends BasicNeo4jRelationship{
    private final ChangeEventManager<Long> changedNodes;
    private final ChangeEventManager<Long> changedRelationships;

    public ChangeEventNeo4jRelationship(Relationship relationship, ChangeEventManager<Long> changedNodes, ChangeEventManager<Long> changedRelationships) {
        super(relationship);
        this.changedNodes = changedNodes;
        this.changedRelationships = changedRelationships;
    }

    @Override
    public void delete() {
        changedRelationships.addEvent(relationship.getId(), ChangeEvent.Type.DELETED);
        super.delete();
    }

    @Override
    public Neo4jNode getStartNode() {
        return new ChangeEventNeo4jNode(relationship.getStartNode(), changedNodes, changedRelationships);
    }

}
