package edu.pku.sei.tsr.snowgraph.javacodeextractor;

import edu.pku.sei.tsr.snowgraph.api.SnowGraphContext;
import edu.pku.sei.tsr.snowgraph.api.SnowGraphPlugin;

import java.util.List;

public class JavaCodeExtractor implements SnowGraphPlugin {
    private String srcPath;

    @Override
    public void preInit() {

    }

    @Override
    public void init(List<String> args) {
        this.srcPath = args.get(0);
    }

    public void run(SnowGraphContext context) {
        JavaCodeGraphBuilder.process(context.getNeo4jServiceFactory(), srcPath);
    }

}
