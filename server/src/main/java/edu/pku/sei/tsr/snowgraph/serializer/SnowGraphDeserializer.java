package edu.pku.sei.tsr.snowgraph.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import edu.pku.sei.tsr.snowgraph.SnowGraph;
import edu.pku.sei.tsr.snowgraph.SnowGraphPluginConfig;
import edu.pku.sei.tsr.snowgraph.SnowGraphPluginInfo;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SnowGraphDeserializer extends StdDeserializer<SnowGraph> {
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

        return new SnowGraph.Builder(name, dataDir, destination, pluginInfos, createTime, updateTime).build();
    }

}
