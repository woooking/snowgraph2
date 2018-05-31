package edu.pku.sei.tsr.snowgraph.neo4j;

import edu.pku.sei.tsr.snowgraph.api.event.ChangeEvent;
import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jNode;
import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jService;
import edu.pku.sei.tsr.snowgraph.api.event.ChangeEventManager;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

public class ChangeEventNeo4jService implements Neo4jService {
    private final GraphDatabaseService graphDatabaseService;
    private final ChangeEventManager<Long> changedNodes = new ChangeEventManager<>();
    private final ChangeEventManager<Long> changedRelationships = new ChangeEventManager<>();
    private boolean isShutdown = false;

    public ChangeEventNeo4jService(GraphDatabaseService graphDatabaseService) {
        this.graphDatabaseService = graphDatabaseService;
    }

    @Override
    public void close() {
        graphDatabaseService.shutdown();
        isShutdown = true;
    }

    @Override
    public Transaction beginTx() {
        return graphDatabaseService.beginTx();
    }

    @Override
    public Neo4jNode getNodeById(long id) {
        return new ChangeEventNeo4jNode(changedNodes, graphDatabaseService.getNodeById(id));
    }

    @Override
    public Stream<Neo4jNode> getAllNodes() {
        return graphDatabaseService.getAllNodes().stream().map(n -> new ChangeEventNeo4jNode(changedNodes, n));
    }

    @Override
    public Stream<Neo4jNode> findNodes(Label label) {
        return graphDatabaseService.findNodes(label).stream().map(n -> new ChangeEventNeo4jNode(changedNodes, n));
    }

    @Override
    public long createNode(Label label, Map<String, Object> properties) {
        var node = graphDatabaseService.createNode(label);
        properties.forEach(node::setProperty);
        var id = node.getId();
        changedNodes.addEvent(id, ChangeEvent.Type.CREATED);
        return id;
    }

    @Override
    public long createRelationship(long nodeAId, long nodeBId, RelationshipType type) {
        var nodeA = graphDatabaseService.getNodeById(nodeAId);
        var nodeB = graphDatabaseService.getNodeById(nodeBId);
        var relationship = nodeA.createRelationshipTo(nodeB, type);
        var id = relationship.getId();
        changedRelationships.addEvent(id, ChangeEvent.Type.CREATED);
        return id;
    }

    public Collection<ChangeEvent<Long>> getChangedNodes() {
        assert isShutdown;
        return changedNodes.getChanges();
    }

    public Collection<ChangeEvent<Long>> getChangedRelationships() {
        assert isShutdown;
        return changedRelationships.getChanges();
    }
}
