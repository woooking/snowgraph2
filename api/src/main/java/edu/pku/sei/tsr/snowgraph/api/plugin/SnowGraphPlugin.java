package edu.pku.sei.tsr.snowgraph.api.plugin;

import edu.pku.sei.tsr.snowgraph.api.*;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public interface SnowGraphPlugin {
    int EXTRACTOR = 1;
    int LINKER = 10;
    int MINER = 100;
    int ANY = 1000;

    List<String> dependsOn();

    int order();

    List<String> entityPackage();

    void preInit(PreInitRegistry preInitRegistry);

    void init(InitRegistry initRegistry);

    void postInit(PostInitRegistry postInitRegistry);

    void run(SnowGraphContext context);

    void update(SnowGraphContext context, Collection<ChangeEvent<Path>> changeEvents);
}
