package edu.pku.sei.tsr.snowgraph.neo4j;

import edu.pku.sei.tsr.snowgraph.api.GraphEntity;
import edu.pku.sei.tsr.snowgraph.api.Neo4jService;
import edu.pku.sei.tsr.snowgraph.api.Neo4jServiceFactory;
import org.neo4j.ogm.session.SessionFactory;

public class GenericNeo4jServiceFactory implements Neo4jServiceFactory {

    private final SessionFactory sessionFactory;

    public GenericNeo4jServiceFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public <T extends GraphEntity> Neo4jService<T> createService(Class<T> entityClass) {
        return new GenericNeo4jService<>(sessionFactory, entityClass);
    }
}
