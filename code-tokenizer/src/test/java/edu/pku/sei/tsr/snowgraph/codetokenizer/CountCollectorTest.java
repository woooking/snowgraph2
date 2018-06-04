package edu.pku.sei.tsr.snowgraph.codetokenizer;

import edu.pku.sei.tsr.snowgraph.codetokenizer.CountCollector;
import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class CountCollectorTest {
    @Test
    public void countCollectorTest() {
        var l1 = Stream.of().collect(CountCollector.create(5));
        var l2 = Stream.of(1, 2).collect(CountCollector.create(5));
        var l3 = Stream.of(1, 2).collect(CountCollector.create(2));
        var l4 = Stream.of(1, 2, 3, 4, 5).collect(CountCollector.create(1));
        var l5 = Stream.of(1, 2, 3, 4, 5).collect(CountCollector.create(2));
        var l6 = Stream.of(1, 2, 3, 4, 5).collect(CountCollector.create(3));
        var l7 = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9).collect(CountCollector.create(3));
        var l8 = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9).collect(CountCollector.create(5));
        assertThat(l1, is(List.of()));
        assertThat(l2, is(List.of(List.of(1, 2))));
        assertThat(l3, is(List.of(List.of(1, 2))));
        assertThat(l4, is(List.of(List.of(1), List.of(2), List.of(3), List.of(4), List.of(5))));
        assertThat(l5, is(List.of(List.of(1, 2), List.of(3, 4), List.of(5))));
        assertThat(l6, is(List.of(List.of(1, 2, 3), List.of(4, 5))));
        assertThat(l7, is(List.of(List.of(1, 2, 3), List.of(4, 5, 6), List.of(7, 8, 9))));
        assertThat(l8, is(List.of(List.of(1, 2, 3, 4, 5), List.of(6, 7, 8, 9))));
    }
}
