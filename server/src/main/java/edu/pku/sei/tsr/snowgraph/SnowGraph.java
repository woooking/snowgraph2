package edu.pku.sei.tsr.snowgraph;

import com.google.common.collect.ImmutableList;
import edu.pku.sei.tsr.snowgraph.api.event.ChangeEvent;
import edu.pku.sei.tsr.snowgraph.api.event.ChangeEventManager;
import edu.pku.sei.tsr.snowgraph.api.plugin.SnowGraphPlugin;
import edu.pku.sei.tsr.snowgraph.context.BasicSnowGraphContext;
import edu.pku.sei.tsr.snowgraph.exception.DependenceException;
import edu.pku.sei.tsr.snowgraph.neo4j.BasicNeo4jService;
import edu.pku.sei.tsr.snowgraph.neo4j.ChangeEventNeo4jService;
import edu.pku.sei.tsr.snowgraph.registry.*;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class SnowGraph {
    private static Logger logger = LoggerFactory.getLogger(SnowGraph.class);

    private final String name;
    private final String dataDir;
    private final String destination;
    private final Date createTime;
    private final DependencyGraph dependencyGraph;
    private final FileWatcher fileWatcher;
    private final SnowGraphUpdater updater;
    private final GraphDatabaseBuilder databaseBuilder;

    private Date updateTime;

    private SnowGraph(String name, String dataDir, String destination, DependencyGraph dependencyGraph, Date createTime, Date updateTime) {
        this.name = name;
        this.dataDir = dataDir;
        this.destination = destination;
        this.dependencyGraph = dependencyGraph;
        this.fileWatcher = new FileWatcher(dataDir);
        this.updater = new SnowGraphUpdater();
        this.databaseBuilder = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(destination));
        this.createTime = createTime;
        this.updateTime = updateTime;
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

    public List<SnowGraphPluginInfo> getPluginInfos() {
        return ImmutableList.copyOf(dependencyGraph.getSortedPlugins());
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    private void watchFile() {
        var publisher = fileWatcher.getPublisher();
        publisher.publishOn(Schedulers.elastic()).subscribe(updater);
    }

    public Pair<ChangeEventManager<Long>, ChangeEventManager<Long>> update(List<ChangeEvent<Path>> changedFiles) {
        var changedNodes = new ChangeEventManager<Long>();
        var changedRelationships = new ChangeEventManager<Long>();
        for (SnowGraphPluginInfo plugin : dependencyGraph.getSortedPlugins()) {
            logger.info("{} started.", plugin.getInstance().getClass().getName());
            // TODO: 只传入plugin注册的data dir下的Path
            var startTime = System.currentTimeMillis();
            var neo4jService = new ChangeEventNeo4jService(databaseBuilder.newGraphDatabase());
            try (var context = new BasicSnowGraphContext(this, plugin, neo4jService)) {
                plugin.update(context, changedFiles, changedNodes.getChanges(), changedRelationships.getChanges());
            }
            var endTime = System.currentTimeMillis();
            logger.info("{} uses {} s.", plugin.getClass().getName(), (endTime - startTime) / 1000);
            neo4jService.getChangedNodes().forEach(changedNodes::addEvent);
            neo4jService.getChangedRelationships().forEach(changedRelationships::addEvent);
        }
        return Pair.of(changedNodes, changedRelationships);
    }

    public static class Builder implements org.apache.commons.lang3.builder.Builder<SnowGraph> {
        private static Logger logger = LoggerFactory.getLogger(SnowGraph.Builder.class);

        private final String name;
        private final String dataDir;
        private final String destination;
        private final Map<String, SnowGraphPluginInfo> plugins;
        private final boolean load;
        private final Date createTime;
        private final Date updateTime;

        public Builder(String name, String dataDir, String destination, List<SnowGraphPluginConfig> pluginConfigs) {
            this.name = name;
            this.dataDir = dataDir;
            this.destination = destination;
            this.plugins = createPlugins(pluginConfigs);
            this.load = false;
            this.createTime = new Date();
            this.updateTime = this.createTime;
        }

        public Builder(String name, String dataDir, String destination, List<SnowGraphPluginConfig> pluginConfigs, Date createTime, Date updateTime) {
            this.name = name;
            this.dataDir = dataDir;
            this.destination = destination;
            this.plugins = createPlugins(pluginConfigs);
            this.load = true;
            this.createTime = createTime;
            this.updateTime = updateTime;
        }

        private static Map<String, SnowGraphPluginInfo> createPlugins(List<SnowGraphPluginConfig> pluginConfigs) {
            var plugins = new HashMap<String, SnowGraphPluginInfo>();
            for (SnowGraphPluginConfig pluginConfig : pluginConfigs) {
                try {
                    var instance = (SnowGraphPlugin) Class.forName(pluginConfig.getPath()).getConstructor().newInstance();
                    plugins.put(pluginConfig.getPath(), new SnowGraphPluginInfo(pluginConfig, instance));
                } catch (InstantiationException
                    | InvocationTargetException
                    | NoSuchMethodException
                    | IllegalAccessException
                    | ClassNotFoundException
                    | ClassCastException e) {
                    logger.error("Could not initialize plugin!", e);
                    throw DependenceException.initializeError(pluginConfig.getPath());
                }
            }
            return plugins;
        }

        private <T> void lifeCycle(Function<SnowGraphPlugin, Consumer<T>> func, LifeCycleRegistry<T> registry) {
            plugins.values()
                .stream()
                .map(SnowGraphPluginInfo::getInstance)
                .forEach(plugin -> func.apply(plugin).accept(registry.viewFor(plugin)));
        }

        private void preInit() {
            var preInitRegistry = new SnowGraphPreInitRegistry(plugins);
            lifeCycle(plugin -> plugin::preInit, preInitRegistry);
        }

        private void init() {
            var initRegistry = new SnowGraphInitRegistry(plugins);
            lifeCycle(plugin -> plugin::init, initRegistry);
        }

        private void postInit() {
            var postInitRegistry = new SnowGraphPostInitRegistry(plugins);
            lifeCycle(plugin -> plugin::postInit, postInitRegistry);
        }

        private void onLoad() {
            var loadEventRegistry = new LoadEventRegistry(plugins);
            lifeCycle(plugin -> plugin::onLoad, loadEventRegistry);
        }

        @Override
        public SnowGraph build() {
            preInit();
            init();
            postInit();
            if (load) onLoad();
            var dependencyGraph = new DependencyGraph(plugins.values());
            var snowGraph = new SnowGraph(name, dataDir, destination, dependencyGraph, createTime, updateTime);
            if (!load) {
                dependencyGraph.getSortedPlugins().forEach(plugin -> {
                    logger.info("{} started.", plugin.getInstance().getClass().getName());
                    long startTime = System.currentTimeMillis();
                    try (var context = new BasicSnowGraphContext(snowGraph, plugin, new BasicNeo4jService(snowGraph.getDatabaseBuilder().newGraphDatabase()))) {
                        plugin.run(context);
                    }
                    long endTime = System.currentTimeMillis();
                    logger.info("{} uses {} s.", plugin.getClass().getName(), (endTime - startTime) / 1000);
                });
            }
            snowGraph.watchFile();
            return snowGraph;
        }
    }
}
