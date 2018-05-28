package edu.pku.sei.tsr.snowgraph;

import edu.pku.sei.tsr.snowgraph.api.context.SnowGraphContext;
import edu.pku.sei.tsr.snowgraph.api.context.SnowGraphDBContext;
import edu.pku.sei.tsr.snowgraph.api.context.SnowGraphOGMContext;
import edu.pku.sei.tsr.snowgraph.api.plugin.SnowGraphDBPlugin;
import edu.pku.sei.tsr.snowgraph.api.plugin.SnowGraphOGMPlugin;
import edu.pku.sei.tsr.snowgraph.api.plugin.SnowGraphPlugin;
import edu.pku.sei.tsr.snowgraph.context.BasicSnowGraphDBContext;
import edu.pku.sei.tsr.snowgraph.context.BasicSnowGraphOGMContext;
import edu.pku.sei.tsr.snowgraph.exception.DependenceException;
import edu.pku.sei.tsr.snowgraph.neo4j.Neo4jSessionFactory;
import edu.pku.sei.tsr.snowgraph.registry.LifeCycleRegistry;
import edu.pku.sei.tsr.snowgraph.registry.SnowGraphInitRegistry;
import edu.pku.sei.tsr.snowgraph.registry.SnowGraphPostInitRegistry;
import edu.pku.sei.tsr.snowgraph.registry.SnowGraphPreInitRegistry;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SnowGraph {
    private final String name;
    private final String dataDir;
    private final String destination;
    private final DependencyGraph dependencyGraph;
    private final FileWatcher fileWatcher;
    private final SessionFactory sessionFactory;
    private final SnowGraphUpdater updater;

    private SnowGraph(String name, String dataDir, String destination, DependencyGraph dependencyGraph, SessionFactory sessionFactory) {
        this.name = name;
        this.dataDir = dataDir;
        this.destination = destination;
        this.dependencyGraph = dependencyGraph;
        this.fileWatcher = new FileWatcher(dataDir);
        this.sessionFactory = sessionFactory;
        this.updater = new SnowGraphUpdater();
    }

    String getName() {
        return name;
    }

    public String getDataDir() {
        return dataDir;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    private void watchFile() {
        var publisher = fileWatcher.getPublisher();
        publisher.publishOn(Schedulers.elastic()).subscribe(updater);
    }

    public static class Builder implements org.apache.commons.lang3.builder.Builder<SnowGraph> {
        private static Logger logger = LoggerFactory.getLogger(SnowGraph.Builder.class);

        private final String name;
        private final String srcDir;
        private final String destination;
        private final Map<String, SnowGraphPluginInfo<?>> plugins = new HashMap<>();

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
                    if (instance instanceof SnowGraphOGMPlugin) {
                        var plugin = (SnowGraphOGMPlugin) instance;
                        this.plugins.put(pluginConfig.getPath(), new SnowGraphPluginInfo<>(SnowGraphOGMContext.class, pluginConfig, plugin));
                    } else if (instance instanceof SnowGraphDBPlugin) {
                        var plugin = (SnowGraphDBPlugin) instance;
                        this.plugins.put(pluginConfig.getPath(), new SnowGraphPluginInfo<>(SnowGraphDBContext.class, pluginConfig, plugin));
                    }
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

        private SessionFactory createSessionFactory() {
            List<String> entityPackages = plugins.values().stream()
                .map(SnowGraphPluginInfo::getInstance)
                .filter(SnowGraphOGMPlugin.class::isInstance)
                .map(SnowGraphOGMPlugin.class::cast)
                .map(SnowGraphOGMPlugin::entityPackage)
                .flatMap(List::stream)
                .collect(Collectors.toList());
            return Neo4jSessionFactory.createSessionFactory(destination, entityPackages);
        }

        @Override
        public SnowGraph build() {
            preInit();
            init();
            postInit();
            var dependencyGraph = new DependencyGraph(plugins.values());
            var sessionFactory = createSessionFactory();
            var snowGraph = new SnowGraph(name, srcDir, destination, dependencyGraph, sessionFactory);
            var databaseBuilder = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(destination));
            plugins.values().forEach(plugin -> {
                if (plugin.getContextClass().equals(SnowGraphOGMContext.class)) {
                    var p = castPluginInfo(plugin, SnowGraphOGMContext.class);
                    p.setContext(new BasicSnowGraphOGMContext(snowGraph, p, sessionFactory));
                } else if (plugin.getContextClass().equals(SnowGraphDBContext.class)) {
                    var p = castPluginInfo(plugin, SnowGraphDBContext.class);
                    p.setContext(new BasicSnowGraphDBContext(snowGraph, p, databaseBuilder));
                }
            });
            dependencyGraph.getSortedPlugins().forEach(plugin -> {
                logger.info("{} started.", plugin.getInstance().getClass().getName());
                long startTime = System.currentTimeMillis();
                plugin.run();
                long endTime = System.currentTimeMillis();
                logger.info("{} uses {} s.", plugin.getClass().getName(), (endTime - startTime) / 1000);
            });
            snowGraph.watchFile();
            return snowGraph;
        }

        @SuppressWarnings("unchecked")
        private <C extends SnowGraphContext> SnowGraphPluginInfo<C> castPluginInfo(SnowGraphPluginInfo<?> pluginInfo, Class<C> clazz) {
            assert pluginInfo.getContextClass().equals(clazz);
            return (SnowGraphPluginInfo<C>) pluginInfo;
        }
    }
}
