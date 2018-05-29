package edu.pku.sei.tsr.snowgraph.api;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import java.util.Map;

public interface Neo4jService extends AutoCloseable {
    void close();

    Transaction beginTx();

    default long createNode(Label label) {
        return createNode(label, Map.of());
    }

    long createNode(Label label, Map<String, Object> properties);

    long createRelationship(long nodeAId, long nodeBId, RelationshipType type);
}
