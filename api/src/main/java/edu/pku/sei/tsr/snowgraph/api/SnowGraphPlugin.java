package edu.pku.sei.tsr.snowgraph.api;

import java.util.List;

public interface SnowGraphPlugin {

    List<String> entityPackage();

    void preInit(PreInitRegistry preInitRegistry);

    void init(InitRegistry initRegistry);

    void postInit(PostInitRegistry postInitRegistry);

    void run(SnowGraphContext context);
}
