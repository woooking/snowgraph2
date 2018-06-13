package edu.pku.sei.tsr.snowgraph.webflux.controller;

import edu.pku.sei.tsr.snowgraph.SnowGraphPluginConfig;
import edu.pku.sei.tsr.snowgraph.SnowGraph;
import edu.pku.sei.tsr.snowgraph.repository.SnowGraphRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

class CreateGraphParam {
    private String name;
    private String srcDir;
    private String destination;
    private List<SnowGraphPluginConfig> pluginConfigs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSrcDir() {
        return srcDir;
    }

    public void setSrcDir(String srcDir) {
        this.srcDir = srcDir;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public List<SnowGraphPluginConfig> getPluginConfigs() {
        return pluginConfigs;
    }

    public void setPluginConfigs(List<SnowGraphPluginConfig> pluginConfigs) {
        this.pluginConfigs = pluginConfigs;
    }
}

@CrossOrigin
@RestController
public class GraphController {
    private final SnowGraphRepository snowGraphRepository;

    @Autowired
    public GraphController(SnowGraphRepository snowGraphRepository) {
        this.snowGraphRepository = snowGraphRepository;
    }

    @GetMapping("/graphs")
    public Flux<SnowGraph> graphs() {
        return snowGraphRepository.getAllGraphs();
    }

    @PostMapping("/graphs")
    public Mono<SnowGraph> build(@RequestBody Mono<CreateGraphParam> paramMono) {
        return paramMono.flatMap(param -> snowGraphRepository.createGraph(
            param.getName(),
            param.getSrcDir(),
            param.getDestination(),
            param.getPluginConfigs()
        ));
    }


}
