package edu.pku.sei.tsr.snowgraph;

import edu.pku.sei.tsr.snowgraph.repository.SnowGraphRepository;
import edu.pku.sei.tsr.snowgraph.webflux.serializer.SnowGraphSerializer;
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
            .serializers(new SnowGraphSerializer());
    }

    @Bean
    @Scope("singleton")
    public SnowGraphConfig snowGraphConfig() {
        return new SnowGraphConfig();
    }

    @Bean
    @Scope("singleton")
    public SnowGraphManager snowGraphManager() {
        return new SnowGraphManager();
    }

    @Bean
    public SnowGraphRepository snowGraphRepository() {
        return snowGraphManager();
    }

    public static void main(String[] args) {
        SpringApplication.run(SnowGraphApplication.class, args);
    }
}
