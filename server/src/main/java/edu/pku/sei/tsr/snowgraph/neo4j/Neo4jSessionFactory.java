package edu.pku.sei.tsr.snowgraph.neo4j;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.SessionFactory;

import java.util.List;

public class Neo4jSessionFactory {
    public static SessionFactory createSessionFactory(String location, List<String> entityPackages) {
        var configuration = new Configuration.Builder().uri("file://" + location).build();
        return new SessionFactory(
            configuration,
            entityPackages.toArray(new String[0])
        );
    }

    private Neo4jSessionFactory() {
    }
}
