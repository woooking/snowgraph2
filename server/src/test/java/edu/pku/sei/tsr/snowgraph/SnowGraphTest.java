package edu.pku.sei.tsr.snowgraph;

import edu.pku.sei.tsr.snowgraph.controller.GraphController;
import edu.pku.sei.tsr.snowgraph.javacodeextractor.entity.JavaClassInfo;
import edu.pku.sei.tsr.snowgraph.javacodeextractor.entity.JavaFieldInfo;
import edu.pku.sei.tsr.snowgraph.javacodeextractor.entity.JavaMethodInfo;
import edu.pku.sei.tsr.snowgraph.neo4j.GenericNeo4JOGMServiceFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SnowGraphTest {
    private static final String DB_LOCATION = "/home/woooking/lab/neo4j/databases/snow-graph";
    private static final String NUTCH_LOCATION = "/home/woooking/lab/nutch";

    @Autowired
    private GraphController graphController;

    @Test
    public void dependencyGraphTest() {

        var codeTokenizer = new SnowGraphPluginConfig();
        codeTokenizer.setPath("edu.pku.sei.tsr.snowgraph.codetokenizer.CodeTokenizer");
        codeTokenizer.setArgs(List.of());

        var javaCodeExtractor = new SnowGraphPluginConfig();
        javaCodeExtractor.setPath("edu.pku.sei.tsr.snowgraph.javacodeextractor.JavaCodeExtractor");
        javaCodeExtractor.setArgs(List.of("sourcecode"));

        var builder = new SnowGraph.Builder("nutch", NUTCH_LOCATION, DB_LOCATION, List.of(codeTokenizer, javaCodeExtractor));
        builder.build();
    }

    @Test
    public void javaCodeExtractorTest() throws IOException {

        var javaCodeExtractor = new SnowGraphPluginConfig();
        javaCodeExtractor.setPath("edu.pku.sei.tsr.snowgraph.javacodeextractor.JavaCodeExtractor");
        javaCodeExtractor.setArgs(List.of("sourcecode"));

        StepVerifier.create(graphController.build("nutch", NUTCH_LOCATION, DB_LOCATION, Collections.singletonList(javaCodeExtractor)))
            .assertNext(snowGraph -> {
                assertEquals(snowGraph.getName(), "nutch");
                var serviceFactory = new GenericNeo4JOGMServiceFactory(snowGraph.getSessionFactory());
                var classInfoService = serviceFactory.createService(JavaClassInfo.class);
                var methodInfoService = serviceFactory.createService(JavaMethodInfo.class);
                var fieldInfoService = serviceFactory.createService(JavaFieldInfo.class);
                assertEquals(classInfoService.count(), 408);
                assertEquals(methodInfoService.count(), 2377);
                assertEquals(fieldInfoService.count(), 1486);
            })
            .expectComplete()
            .verify();

//        Files.copy(Paths.get("/home/woooking/lab/nutch/test/TestBytes.java"), Paths.get("/home/woooking/lab/nutch/sourcecode/apache-nutch-2.3.1/src/java/org/apache/nutch/util/TestBytes.java"));
//        File file = new File("/home/woooking/lab/nutch/sourcecode/test.java");
//        assertTrue(file.createNewFile());
//        assertTrue(file.delete());
    }
}
