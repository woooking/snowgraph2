package edu.pku.sei.tsr.snowgraph.api;

public interface Neo4jServiceFactory {
    <T extends GraphEntity> Neo4jService<T> createService(Class<T> entityClass);
}
