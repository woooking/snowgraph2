package edu.pku.sei.tsr.snowgraph.web.message;

import edu.pku.sei.tsr.snowgraph.SnowGraphPluginConfig;
import lombok.Data;

import java.util.List;

@Data
public class CreateGraphMessage {
    private String name;
    private String srcDir;
    private String destination;
    private List<SnowGraphPluginConfig> pluginConfigs;
}
