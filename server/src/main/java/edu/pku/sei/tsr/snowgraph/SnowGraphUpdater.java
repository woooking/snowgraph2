package edu.pku.sei.tsr.snowgraph;

import edu.pku.sei.tsr.snowgraph.api.ChangeEvent;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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
        var state = new HashMap<Path, ChangeEvent.Type>();
        for (var event : events) {
            var type = event.getType();
            var path = event.getInstance();
            if (state.containsKey(path)) {
                var newState = transform(state.get(path), type);
                if (newState.isPresent()) state.put(path, newState.get());
                else state.remove(path);
            } else {
                state.put(path, type);
            }
        }
    }

    private Optional<ChangeEvent.Type> transform(ChangeEvent.Type old, ChangeEvent.Type next) {
        switch (old) {
            case CREATED:
                switch (next) {
                    case CREATED:
                        logger.warn("File created twice!");
                        return Optional.of(ChangeEvent.Type.CREATED);
                    case MODIFIED:
                        return Optional.of(ChangeEvent.Type.CREATED);
                    case DELETED:
                        return Optional.empty();
                }
            case MODIFIED:
                switch (next) {
                    case CREATED:
                        logger.warn("Creating an existed file!");
                        return Optional.of(ChangeEvent.Type.MODIFIED);
                    case MODIFIED:
                        return Optional.of(ChangeEvent.Type.MODIFIED);
                    case DELETED:
                        return Optional.of(ChangeEvent.Type.DELETED);
                }
            case DELETED:
                switch (next) {
                    case CREATED:
                        return Optional.of(ChangeEvent.Type.CREATED);
                    case MODIFIED:
                        logger.warn("Modifying a deleted file!");
                        return Optional.of(ChangeEvent.Type.DELETED);
                    case DELETED:
                        logger.warn("File deleted twice!");
                        return Optional.of(ChangeEvent.Type.DELETED);
                }
        }
        return Optional.empty();
    }
}
