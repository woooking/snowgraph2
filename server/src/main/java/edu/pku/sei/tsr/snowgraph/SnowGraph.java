package edu.pku.sei.tsr.snowgraph;

import com.google.common.collect.ImmutableList;
import edu.pku.sei.tsr.snowgraph.api.event.ChangeEvent;
import edu.pku.sei.tsr.snowgraph.api.event.ChangeEventManager;
import edu.pku.sei.tsr.snowgraph.context.BasicSnowGraphContext;
import edu.pku.sei.tsr.snowgraph.neo4j.BasicNeo4jService;
import edu.pku.sei.tsr.snowgraph.neo4j.ChangeEventNeo4jService;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

public class SnowGraph {
    private static Logger logger = LoggerFactory.getLogger(SnowGraph.class);

    @Getter private final String name;
    @Getter private final String dataDir;
    @Getter private final String destination;
    @Getter private final Date createTime;
    private final DependencyGraph dependencyGraph;
    private final FileWatcher fileWatcher;
    private final SnowGraphUpdater updater;
    @Getter private final GraphDatabaseBuilder databaseBuilder;

    @Getter private Date updateTime;

    SnowGraph(String name, String dataDir, String destination, DependencyGraph dependencyGraph, Date createTime, Date updateTime) {
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

    public void init() {
        dependencyGraph.getSortedPlugins().forEach(plugin -> {
            logger.info("{} started.", plugin.getInstance().getClass().getName());
            long startTime = System.currentTimeMillis();
            try (var context = new BasicSnowGraphContext(this, plugin, new BasicNeo4jService(getDatabaseBuilder().newGraphDatabase()))) {
                plugin.run(context);
            }
            long endTime = System.currentTimeMillis();
            logger.info("{} uses {} s.", plugin.getClass().getName(), (endTime - startTime) / 1000);
        });
    }

    public List<SnowGraphPluginInfo> getPluginInfos() {
        return ImmutableList.copyOf(dependencyGraph.getSortedPlugins());
    }

    void watchFile() {
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

}
