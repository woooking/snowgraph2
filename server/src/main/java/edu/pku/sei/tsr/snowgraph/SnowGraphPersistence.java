package edu.pku.sei.tsr.snowgraph;

import edu.pku.sei.tsr.snowgraph.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SnowGraphPersistence implements InitializingBean {
    private static Logger logger = LoggerFactory.getLogger(SnowGraphPersistence.class);

    private static Path configDirPath = Paths.get(System.getProperty("user.home")).resolve(".snowgraph");

    public static Path configDirPathOfGraph(SnowGraph graph) {
        return configDirPath.resolve(graph.getName());
    }

    @Autowired
    private Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;

    @Override
    public void afterPropertiesSet() {
        FileUtils.createDirectory(configDirPath);
    }

    public void saveGraph(SnowGraph graph) {
        var graphDirPath = configDirPath.resolve(graph.getName());
        FileUtils.createDirectory(graphDirPath);
        var objectMapper = jackson2ObjectMapperBuilder.build();
        var graphFile = graphDirPath.resolve("info.json").toFile();
        try {
            objectMapper.writeValue(graphFile, graph);
            graph.getPluginInfos().stream()
                .map(SnowGraphPluginInfo::getInstance)
                .map(Object::getClass)
                .map(Object::toString)
                .map(graphDirPath::resolve)
                .forEach(FileUtils::createDirectory);
        } catch (IOException e) {
            logger.error("Error occurred when saving graph {}", graph.getName());
            logger.error("", e);
        }
    }

    public List<SnowGraph> loadGraphs() {
        try {
            return Files.list(configDirPath)
                .map(this::loadGraph)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return List.of();
    }

    private Optional<SnowGraph> loadGraph(Path graphDirPath) {
        var graphFile = graphDirPath.resolve("info.json").toFile();
        var objectMapper = jackson2ObjectMapperBuilder.build();
        try {
            return Optional.of(objectMapper.readValue(graphFile, SnowGraph.class));
        } catch (IOException e) {
            logger.error("Error occurred when loading graph", e);
        }
        return Optional.empty();
    }
}
