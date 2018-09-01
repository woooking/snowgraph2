package edu.pku.sei.tsr.snowgraph.web.message;

import edu.pku.sei.tsr.snowgraph.SnowGraph;
import lombok.Getter;

import java.util.List;

public class OnGetAllMessage {
    @Getter private final List<SnowGraph> graphs;

    public OnGetAllMessage(List<SnowGraph> graphs) {
        this.graphs = graphs;
    }
}
