package edu.pku.sei.tsr.snowgraph;

import edu.pku.sei.tsr.snowgraph.api.event.ChangeEvent;
import edu.pku.sei.tsr.snowgraph.javacodeextractor.JavaCodeGraphBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JiraExtractorTest {
    private static final String DB_LOCATION = "/home/woooking/lab/neo4j/databases/snow-graph";
    private static final String NUTCH_LOCATION = "/home/woooking/lab/nutch";
    private SnowGraph snowGraph;

    public void cleanDatabase() throws IOException {
        TestUtils.deleteDirectory(Paths.get(DB_LOCATION));
    }

    public void removeRedundantFile() {
    }

    public void buildGraph() {
        var jiraExtractor = new SnowGraphPluginConfig();
        jiraExtractor.setPath("edu.pku.sei.tsr.snowgraph.jiraextractor.JiraExtractor");
        jiraExtractor.setArgs(List.of("jira"));

        snowGraph = new SnowGraph.Builder("nutch", NUTCH_LOCATION, DB_LOCATION, Collections.singletonList(jiraExtractor)).build();
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
            assertEquals(2336, db.findNodes(Label.label("JiraIssue")).stream().count());
            assertEquals(13981, db.findNodes(Label.label("JiraIssueComment")).stream().count());
            assertEquals(786, db.findNodes(Label.label("JiraIssueUser")).stream().count());
            assertEquals(2323, db.findNodes(Label.label("JiraPatch")).stream().count());
            assertEquals(13981, db.getAllRelationships().stream().filter(r -> r.isType(RelationshipType.withName("jira_have_issue_comment"))).count());
            assertEquals(2323, db.getAllRelationships().stream().filter(r -> r.isType(RelationshipType.withName("jira_have_patch"))).count());
            assertEquals(1250, db.getAllRelationships().stream().filter(r -> r.isType(RelationshipType.withName("jira_is_assignee_of_issue"))).count());
            assertEquals(2336, db.getAllRelationships().stream().filter(r -> r.isType(RelationshipType.withName("jira_is_creator_of_issue"))).count());
            assertEquals(13981, db.getAllRelationships().stream().filter(r -> r.isType(RelationshipType.withName("jira_is_creator_of_issueComment"))).count());
            assertEquals(2323, db.getAllRelationships().stream().filter(r -> r.isType(RelationshipType.withName("jira_is_creator_of_patch"))).count());
            assertEquals(2336, db.getAllRelationships().stream().filter(r -> r.isType(RelationshipType.withName("jira_is_reporter_of_issue"))).count());
            assertEquals(13981, db.getAllRelationships().stream().filter(r -> r.isType(RelationshipType.withName("jira_is_updater_of_issueComment"))).count());
            assertEquals(384, db.getAllRelationships().stream().filter(r -> r.isType(RelationshipType.withName("jira_issue_duplicate"))).count());
            tx.success();
        }
    }

    @Test
    public void fileChangeTest() throws IOException {
    }
}
