package edu.pku.sei.tsr.snowgraph;

import edu.pku.sei.tsr.snowgraph.api.event.ChangeEvent;
import edu.pku.sei.tsr.snowgraph.javacodeextractor.JavaCodeGraphBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.*;
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

        snowGraph = SnowGraphFactory.create("nutch", NUTCH_LOCATION, DB_LOCATION, Collections.singletonList(javaCodeExtractor));
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
        checkCount(408, 2377, 1486);
    }

    @Test
    public void fileChangeTest() throws IOException {
        assertEquals("nutch", snowGraph.getName());
        checkCount(408, 2377, 1486);

        var target = Files.copy(Paths.get(TEST_CODE_FILE), Paths.get(TEST_CODE_TARGET));

        var changes1 = snowGraph.update(List.of(
            new ChangeEvent<>(ChangeEvent.Type.CREATED, target)
        ));

        checkCount(409, 2448, 1500);

        assertEquals(1 + 71 + 14, changes1.getLeft().getChanges().size());
        assertEquals(128, changes1.getRight().getChanges().size());

        Files.delete(target);

        var changes2 = snowGraph.update(List.of(
            new ChangeEvent<>(ChangeEvent.Type.DELETED, target)
        ));

        checkCount(408, 2377, 1486);

        assertEquals(1 + 71 + 14, changes2.getLeft().getChanges().size());
        assertEquals(128, changes2.getRight().getChanges().size());
    }

    private void checkCount(int expectedClassNum, int expectedMethodNum, int expectedFieldNum) {
        var db = snowGraph.getDatabaseBuilder().newGraphDatabase();
        try (var tx = db.beginTx()) {
            assertEquals(expectedClassNum, db.findNodes(Label.label(JavaCodeGraphBuilder.CLASS)).stream().count());
            assertEquals(expectedMethodNum, db.findNodes(Label.label(JavaCodeGraphBuilder.METHOD)).stream().count());
            assertEquals(expectedFieldNum, db.findNodes(Label.label(JavaCodeGraphBuilder.FIELD)).stream().count());
            tx.success();
        }
        db.shutdown();
    }
}
