package edu.pku.sei.tsr.snowgraph.javacodeextractor;

import edu.pku.sei.tsr.snowgraph.api.*;
import edu.pku.sei.tsr.snowgraph.api.context.SnowGraphContext;
import edu.pku.sei.tsr.snowgraph.api.event.ChangeEvent;
import edu.pku.sei.tsr.snowgraph.api.event.LoadEvent;
import edu.pku.sei.tsr.snowgraph.api.event.ShutDownEvent;
import edu.pku.sei.tsr.snowgraph.api.plugin.SnowGraphPlugin;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("unused")
public class JavaCodeExtractor implements SnowGraphPlugin {
    private String srcPath;
    private JavaCodeGraphBuilder graphBuilder;

    @Override
    public List<String> dependsOn() {
        return List.of();
    }

    @Override
    public List<String> optionalDependsOn() {
        return List.of();
    }

    @Override
    public int order() {
        return SnowGraphPlugin.EXTRACTOR;
    }

    @Override
    public void preInit(PreInitRegistry preInitRegistry) {

    }

    @Override
    public void init(InitRegistry initRegistry) {
        this.srcPath = initRegistry.getArgs().get(0);
        this.graphBuilder = new JavaCodeGraphBuilder(initRegistry.getLogger());
    }

    @Override
    public void postInit(PostInitRegistry postInitRegistry) {
        postInitRegistry.registerDataPath(srcPath, true);
    }

    @Override
    public void onLoad(LoadEvent event) {
        var pluginConfigDirPath = event.getConfigDirPath();
        var projectFile = pluginConfigDirPath.resolve("project").toFile();
        graphBuilder.onLoad(projectFile);
    }

    @Override
    public void onShutDown(ShutDownEvent event) {
        var pluginConfigDirPath = event.getConfigDirPath();
        var projectFile = pluginConfigDirPath.resolve("project").toFile();
        graphBuilder.onSave(projectFile);
    }

    @Override
    public void run(SnowGraphContext context) {
        graphBuilder.process(context.getNeo4jService(), context.getData());
    }

    @Override
    public void update(SnowGraphContext context, Collection<ChangeEvent<Path>> changedFiles, Collection<ChangeEvent<Long>> changedNodes, Collection<ChangeEvent<Long>> changedRelationships) {
        graphBuilder.update(context.getNeo4jService(), changedFiles);
    }

}
