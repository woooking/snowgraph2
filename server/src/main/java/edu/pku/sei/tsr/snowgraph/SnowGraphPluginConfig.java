package edu.pku.sei.tsr.snowgraph;

import lombok.Data;

import java.util.List;

@Data
public class SnowGraphPluginConfig {
    private String path;
    private List<String> args;
}
