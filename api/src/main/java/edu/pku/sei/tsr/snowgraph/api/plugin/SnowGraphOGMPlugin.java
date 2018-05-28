package edu.pku.sei.tsr.snowgraph.api.plugin;

import edu.pku.sei.tsr.snowgraph.api.context.SnowGraphOGMContext;

import java.util.List;

public interface SnowGraphOGMPlugin extends SnowGraphPlugin<SnowGraphOGMContext> {
    List<String> entityPackage();

}
