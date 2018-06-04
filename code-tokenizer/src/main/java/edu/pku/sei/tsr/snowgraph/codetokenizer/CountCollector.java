package edu.pku.sei.tsr.snowgraph.codetokenizer;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class CountCollector<T> implements Collector<T, MutablePair<List<T>, List<List<T>>>, List<List<T>>> {
    public static <T> CountCollector<T> create(int count) {
        return new CountCollector<>(count);
    }

    private final int count;

    private CountCollector(int count) {
        this.count = count;
    }

    @Override
    public Supplier<MutablePair<List<T>, List<List<T>>>> supplier() {
        return () -> MutablePair.of(new ArrayList<>(), new ArrayList<>());
    }

    @Override
    public BiConsumer<MutablePair<List<T>, List<List<T>>>, T> accumulator() {
        return (p, v) -> {
            if (p.getLeft().size() >= count) {
                p.getRight().add(p.getLeft());
                p.setLeft(new ArrayList<>());
            }
            p.getLeft().add(v);
        };
    }

    @Override
    public BinaryOperator<MutablePair<List<T>, List<List<T>>>> combiner() {
       return (a, b) -> {
           throw new NotImplementedException("Combiner for CountCollector is not implemented.");
       };
    }

    @Override
    public Function<MutablePair<List<T>, List<List<T>>>, List<List<T>>> finisher() {
        return p -> {
            var r = p.getRight();
            if (p.getLeft().size() > 0) r.add(p.getLeft());
            return r;
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of();
    }
}
