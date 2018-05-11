package edu.pku.sei.tsr.snowgraph.javacodeextractor;

import edu.pku.sei.tsr.snowgraph.api.*;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class JavaCodeExtractor implements SnowGraphPlugin {
    private String srcPath;

    @Override
    public List<String> entityPackage() {
        return Collections.singletonList("edu.pku.sei.tsr.snowgraph.javacodeextractor.entity");
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

    }

    public void run(SnowGraphContext context) {
        JavaCodeGraphBuilder.process(context.getNeo4jServiceFactory(), srcPath);
    }

}
