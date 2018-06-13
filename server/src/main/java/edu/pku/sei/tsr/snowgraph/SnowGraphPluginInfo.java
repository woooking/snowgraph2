package edu.pku.sei.tsr.snowgraph;

import edu.pku.sei.tsr.snowgraph.api.event.ChangeEvent;
import edu.pku.sei.tsr.snowgraph.api.context.SnowGraphContext;
import edu.pku.sei.tsr.snowgraph.api.plugin.SnowGraphPlugin;
import lombok.Getter;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SnowGraphPluginInfo {
    @Getter private final SnowGraphPluginConfig config;
    @Getter private final SnowGraphPlugin instance;
    @Getter private final List<String> dataPaths = new ArrayList<>();
    @Getter private final List<String> watchPaths = new ArrayList<>();

    public SnowGraphPluginInfo(SnowGraphPluginConfig config, SnowGraphPlugin instance) {
        this.config = config;
        this.instance = instance;
    }

    public void addDataPath(String path, boolean watch) {
        dataPaths.add(path);
        if (watch) watchPaths.add(path);
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
