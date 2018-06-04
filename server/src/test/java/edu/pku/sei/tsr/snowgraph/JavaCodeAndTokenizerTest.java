package edu.pku.sei.tsr.snowgraph;

import edu.pku.sei.tsr.snowgraph.api.event.ChangeEvent;
import edu.pku.sei.tsr.snowgraph.javacodeextractor.JavaCodeGraphBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.Label;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JavaCodeAndTokenizerTest {
    private static final String DB_LOCATION = "/home/woooking/lab/neo4j/databases/snow-graph";
    private static final String NUTCH_LOCATION = "/home/woooking/lab/nutch";
    private static final String TEST_CODE_FILE = "/home/woooking/lab/nutch/test/TestBytes.java";
    private static final String TEST_CODE_TARGET = "/home/woooking/lab/nutch/sourcecode/apache-nutch-2.3.1/src/java/org/apache/nutch/util/TestBytes.java";
    private SnowGraph snowGraph;

    public void cleanDatabase() throws IOException {
        TestUtils.deleteDirectory(Paths.get(DB_LOCATION));
    }

    public void removeRedundantFile() throws IOException {
        var target = Paths.get(TEST_CODE_TARGET);
        Files.deleteIfExists(target);
    }

    public void buildGraph() {
        var codeTokenizer = new SnowGraphPluginConfig();
        codeTokenizer.setPath("edu.pku.sei.tsr.snowgraph.codetokenizer.CodeTokenizer");
        codeTokenizer.setArgs(List.of());

        var javaCodeExtractor = new SnowGraphPluginConfig();
        javaCodeExtractor.setPath("edu.pku.sei.tsr.snowgraph.javacodeextractor.JavaCodeExtractor");
        javaCodeExtractor.setArgs(List.of("sourcecode"));

        snowGraph = new SnowGraph.Builder("nutch", NUTCH_LOCATION, DB_LOCATION, List.of(codeTokenizer, javaCodeExtractor)).build();
    }

    @Before
    public void setup() throws IOException {
        cleanDatabase();
        removeRedundantFile();
        buildGraph();
    }

    @Test
    public void basicTest() {
        assertEquals("nutch", snowGraph.getName());
        var db = snowGraph.getDatabaseBuilder().newGraphDatabase();

        try (var tx = db.beginTx()) {
            assertEquals(408, db.findNodes(Label.label(JavaCodeGraphBuilder.CLASS)).stream().count());
            assertEquals(2377, db.findNodes(Label.label(JavaCodeGraphBuilder.METHOD)).stream().count());
            assertEquals(1486, db.findNodes(Label.label(JavaCodeGraphBuilder.FIELD)).stream().count());
            tx.success();
        }
    }

    @Test
    public void fileChangeTest() throws IOException {
        var target = Files.copy(Paths.get(TEST_CODE_FILE), Paths.get(TEST_CODE_TARGET));

        var changes = snowGraph.update(List.of(
            new ChangeEvent<>(ChangeEvent.Type.CREATED, target)
        ));

        var db = snowGraph.getDatabaseBuilder().newGraphDatabase();
        try (var tx = db.beginTx()) {
            assertEquals(409, db.findNodes(Label.label(JavaCodeGraphBuilder.CLASS)).stream().count());
            assertEquals(2448, db.findNodes(Label.label(JavaCodeGraphBuilder.METHOD)).stream().count());
            assertEquals(1500, db.findNodes(Label.label(JavaCodeGraphBuilder.FIELD)).stream().count());
            tx.success();
        }

        assertEquals(1 + 71 + 14, changes.getLeft().getChanges().size());
        assertEquals(126, changes.getRight().getChanges().size());

        Files.delete(target);
    }
}
