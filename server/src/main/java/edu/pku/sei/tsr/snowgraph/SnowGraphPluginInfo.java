package edu.pku.sei.tsr.snowgraph;

import edu.pku.sei.tsr.snowgraph.api.ChangeEvent;
import edu.pku.sei.tsr.snowgraph.api.context.SnowGraphContext;
import edu.pku.sei.tsr.snowgraph.api.plugin.SnowGraphPlugin;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SnowGraphPluginInfo {
    private final SnowGraphPluginConfig config;
    private final SnowGraphPlugin instance;
    private final List<String> dataPaths = new ArrayList<>();
    private final List<String> watchPaths = new ArrayList<>();

    public SnowGraphPluginInfo(SnowGraphPluginConfig config, SnowGraphPlugin instance) {
        this.config = config;
        this.instance = instance;
    }

    public SnowGraphPlugin getInstance() {
        return instance;
    }

    public SnowGraphPluginConfig getConfig() {
        return config;
    }

    public void addDataPath(String path, boolean watch) {
        dataPaths.add(path);
        if (watch) watchPaths.add(path);
    }

    public List<String> getDataPaths() {
        return dataPaths;
    }

    public List<String> getWatchPaths() {
        return watchPaths;
    }

    public void run(SnowGraphContext context) {
        instance.run(context);
    }

    public void update(SnowGraphContext context, Collection<ChangeEvent<Path>> changedFiles, Collection<ChangeEvent<Long>> changedNodes, Collection<ChangeEvent<Long>> changedRelationships) {
        instance.update(context, changedFiles, changedNodes, changedRelationships);
    }

    public Subscriber<ChangeEvent<Path>> subcriber() {
        return new BaseSubscriber<>() {
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                request(1);
            }

            @Override
            protected void hookOnNext(ChangeEvent<Path> value) {
//                instance.update(context, Collections.singleton(value));
                request(1);
            }
        };
    }
}
