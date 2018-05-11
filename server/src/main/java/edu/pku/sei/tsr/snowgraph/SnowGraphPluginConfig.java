package edu.pku.sei.tsr.snowgraph;

import java.util.List;

public class SnowGraphPluginConfig {
    private String path;
    private List<String> args;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }
}
