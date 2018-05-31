package edu.pku.sei.tsr.snowgraph;

import edu.pku.sei.tsr.snowgraph.api.event.ChangeEvent;
import edu.pku.sei.tsr.snowgraph.javacodeextractor.JavaCodeGraphBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JavaCodeExtractorTest {
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
        var javaCodeExtractor = new SnowGraphPluginConfig();
        javaCodeExtractor.setPath("edu.pku.sei.tsr.snowgraph.javacodeextractor.JavaCodeExtractor");
        javaCodeExtractor.setArgs(List.of("sourcecode"));

        snowGraph = new SnowGraph.Builder("nutch", NUTCH_LOCATION, DB_LOCATION, Collections.singletonList(javaCodeExtractor)).build();
    }

    @Before
    public void setup() throws IOException {
        cleanDatabase();
        removeRedundantFile();
        buildGraph();
    }

    @Test
    public void basicTest() {
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
        var target = Files.copy(Paths.get(TEST_CODE_FILE), Paths.get(TEST_CODE_TARGET));

        var changes = snowGraph.update(List.of(
            new ChangeEvent<>(ChangeEvent.Type.CREATED, target)
        ));

        var db = snowGraph.getDatabaseBuilder().newGraphDatabase();
        try (var tx = db.beginTx()) {
            assertEquals(db.findNodes(JavaCodeGraphBuilder.CLASS).stream().count(), 409);
            assertEquals(db.findNodes(JavaCodeGraphBuilder.METHOD).stream().count(), 2448);
            assertEquals(db.findNodes(JavaCodeGraphBuilder.FIELD).stream().count(), 1500);
            tx.success();
        }

        assertEquals(changes.getLeft().getChanges().size(), 1 + 71 + 14);
        assertEquals(changes.getRight().getChanges().size(), 126);

        Files.delete(target);
    }
}
