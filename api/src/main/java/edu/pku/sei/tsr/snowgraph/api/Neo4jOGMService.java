package edu.pku.sei.tsr.snowgraph.api;

import java.util.Collection;

public interface Neo4jOGMService<T> {

    long count();

    Iterable<T> findAll();

    T find(Long id);

    void delete(Long id);

    T save(T object);

    Collection<T> saveAll(Collection<T> objects);
}
