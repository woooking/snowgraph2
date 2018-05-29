package edu.pku.sei.tsr.snowgraph;

import edu.pku.sei.tsr.snowgraph.api.ChangeEvent;
import edu.pku.sei.tsr.snowgraph.api.context.SnowGraphContext;
import edu.pku.sei.tsr.snowgraph.api.plugin.SnowGraphPlugin;
import edu.pku.sei.tsr.snowgraph.context.BasicSnowGraphContext;
import edu.pku.sei.tsr.snowgraph.exception.DependenceException;
import edu.pku.sei.tsr.snowgraph.registry.LifeCycleRegistry;
import edu.pku.sei.tsr.snowgraph.registry.SnowGraphInitRegistry;
import edu.pku.sei.tsr.snowgraph.registry.SnowGraphPostInitRegistry;
import edu.pku.sei.tsr.snowgraph.registry.SnowGraphPreInitRegistry;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SnowGraph {
    private static Logger logger = LoggerFactory.getLogger(SnowGraph.class);

    private final String name;
    private final String dataDir;
    private final String destination;
    private final DependencyGraph dependencyGraph;
    private final FileWatcher fileWatcher;
    private final SnowGraphUpdater updater;
    private final GraphDatabaseBuilder databaseBuilder;

    private SnowGraph(String name, String dataDir, String destination, DependencyGraph dependencyGraph) {
        this.name = name;
        this.dataDir = dataDir;
        this.destination = destination;
        this.dependencyGraph = dependencyGraph;
        this.fileWatcher = new FileWatcher(dataDir);
        this.updater = new SnowGraphUpdater();
        this.databaseBuilder = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(destination));
    }

    public String getName() {
        return name;
    }

    public String getDataDir() {
        return dataDir;
    }

    public String getDestination() {
        return destination;
    }

    public GraphDatabaseBuilder getDatabaseBuilder() {
        return databaseBuilder;
    }

    public List<SnowGraphPluginConfig> getPlugins() {
        return dependencyGraph.getSortedPlugins().stream()
            .map(SnowGraphPluginInfo::getConfig)
            .collect(Collectors.toList());
    }

    private void watchFile() {
        var publisher = fileWatcher.getPublisher();
        publisher.publishOn(Schedulers.elastic()).subscribe(updater);
    }

    public void update(List<ChangeEvent<Path>> changedFiles) {
        var changedNodes = new ArrayList<ChangeEvent<Long>>();
        var changedRelationships = new ArrayList<ChangeEvent<Long>>();
        for (SnowGraphPluginInfo plugin : dependencyGraph.getSortedPlugins()) {
            logger.info("{} started.", plugin.getInstance().getClass().getName());
            long startTime = System.currentTimeMillis();
            try (var context = new BasicSnowGraphContext(this, plugin, databaseBuilder)) {
                plugin.update(context, changedFiles, changedNodes, changedRelationships);
            }
            long endTime = System.currentTimeMillis();
            logger.info("{} uses {} s.", plugin.getClass().getName(), (endTime - startTime) / 1000);
        }
    }

    public static class Builder implements org.apache.commons.lang3.builder.Builder<SnowGraph> {
        private static Logger logger = LoggerFactory.getLogger(SnowGraph.Builder.class);

        private final String name;
        private final String srcDir;
        private final String destination;
        private final Map<String, SnowGraphPluginInfo> plugins = new HashMap<>();

        private final SnowGraphPreInitRegistry preInitRegistry = new SnowGraphPreInitRegistry(plugins);
        private final SnowGraphInitRegistry initRegistry = new SnowGraphInitRegistry(plugins);
        private final SnowGraphPostInitRegistry postInitRegistry = new SnowGraphPostInitRegistry(plugins);

        public Builder(String name, String srcDir, String destination, List<SnowGraphPluginConfig> pluginConfigs) {
            this.name = name;
            this.srcDir = srcDir;
            this.destination = destination;
            for (SnowGraphPluginConfig pluginConfig : pluginConfigs) {
                try {
                    var instance = (SnowGraphPlugin) Class.forName(pluginConfig.getPath()).getConstructor().newInstance();
                    this.plugins.put(pluginConfig.getPath(), new SnowGraphPluginInfo(pluginConfig, instance));
                } catch (InstantiationException
                    | InvocationTargetException
                    | NoSuchMethodException
                    | IllegalAccessException
                    | ClassNotFoundException
                    | ClassCastException e) {
                    throw DependenceException.initializeError(pluginConfig.getPath());
                }
            }
        }

        private <T> void lifeCycle(Function<SnowGraphPlugin, Consumer<T>> func, LifeCycleRegistry<T> registry) {
            plugins.values()
                .stream()
                .map(SnowGraphPluginInfo::getInstance)
                .forEach(plugin -> func.apply(plugin).accept(registry.viewFor(plugin)));
        }

        private void preInit() {
            lifeCycle(plugin -> plugin::preInit, preInitRegistry);
        }

        private void init() {
            lifeCycle(plugin -> plugin::init, initRegistry);
        }

        private void postInit() {
            lifeCycle(plugin -> plugin::postInit, postInitRegistry);
        }

        @Override
        public SnowGraph build() {
            preInit();
            init();
            postInit();
            var dependencyGraph = new DependencyGraph(plugins.values());
            var snowGraph = new SnowGraph(name, srcDir, destination, dependencyGraph);
            dependencyGraph.getSortedPlugins().forEach(plugin -> {
                logger.info("{} started.", plugin.getInstance().getClass().getName());
                long startTime = System.currentTimeMillis();
                try(var context = new BasicSnowGraphContext(snowGraph, plugin, snowGraph.getDatabaseBuilder())) {
                    plugin.run(context);
                }
                long endTime = System.currentTimeMillis();
                logger.info("{} uses {} s.", plugin.getClass().getName(), (endTime - startTime) / 1000);
            });
            snowGraph.watchFile();
            return snowGraph;
        }
    }
}
