package edu.pku.sei.tsr.snowgraph;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import edu.pku.sei.tsr.snowgraph.api.SnowGraphPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystems;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SnowGraph {
    private static Logger logger = LoggerFactory.getLogger(SnowGraph.class);

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

    private List<SnowGraphPlugin> preInit() throws IOException {
        var mapper = new ObjectMapper(new YAMLFactory());
        var config = mapper.readValue(new File("/home/woooking/java/snowgraph2/server/src/main/resources/plugins.yml"), SnowGraphConfig.class);
        return config.getPlugins().stream().map(p -> {
            var plugin = forName(p.getPath());
            return plugin.flatMap(
                pluginInstance -> pluginInstance instanceof SnowGraphPlugin ? Optional.of((SnowGraphPlugin) pluginInstance) : Optional.empty()
            ).map(pluginInstance -> {
                pluginInstance.preInit();
                pluginInstance.init(p.getArgs());
                return pluginInstance;
            });
        })
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    private void buildGragh() throws IOException {
        var plugins = preInit();
        plugins.forEach(plugin -> {
            logger.info("{} started.", plugin.getClass().getName());
            long startTime = System.currentTimeMillis();
            plugin.run(new BasicSnowGraphContext(plugin.getClass()));
            long endTime = System.currentTimeMillis();
            logger.info("{} uses {} s.", plugin.getClass().getName(), (endTime - startTime) / 1000);
        });
    }

    private void watchForChanges() throws IOException {
        var watcher = FileSystems.getDefault().newWatchService();
    }

    private void run() throws IOException {
        buildGragh();
    }

    public static void main(String[] args) throws IOException {
//        new SnowGraph().run();
        System.out.println(System.getProperty("user.home"));
    }
}
