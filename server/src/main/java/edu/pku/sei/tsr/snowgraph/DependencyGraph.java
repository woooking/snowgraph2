package edu.pku.sei.tsr.snowgraph;

import edu.pku.sei.tsr.snowgraph.api.plugin.SnowGraphPlugin;
import edu.pku.sei.tsr.snowgraph.exception.DependenceException;
import lombok.Getter;

import java.util.*;

class DependencyGraph {
    private Map<String, PluginNode> nodes = new HashMap<>();
    @Getter private List<SnowGraphPluginInfo> sortedPlugins = new ArrayList<>();

    private class PluginNode {
        private int inDegree = 0;
        private List<PluginNode> outNodes = new ArrayList<>();
        private SnowGraphPluginInfo pluginInfo;

        private PluginNode(SnowGraphPluginInfo pluginInfo) {
            this.pluginInfo = pluginInfo;
        }

        private void addDependency(PluginNode dependency) {
            outNodes.add(dependency);
            ++dependency.inDegree;
        }

        private int getOrder() {
            return pluginInfo.getInstance().order();
        }
    }

    DependencyGraph(Collection<SnowGraphPluginInfo> plugins) {
        plugins.forEach(plugin -> nodes.put(plugin.getInstance().getClass().getName(), new PluginNode(plugin)));
        plugins.forEach(this::resolveDependency);
        topologicalSort();
    }

    private void resolveDependency(SnowGraphPluginInfo plugin) {
        plugin.getInstance().dependsOn().forEach(dependency -> addDependency(plugin.getInstance(), dependency, false));
        plugin.getInstance().optionalDependsOn().forEach(dependency -> addDependency(plugin.getInstance(), dependency, true));
    }

    private void addDependency(SnowGraphPlugin plugin, String dependency, boolean optional) {
        var dependencyNode = nodes.get(dependency);
        if (dependencyNode == null) {
            if (optional) return;
            throw DependenceException.notSatisfied(plugin, dependency);
        }
        dependencyNode.addDependency(nodes.get(plugin.getClass().getName()));
    }

    private void topologicalSort() {
        PriorityQueue<PluginNode> noIncomingNodes = new PriorityQueue<>(Comparator.comparing(PluginNode::getOrder));
        var pluginNodes = nodes.values();
        var ite = pluginNodes.iterator();
        while (ite.hasNext()) {
            PluginNode next = ite.next();
            if (next.inDegree == 0) {
                noIncomingNodes.add(next);
                ite.remove();
            }
        }
        while (!noIncomingNodes.isEmpty() || !pluginNodes.isEmpty()) {
            var node = noIncomingNodes.poll();
            if (node == null) throw DependenceException.cycledDependence();
            sortedPlugins.add(node.pluginInfo);
            node.outNodes.forEach(out -> {
                --out.inDegree;
                if (out.inDegree == 0) {
                    noIncomingNodes.add(out);
                    pluginNodes.remove(out);
                }
            });
        }
    }
}
