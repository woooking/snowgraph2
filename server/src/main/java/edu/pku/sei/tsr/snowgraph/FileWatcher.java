package edu.pku.sei.tsr.snowgraph;

import com.google.common.collect.Lists;
import edu.pku.sei.tsr.snowgraph.api.event.ChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher {
    private static Logger logger = LoggerFactory.getLogger(FileWatcher.class);

    private WatchService watcher;
    private final Map<WatchKey, Path> keys = new HashMap<>();
    private final Flux<List<ChangeEvent<Path>>> publisher;

    @SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    FileWatcher(String dataRoot) {
        try {
            this.watcher = FileSystems.getDefault().newWatchService();
            registerDir(Paths.get(dataRoot));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.publisher = Flux.create(this::processEvents).subscribeOn(Schedulers.elastic());
    }

    public Flux<List<ChangeEvent<Path>>> getPublisher() {
        return publisher;
    }

    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
    }

    private void registerDir(Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private List<ChangeEvent<Path>> watchKey2events(WatchKey key) {
        var dir = keys.get(key);
        var result = key.pollEvents().stream()
            .filter(e -> e.kind() != OVERFLOW)
            .map(FileWatcher::<Path>cast)
            .map(e -> {
                var name = e.context();
                var child = dir.resolve(name);
                var kind = e.kind();

                ChangeEvent.Type changeType = null;
                if (kind == ENTRY_CREATE) changeType = ChangeEvent.Type.CREATED;
                if (kind == ENTRY_MODIFY) changeType = ChangeEvent.Type.MODIFIED;
                if (kind == ENTRY_DELETE) changeType = ChangeEvent.Type.DELETED;

                if (kind == ENTRY_CREATE) {
                    try {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            registerDir(child);
                        }
                    } catch (IOException x) {
                        logger.warn("Can not watch directory {}.", child);
                    }
                }

                return new ChangeEvent<>(changeType, child);
            })
            .collect(Collectors.toList());
        boolean valid = key.reset();
        if (!valid) {
            keys.remove(key);
        }
        return result;
    }

    private void processEvents(FluxSink<List<ChangeEvent<Path>>> sink) {
        if (watcher == null) sink.error(new RuntimeException("Can not createAndInit watch service!"));
        for (; ; ) {

            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }
            var events = Lists.<ChangeEvent<Path>>newArrayList();
            events.addAll(watchKey2events(key));
            while ((key = watcher.poll()) != null) {
                events.addAll(watchKey2events(key));
            }
            sink.next(events);
            if (keys.isEmpty()) sink.complete();
        }
    }
}
