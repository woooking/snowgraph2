package edu.pku.sei.tsr.snowgraph.neo4j;

import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jNode;
import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import java.util.Map;
import java.util.stream.Stream;

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
    public Neo4jNode getNodeById(long id) {
        return new BasicNeo4jNode(graphDatabaseService.getNodeById(id));
    }

    @Override
    public Stream<Neo4jNode> getAllNodes() {
        return graphDatabaseService.getAllNodes().stream().map(BasicNeo4jNode::new);
    }

    @Override
    public Stream<Neo4jNode> findNodes(String label) {
        return graphDatabaseService.findNodes(Label.label(label)).stream().map(BasicNeo4jNode::new);
    }

    @Override
    public Neo4jNode createNode(String label, Map<String, Object> properties) {
        var node = graphDatabaseService.createNode(Label.label(label));
        properties.forEach(node::setProperty);
        return new BasicNeo4jNode(node);
    }

    @Override
    public long createRelationship(long nodeAId, long nodeBId, RelationshipType type) {
        var nodeA = graphDatabaseService.getNodeById(nodeAId);
        var nodeB = graphDatabaseService.getNodeById(nodeBId);
        var relationship = nodeA.createRelationshipTo(nodeB, type);
        return relationship.getId();
    }
}
