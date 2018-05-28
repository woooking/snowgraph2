package edu.pku.sei.tsr.snowgraph.neo4j;

import edu.pku.sei.tsr.snowgraph.api.GraphEntity;
import edu.pku.sei.tsr.snowgraph.api.Neo4jOGMService;
import edu.pku.sei.tsr.snowgraph.api.Neo4jOGMServiceFactory;
import org.neo4j.ogm.session.SessionFactory;

public class GenericNeo4JOGMServiceFactory implements Neo4jOGMServiceFactory {

    private final SessionFactory sessionFactory;

    public GenericNeo4JOGMServiceFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public <T extends GraphEntity> Neo4jOGMService<T> createService(Class<T> entityClass) {
        return new GenericNeo4JOGMService<>(sessionFactory, entityClass);
    }
}
