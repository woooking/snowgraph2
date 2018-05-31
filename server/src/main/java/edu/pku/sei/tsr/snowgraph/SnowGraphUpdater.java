package edu.pku.sei.tsr.snowgraph;

import edu.pku.sei.tsr.snowgraph.api.event.ChangeEvent;
import edu.pku.sei.tsr.snowgraph.api.event.ChangeEventManager;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

public class SnowGraphUpdater extends BaseSubscriber<List<ChangeEvent<Path>>> {
    private static Logger logger = LoggerFactory.getLogger(SnowGraphUpdater.class);

    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        request(1);
    }

    @Override
    protected void hookOnNext(List<ChangeEvent<Path>> value) {
        update(value);
        request(1);
    }

    private void update(List<ChangeEvent<Path>> events) {
        var manager = new ChangeEventManager<Path>();
        events.forEach(manager::addEvent);
    }
}
