package edu.pku.sei.tsr.snowgraph;

import edu.pku.sei.tsr.snowgraph.serializer.SnowGraphDeserializer;
import edu.pku.sei.tsr.snowgraph.serializer.SnowGraphSerializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@SpringBootApplication
public class SnowGraphApplication {

    @Bean
    @Primary
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
        return Jackson2ObjectMapperBuilder.json()
            .serializers(new SnowGraphSerializer())
            .deserializers(new SnowGraphDeserializer());
    }

    @Bean
    @Scope("singleton")
    public SnowGraphPersistence snowGraphPersistence() {
        return new SnowGraphPersistence();
    }

    @Bean
    @Scope("singleton")
    public SnowGraphManager snowGraphManager() {
        return new SnowGraphManager();
    }

    public static void main(String[] args) {
        SpringApplication.run(SnowGraphApplication.class, args);
    }
}
