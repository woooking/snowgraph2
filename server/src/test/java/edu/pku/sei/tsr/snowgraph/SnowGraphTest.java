package edu.pku.sei.tsr.snowgraph;

import edu.pku.sei.tsr.snowgraph.controller.GraphController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SnowGraphTest {

    @Autowired
    private GraphController graphController;

    @Test
    public void javaCodeExtractorTest() {
        var javaCodeExtractor = new SnowGraphPluginConfig();
        javaCodeExtractor.setPath("edu.pku.sei.tsr.snowgraph.javacodeextractor.JavaCodeExtractor");
        javaCodeExtractor.setArgs(List.of("/home/woooking/lab/nutch/sourcecode"));

        StepVerifier.create(graphController.build("java-code", "/home/woooking/lab/neo4j/databases/snow-graph", Collections.singletonList(javaCodeExtractor)))
            .expectNextMatches(snowGraph -> snowGraph.getName().equals("java-code"))
            .expectComplete()
            .verify();
    }
}
