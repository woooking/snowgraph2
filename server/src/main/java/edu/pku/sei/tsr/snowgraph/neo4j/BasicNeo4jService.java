package edu.pku.sei.tsr.snowgraph.neo4j;

import edu.pku.sei.tsr.snowgraph.api.Neo4jService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import java.util.Map;

public class BasicNeo4jService implements Neo4jService {
    private final GraphDatabaseService graphDatabaseService;

    public BasicNeo4jService(GraphDatabaseService graphDatabaseService) {
        this.graphDatabaseService = graphDatabaseService;
    }

    @Override
    public void close() {
        graphDatabaseService.shutdown();
    }

    @Override
    public Transaction beginTx() {
        return graphDatabaseService.beginTx();
    }

    @Override
    public long createNode(Label label, Map<String, Object> properties) {
        var node = graphDatabaseService.createNode(label);
        properties.forEach(node::setProperty);
        return node.getId();
    }

    @Override
    public long createRelationship(long nodeAId, long nodeBId, RelationshipType type) {
        var nodeA = graphDatabaseService.getNodeById(nodeAId);
        var nodeB = graphDatabaseService.getNodeById(nodeBId);
        var relationship = nodeA.createRelationshipTo(nodeB, type);
        return relationship.getId();
    }
}
