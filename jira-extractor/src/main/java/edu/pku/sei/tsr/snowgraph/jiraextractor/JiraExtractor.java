package edu.pku.sei.tsr.snowgraph.jiraextractor;

import edu.pku.sei.tsr.snowgraph.api.InitRegistry;
import edu.pku.sei.tsr.snowgraph.api.PostInitRegistry;
import edu.pku.sei.tsr.snowgraph.api.PreInitRegistry;
import edu.pku.sei.tsr.snowgraph.api.context.SnowGraphContext;
import edu.pku.sei.tsr.snowgraph.api.event.ChangeEvent;
import edu.pku.sei.tsr.snowgraph.api.event.LoadEvent;
import edu.pku.sei.tsr.snowgraph.api.event.ShutDownEvent;
import edu.pku.sei.tsr.snowgraph.api.plugin.SnowGraphPlugin;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("unused")
public class JiraExtractor implements SnowGraphPlugin {
    private String srcPath;

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
    }

    @Override
    public void postInit(PostInitRegistry postInitRegistry) {
        postInitRegistry.registerDataPath(srcPath, true);
    }

    @Override
    public void onLoad(LoadEvent event) {

    }

    @Override
    public void onShutDown(ShutDownEvent event) {

    }

    @Override
    public void run(SnowGraphContext context) {
        JiraGraphBuilder.process(context);
    }

    @Override
    public void update(SnowGraphContext context, Collection<ChangeEvent<Path>> changedFiles, Collection<ChangeEvent<Long>> changedNodes, Collection<ChangeEvent<Long>> changedRelationships) {

    }
}
