package edu.pku.sei.tsr.snowgraph.api.neo4j;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import java.util.Map;
import java.util.stream.Stream;

public interface Neo4jService extends AutoCloseable {
    void close();

    Transaction beginTx();

    Neo4jNode getNodeById(long id);

    Stream<Neo4jNode> getAllNodes();

    Stream<Neo4jNode> findNodes(String label);

    default Neo4jNode createNode(String label) {
        return createNode(label, Map.of());
    }

    Neo4jNode createNode(String label, Map<String, Object> properties);

    long createRelationship(long nodeAId, long nodeBId, RelationshipType type);
}
