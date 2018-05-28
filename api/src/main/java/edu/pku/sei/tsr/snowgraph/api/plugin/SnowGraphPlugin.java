package edu.pku.sei.tsr.snowgraph.api.plugin;

import edu.pku.sei.tsr.snowgraph.api.*;
import edu.pku.sei.tsr.snowgraph.api.context.SnowGraphContext;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public interface SnowGraphPlugin<C extends SnowGraphContext> {
    int EXTRACTOR = 1;
    int LINKER = 10;
    int MINER = 100;
    int ANY = 1000;

    List<String> dependsOn();

    List<String> optionalDependsOn();

    int order();

    void preInit(PreInitRegistry preInitRegistry);

    void init(InitRegistry initRegistry);

    void postInit(PostInitRegistry postInitRegistry);

    void run(C context);

    void update(C context, Collection<ChangeEvent<Path>> changeEvents);
}
