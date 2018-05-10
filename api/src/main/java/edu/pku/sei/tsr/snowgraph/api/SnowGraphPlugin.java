package edu.pku.sei.tsr.snowgraph.api;

import java.util.List;

public interface SnowGraphPlugin {

    void preInit();

    void init(List<String> args);

    void run(SnowGraphContext context);
}
