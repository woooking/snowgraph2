package edu.pku.sei.tsr.snowgraph.codetokenizer;

import edu.pku.sei.tsr.snowgraph.api.*;
import edu.pku.sei.tsr.snowgraph.api.plugin.SnowGraphPlugin;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class CodeTokenizer implements SnowGraphPlugin {
    @Override
    public List<String> dependsOn() {
        return List.of();
    }

    @Override
    public List<String> optionalDependsOn() {
        return List.of("edu.pku.sei.tsr.snowgraph.javacodeextractor.JavaCodeExtractor");
    }

    @Override
    public int order() {
        return SnowGraphPlugin.EXTRACTOR;
    }

    @Override
    public List<String> entityPackage() {
        return List.of();
    }

    @Override
    public void preInit(PreInitRegistry preInitRegistry) {

    }

    @Override
    public void init(InitRegistry initRegistry) {

    }

    @Override
    public void postInit(PostInitRegistry postInitRegistry) {

    }

    @Override
    public void run(SnowGraphContext context) {
        context.getLogger().info("Code tokenizer called.");
    }

    @Override
    public void update(SnowGraphContext context, Collection<ChangeEvent<Path>> changeEvents) {

    }
}
