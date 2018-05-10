package edu.pku.sei.tsr.snowgraph.neo4j;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

public class Neo4jSessionFactory {
    private final static Configuration configuration = new Configuration.Builder()
        .uri("file:///home/woooking/lab/neo4j/databases/snow-graph")
        .build();
    private final static SessionFactory sessionFactory = new SessionFactory(
        configuration,
        "edu.pku.sei.tsr.snowgraph.javacodeextractor.entity"
    ); // TODO: dynamic scan packages
    private static Neo4jSessionFactory factory = new Neo4jSessionFactory();

    public static Neo4jSessionFactory getInstance() {
        return factory;
    }

    private Neo4jSessionFactory() {
    }

    public Session getNeo4jSession() {
        return sessionFactory.openSession();
    }
}
