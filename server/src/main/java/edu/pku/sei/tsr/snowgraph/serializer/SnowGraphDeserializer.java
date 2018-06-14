package edu.pku.sei.tsr.snowgraph.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import edu.pku.sei.tsr.snowgraph.SnowGraph;
import edu.pku.sei.tsr.snowgraph.SnowGraphFactory;
import edu.pku.sei.tsr.snowgraph.SnowGraphPluginConfig;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SnowGraphDeserializer extends StdDeserializer<SnowGraph> {
    @Autowired private SnowGraphFactory snowGraphFactory;

    public SnowGraphDeserializer() {
        super(SnowGraph.class);
    }

    @Override
    public SnowGraph deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        var root = p.getCodec().readTree(p);
        var name = ((TextNode) root.get("name")).asText();
        var dataDir = ((TextNode) root.get("dataDir")).asText();
        var destination = ((TextNode) root.get("destination")).asText();
        var createTime = p.getCodec().treeToValue(root.get("createTime"), Date.class);
        var updateTime = p.getCodec().treeToValue(root.get("updateTime"), Date.class);
        var pluginInfos = StreamSupport.stream(((ArrayNode) root.get("pluginConfigs")).spliterator(), false)
            .map(pluginConfigNode -> {
                try {
                    return p.getCodec().treeToValue(pluginConfigNode, SnowGraphPluginConfig.class);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());

        return SnowGraphFactory.load(name, dataDir, destination, pluginInfos, createTime, updateTime);
    }

}
