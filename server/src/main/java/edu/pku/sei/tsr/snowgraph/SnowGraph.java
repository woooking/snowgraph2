package edu.pku.sei.tsr.snowgraph;

import edu.pku.sei.tsr.snowgraph.api.SnowGraphPlugin;
import edu.pku.sei.tsr.snowgraph.neo4j.Neo4jSessionFactory;
import edu.pku.sei.tsr.snowgraph.registry.LifeCycleRegistry;
import edu.pku.sei.tsr.snowgraph.registry.SnowGraphInitRegistry;
import edu.pku.sei.tsr.snowgraph.registry.SnowGraphPostInitRegistry;
import edu.pku.sei.tsr.snowgraph.registry.SnowGraphPreInitRegistry;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SnowGraph {
    private final String name;
    private final String destination;
    private final Collection<SnowGraphPluginInfo> plugins;

    private SnowGraph(String name, String destination, Collection<SnowGraphPluginInfo> plugins) {
        this.name = name;
        this.destination = destination;
        this.plugins = plugins;
    }

    String getName() {
        return name;
    }

    public static class Builder implements org.apache.commons.lang3.builder.Builder<SnowGraph> {
        private static Logger logger = LoggerFactory.getLogger(SnowGraph.Builder.class);

        private final String name;
        private final String destination;
        private final Map<String, SnowGraphPluginInfo> plugins = new HashMap<>();

        private final SnowGraphPreInitRegistry preInitRegistry = new SnowGraphPreInitRegistry(plugins);
        private final SnowGraphInitRegistry initRegistry = new SnowGraphInitRegistry(plugins);
        private final SnowGraphPostInitRegistry postInitRegistry = new SnowGraphPostInitRegistry(plugins);

        public Builder(String name, String destination, List<SnowGraphPluginConfig> pluginConfigs) {
            this.name = name;
            this.destination = destination;
            pluginConfigs.forEach(config -> this.plugins.put(config.getPath(), new SnowGraphPluginInfo(config)));
        }

        private Optional<?> forName(String className) {
            try {
                return Optional.of(Class.forName(className).getConstructor().newInstance());
            } catch (ClassNotFoundException
                | NoSuchMethodException
                | InstantiationException
                | IllegalAccessException
                | InvocationTargetException e
                ) {
                e.printStackTrace();
            }
            return Optional.empty();
        }

        private <T> void lifeCycle(Function<SnowGraphPlugin, Consumer<T>> func, LifeCycleRegistry<T> registry) {
            plugins.values()
                .stream()
                .map(SnowGraphPluginInfo::getInstance)
                .forEach(plugin -> func.apply(plugin).accept(registry.viewFor(plugin)));
        }

        private void buildInstances() {
            plugins.keySet().stream()
                .map(this::forName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(SnowGraphPlugin.class::isInstance)
                .map(SnowGraphPlugin.class::cast)
                .forEach(plugin -> plugins.get(plugin.getClass().getName()).setInstance(plugin));
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
                .map(SnowGraphPlugin::entityPackage)
                .flatMap(List::stream)
                .collect(Collectors.toList());
            return Neo4jSessionFactory.createSessionFactory(destination, entityPackages);
        }

        @Override
        public SnowGraph build() {
            buildInstances();
            preInit();
            init();
            postInit();
            var sessionFactory = createSessionFactory();
            plugins.values().forEach(plugin -> {
                logger.info("{} started.", plugin.getClass().getName());
                long startTime = System.currentTimeMillis();
                plugin.getInstance().run(new BasicSnowGraphContext(sessionFactory, plugin.getClass()));
                long endTime = System.currentTimeMillis();
                logger.info("{} uses {} s.", plugin.getClass().getName(), (endTime - startTime) / 1000);
            });
            return new SnowGraph(name, destination, plugins.values());
        }
    }
}
