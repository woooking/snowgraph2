package edu.pku.sei.tsr.snowgraph.repository;

import edu.pku.sei.tsr.snowgraph.SnowGraph;
import reactor.core.publisher.Flux;

public interface SnowGraphRepository {
    Flux<SnowGraph> getAllGraphs();
}
