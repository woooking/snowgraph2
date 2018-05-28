package edu.pku.sei.tsr.snowgraph;

import edu.pku.sei.tsr.snowgraph.api.ChangeEvent;
import edu.pku.sei.tsr.snowgraph.api.context.SnowGraphContext;
import edu.pku.sei.tsr.snowgraph.api.plugin.SnowGraphPlugin;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SnowGraphPluginInfo<C extends SnowGraphContext> {
    private final Class<? extends SnowGraphContext> contextClass;
    private final SnowGraphPluginConfig config;
    private final SnowGraphPlugin<C> instance;
    private final List<String> dataPaths = new ArrayList<>();
    private final List<String> watchPaths = new ArrayList<>();
    private C context;

    public SnowGraphPluginInfo(Class<? extends SnowGraphContext> contextClass, SnowGraphPluginConfig config, SnowGraphPlugin<C> instance) {
        this.contextClass = contextClass;
        this.config = config;
        this.instance = instance;
    }

    public Class<? extends SnowGraphContext> getContextClass() {
        return contextClass;
    }

    public SnowGraphPlugin<C> getInstance() {
        return instance;
    }

    public void setContext(C context) {
        this.context = context;
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

    public void run() {
        instance.run(context);
    }

    public Subscriber<ChangeEvent<Path>> subcriber() {
        return new BaseSubscriber<>() {
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                request(1);
            }

            @Override
            protected void hookOnNext(ChangeEvent<Path> value) {
                instance.update(context, Collections.singleton(value));
                request(1);
            }
        };
    }
}
