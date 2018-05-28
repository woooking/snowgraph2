package edu.pku.sei.tsr.snowgraph.api;

public interface Neo4jOGMServiceFactory {
    <T extends GraphEntity> Neo4jOGMService<T> createService(Class<T> entityClass);
}
