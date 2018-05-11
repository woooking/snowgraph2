package edu.pku.sei.tsr.snowgraph.neo4j;

import edu.pku.sei.tsr.snowgraph.api.GraphEntity;
import edu.pku.sei.tsr.snowgraph.api.Neo4jService;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import java.util.Collection;

class GenericNeo4jService<T extends GraphEntity> implements Neo4jService<T> {
    private static final int DEPTH_LIST = 0;
    private static final int DEPTH_ENTITY = 1;
    private final Session session;
    private final Class<T> entityClass;

    GenericNeo4jService(SessionFactory sessionFactory, Class<T> entityClass) {
        this.session = sessionFactory.openSession();
        this.entityClass = entityClass;
    }

    @Override
    public Iterable<T> findAll() {
        return session.loadAll(getEntityType(), DEPTH_LIST);
    }

    @Override
    public T find(Long id) {
        return session.load(getEntityType(), id, DEPTH_ENTITY);
    }

    @Override
    public void delete(Long id) {
        session.delete(session.load(getEntityType(), id));
    }

    @Override
    public T save(T entity) {
        session.save(entity);
        return entity;
    }

    @Override
    public Collection<T> saveAll(Collection<T> objects) {
        session.save(objects, DEPTH_ENTITY);
        return objects;
    }

    private Class<T> getEntityType() {
        return this.entityClass;
    }
}
