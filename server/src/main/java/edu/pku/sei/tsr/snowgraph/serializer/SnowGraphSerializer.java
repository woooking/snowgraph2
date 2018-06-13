package edu.pku.sei.tsr.snowgraph.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import edu.pku.sei.tsr.snowgraph.SnowGraph;
import edu.pku.sei.tsr.snowgraph.SnowGraphPluginInfo;

import java.io.IOException;

public class SnowGraphSerializer extends StdSerializer<SnowGraph> {
    public SnowGraphSerializer() {
        super(SnowGraph.class);
    }

    @Override
    public void serialize(SnowGraph value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("name", value.getName());
        gen.writeStringField("dataDir", value.getDataDir());
        gen.writeStringField("destination", value.getDestination());
        gen.writeObjectField("createTime", value.getCreateTime());
        gen.writeObjectField("updateTime", value.getUpdateTime());
        gen.writeArrayFieldStart("pluginConfigs");
        for (SnowGraphPluginInfo pluginInfo : value.getPluginInfos()) {
            gen.writeObject(pluginInfo.getConfig());
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }

}
