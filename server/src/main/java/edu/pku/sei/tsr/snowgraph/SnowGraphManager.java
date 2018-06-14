package edu.pku.sei.tsr.snowgraph;

import edu.pku.sei.tsr.snowgraph.registry.ShutDownEventRegistry;
import edu.pku.sei.tsr.snowgraph.repository.SnowGraphRepository;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class SnowGraphManager implements SnowGraphRepository, InitializingBean, DisposableBean {
    @Autowired private SnowGraphPersistence snowGraphPersistence;

    private List<SnowGraph> graphs = new ArrayList<>();

    @Override
    public Flux<SnowGraph> getAllGraphs() {
        return Flux.fromIterable(graphs);
    }

    @Override
    public Mono<SnowGraph> createGraph(String name, String dataDir, String destination, List<SnowGraphPluginConfig> pluginConfigs) {
        var graph = SnowGraphFactory.create(name, dataDir, destination, pluginConfigs);
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
            var shutDownEventRegistry = new ShutDownEventRegistry(SnowGraphPersistence.configDirPathOfGraph(graph));
            graph.getPluginInfos().forEach(p -> p.getInstance().onShutDown(shutDownEventRegistry.viewFor(p.getInstance())));
        });
    }
}
