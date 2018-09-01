package edu.pku.sei.tsr.snowgraph;

import edu.pku.sei.tsr.snowgraph.api.plugin.SnowGraphPlugin;
import edu.pku.sei.tsr.snowgraph.context.BasicSnowGraphContext;
import edu.pku.sei.tsr.snowgraph.exception.DependenceException;
import edu.pku.sei.tsr.snowgraph.neo4j.BasicNeo4jService;
import edu.pku.sei.tsr.snowgraph.registry.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class SnowGraphFactory {
    private static Logger logger = LoggerFactory.getLogger(SnowGraphFactory.class);

    public static SnowGraph create(String name, String dataDir, String destination, List<SnowGraphPluginConfig> pluginConfigs) {
        var plugins = createPlugins(pluginConfigs);
        var createTime = new Date();
        return create(name, dataDir, destination, plugins, createTime, createTime);
    }

    public static SnowGraph createAndInit(String name, String dataDir, String destination, List<SnowGraphPluginConfig> pluginConfigs) {
        var plugins = createPlugins(pluginConfigs);
        var createTime = new Date();
        var graph = create(name, dataDir, destination, plugins, createTime, createTime);
        graph.init();
        graph.watchFile();
        return graph;
    }

    public static SnowGraph load(String name, String dataDir, String destination, List<SnowGraphPluginConfig> pluginConfigs, Date createTime, Date updateTime) {
        var plugins = createPlugins(pluginConfigs);
        var graph = create(name, dataDir, destination, plugins, createTime, updateTime);
        onLoad(graph, plugins);
        graph.watchFile();
        return graph;
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

    private static <T> void lifeCycle(
        Map<String, SnowGraphPluginInfo> plugins,
        Function<SnowGraphPlugin, Consumer<T>> func,
        LifeCycleRegistry<T> registry
    ) {
        plugins.values()
            .stream()
            .map(SnowGraphPluginInfo::getInstance)
            .forEach(plugin -> func.apply(plugin).accept(registry.viewFor(plugin)));
    }

    private static void preInit(Map<String, SnowGraphPluginInfo> plugins) {
        var preInitRegistry = new SnowGraphPreInitRegistry(plugins);
        lifeCycle(plugins, plugin -> plugin::preInit, preInitRegistry);
    }

    private static void init(Map<String, SnowGraphPluginInfo> plugins) {
        var initRegistry = new SnowGraphInitRegistry(plugins);
        lifeCycle(plugins, plugin -> plugin::init, initRegistry);
    }

    private static void postInit(Map<String, SnowGraphPluginInfo> plugins) {
        var postInitRegistry = new SnowGraphPostInitRegistry(plugins);
        lifeCycle(plugins, plugin -> plugin::postInit, postInitRegistry);
    }

    private static void onLoad(SnowGraph graph, Map<String, SnowGraphPluginInfo> plugins) {
        var loadEventRegistry = new LoadEventRegistry(SnowGraphPersistence.configDirPathOfGraph(graph));
        lifeCycle(plugins, plugin -> plugin::onLoad, loadEventRegistry);
    }

    private static SnowGraph create(
        String name, String dataDir, String destination,
        Map<String, SnowGraphPluginInfo> plugins,
        Date createTime, Date updateTime
    ) {
        preInit(plugins);
        init(plugins);
        postInit(plugins);
        var dependencyGraph = new DependencyGraph(plugins.values());
        return new SnowGraph(name, dataDir, destination, dependencyGraph, createTime, updateTime);
    }
}
