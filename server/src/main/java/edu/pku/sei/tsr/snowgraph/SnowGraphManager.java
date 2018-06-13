package edu.pku.sei.tsr.snowgraph;

import edu.pku.sei.tsr.snowgraph.api.event.ShutDownEvent;
import edu.pku.sei.tsr.snowgraph.repository.SnowGraphRepository;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SnowGraphManager implements SnowGraphRepository, InitializingBean, DisposableBean {
    @Autowired
    private SnowGraphPersistence snowGraphPersistence;

    private List<SnowGraph> graphs = new ArrayList<>();

    @Override
    public Flux<SnowGraph> getAllGraphs() {
        return Flux.fromIterable(graphs);
    }

    @Override
    public Mono<SnowGraph> createGraph(String name, String dataDir, String destination, List<SnowGraphPluginConfig> pluginConfigs) {
        var graph = new SnowGraph.Builder(name, dataDir, destination, pluginConfigs).build();
        graphs.add(graph);
        return Mono.just(graph);
    }

    @Override
    public void afterPropertiesSet() {
        graphs = snowGraphPersistence.loadGraphs();
    }

    @Override
    public void destroy() {
        graphs.forEach(graph -> {
            snowGraphPersistence.saveGraph(graph);
            graph.getPluginInfos().forEach(p -> p.getInstance().onShutDown(new ShutDownEvent() {
            }));
        });
    }
}
