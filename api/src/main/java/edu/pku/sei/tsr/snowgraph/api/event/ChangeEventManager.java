package edu.pku.sei.tsr.snowgraph.api.event;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ChangeEventManager<T> {
    private Map<T, ChangeEvent.Type> state = new HashMap<>();

    public void addEvent(ChangeEvent<T> event) {
        var instance = event.getInstance();
        var type = event.getType();
        addEvent(instance, type);
    }

    public void addEvent(T instance, ChangeEvent.Type type) {
        if (state.containsKey(instance)) {
            var newState = ChangeEvent.transform(state.get(instance), type);
            if (newState.isPresent()) state.put(instance, newState.get());
            else state.remove(instance);
        } else {
            state.put(instance, type);
        }
    }

    public Collection<ChangeEvent<T>> getChanges() {
        return state.entrySet().stream()
            .map(e -> new ChangeEvent<>(e.getValue(), e.getKey()))
            .collect(Collectors.toSet());
    }
}
