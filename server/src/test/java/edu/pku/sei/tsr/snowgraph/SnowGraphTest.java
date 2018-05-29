package edu.pku.sei.tsr.snowgraph;

import edu.pku.sei.tsr.snowgraph.api.ChangeEvent;
import edu.pku.sei.tsr.snowgraph.webflux.controller.GraphController;
import edu.pku.sei.tsr.snowgraph.javacodeextractor.JavaCodeGraphBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SnowGraphTest {
    private static final String DB_LOCATION = "/home/woooking/lab/neo4j/databases/snow-graph";
    private static final String NUTCH_LOCATION = "/home/woooking/lab/nutch";
    private SnowGraph snowGraph;

    public void cleanDatabase() throws IOException {
        Files.walkFileTree(Paths.get(DB_LOCATION), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void buildGraph() {
        var javaCodeExtractor = new SnowGraphPluginConfig();
        javaCodeExtractor.setPath("edu.pku.sei.tsr.snowgraph.javacodeextractor.JavaCodeExtractor");
        javaCodeExtractor.setArgs(List.of("sourcecode"));

        snowGraph = new SnowGraph.Builder("nutch", NUTCH_LOCATION, DB_LOCATION, Collections.singletonList(javaCodeExtractor)).build();
    }

    @Before
    public void setup() throws IOException {
        cleanDatabase();
        buildGraph();
    }

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
        assertEquals(snowGraph.getName(), "nutch");
        var db = snowGraph.getDatabaseBuilder().newGraphDatabase();

        try (var tx = db.beginTx()) {
            assertEquals(db.findNodes(JavaCodeGraphBuilder.CLASS).stream().count(), 408);
            assertEquals(db.findNodes(JavaCodeGraphBuilder.METHOD).stream().count(), 2377);
            assertEquals(db.findNodes(JavaCodeGraphBuilder.FIELD).stream().count(), 1486);
            tx.success();
        }
    }

    @Test
    public void fileChangeTest() throws IOException {
        var target = Files.copy(Paths.get("/home/woooking/lab/nutch/test/TestBytes.java"), Paths.get("/home/woooking/lab/nutch/sourcecode/apache-nutch-2.3.1/src/java/org/apache/nutch/util/TestBytes.java"));
        snowGraph.update(List.of(
            new ChangeEvent<>(ChangeEvent.Type.CREATED, Paths.get("/home/woooking/lab/nutch/sourcecode/apache-nutch-2.3.1/src/java/org/apache/nutch/util/TestBytes.java"))
        ));

        var db = snowGraph.getDatabaseBuilder().newGraphDatabase();
        try (var tx = db.beginTx()) {
            assertEquals(db.findNodes(JavaCodeGraphBuilder.CLASS).stream().count(), 409);
            assertEquals(db.findNodes(JavaCodeGraphBuilder.METHOD).stream().count(), 2448);
            assertEquals(db.findNodes(JavaCodeGraphBuilder.FIELD).stream().count(), 1500);
            tx.success();
        }

        Files.delete(target);
    }
}
