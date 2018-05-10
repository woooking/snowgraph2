package edu.pku.sei.tsr.snowgraph;

import java.util.List;

public class SnowGraphConfig {
    public static class PluginConfig {
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

    private List<PluginConfig> plugins;

    public List<PluginConfig> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<PluginConfig> plugins) {
        this.plugins = plugins;
    }
}
