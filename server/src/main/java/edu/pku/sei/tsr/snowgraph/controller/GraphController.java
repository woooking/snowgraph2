package edu.pku.sei.tsr.snowgraph.controller;

import edu.pku.sei.tsr.snowgraph.SnowGraphPluginConfig;
import edu.pku.sei.tsr.snowgraph.SnowGraph;
import edu.pku.sei.tsr.snowgraph.repository.SnowGraphRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class GraphController {
    private final SnowGraphRepository snowGraphRepository;

    @Autowired
    public GraphController(SnowGraphRepository snowGraphRepository) {
        this.snowGraphRepository = snowGraphRepository;
    }

    @GetMapping("/graphs")
    public Flux<SnowGraph> hello() {
        return snowGraphRepository.getAllGraphs();
    }

    @PostMapping("/graphs")
    public Mono<SnowGraph> build(String name, String destination, List<SnowGraphPluginConfig> pluginConfigs) {
        return Mono.create(sink -> sink.success(new SnowGraph.Builder(name, destination, pluginConfigs).build()));
    }


}
