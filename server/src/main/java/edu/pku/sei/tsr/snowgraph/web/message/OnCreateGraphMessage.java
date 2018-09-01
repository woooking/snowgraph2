package edu.pku.sei.tsr.snowgraph.web.message;

import edu.pku.sei.tsr.snowgraph.SnowGraph;
import lombok.Getter;

public class OnCreateGraphMessage {
    @Getter private final SnowGraph graph;

    public OnCreateGraphMessage(SnowGraph graph) {
        this.graph = graph;
    }
}
