package edu.pku.sei.tsr.snowgraph;

import edu.pku.sei.tsr.snowgraph.repository.SnowGraphRepository;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

public class SnowGraphManager implements SnowGraphRepository {
    private List<SnowGraph> graphs = new ArrayList<>();

    @Override
    public Flux<SnowGraph> getAllGraphs() {
        return Flux.fromIterable(graphs);
    }
}
