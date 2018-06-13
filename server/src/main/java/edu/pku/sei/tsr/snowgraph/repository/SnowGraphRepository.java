package edu.pku.sei.tsr.snowgraph.repository;

import edu.pku.sei.tsr.snowgraph.SnowGraph;
import edu.pku.sei.tsr.snowgraph.SnowGraphPluginConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface SnowGraphRepository {
    Flux<SnowGraph> getAllGraphs();

    Mono<SnowGraph> createGraph(String name, String dataDir, String destination, List<SnowGraphPluginConfig> pluginConfigs);
}
