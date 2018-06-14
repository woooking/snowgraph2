package edu.pku.sei.tsr.snowgraph.jiraextractor;

import edu.pku.sei.tsr.snowgraph.api.context.SnowGraphContext;
import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jNode;
import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jService;
import edu.pku.sei.tsr.snowgraph.jiraextractor.entity.IssueCommentInfo;
import edu.pku.sei.tsr.snowgraph.jiraextractor.entity.IssueInfo;
import edu.pku.sei.tsr.snowgraph.jiraextractor.entity.IssueUserInfo;
import edu.pku.sei.tsr.snowgraph.jiraextractor.entity.PatchInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class JiraGraphBuilder {

    public static final String ISSUE = "JiraIssue";
    public static final String ISSUE_ID = "id";
    public static final String ISSUE_NAME = "name";
    public static final String ISSUE_SUMMARY = "summary";
    public static final String ISSUE_TYPE = "type";
    public static final String ISSUE_STATUS = "status";
    public static final String ISSUE_PRIORITY = "priority";
    public static final String ISSUE_RESOLUTION = "resolution";
    public static final String ISSUE_VERSIONS = "versions";
    public static final String ISSUE_FIX_VERSIONS = "fixVersions";
    public static final String ISSUE_COMPONENTS = "components";
    public static final String ISSUE_LABELS = "labels";
    public static final String ISSUE_DESCRIPTION = "description";
    public static final String ISSUE_CREATOR_NAME = "crearorName";
    public static final String ISSUE_ASSIGNEE_NAME = "assigneeName";
    public static final String ISSUE_REPORTER_NAME = "reporterName";
    public static final String ISSUE_CREATED_DATE = "createdDate";
    public static final String ISSUE_UPDATED_DATE = "updatedDate";
    public static final String ISSUE_RESOLUTION_DATE = "resolutionDate";

    public static final String PATCH = "JiraPatch";
    public static final String PATCH_ISSUE_ID = "issueId";
    public static final String PATCH_ID = "id";
    public static final String PATCH_NAME = "name";
    public static final String PATCH_CONTENT = "content";
    public static final String PATCH_CREATOR_NAME = "creatorName";
    public static final String PATCH_CREATED_DATE = "createdDate";

    public static final String ISSUECOMMENT = "JiraIssueComment";
    public static final String ISSUECOMMENT_ID = "id";
    public static final String ISSUECOMMENT_BODY = "body";
    public static final String ISSUECOMMENT_CREATOR_NAME = "creatorName";
    public static final String ISSUECOMMENT_UPDATER_NAME = "updaterName";
    public static final String ISSUECOMMENT_CREATED_DATE = "createdDate";
    public static final String ISSUECOMMENT_UPDATED_DATE = "updatedDate";

    public static final String ISSUEUSER = "JiraIssueUser";
    public static final String ISSUEUSER_NAME = "name";
    public static final String ISSUEUSER_EMAIL_ADDRESS = "emailAddress";
    public static final String ISSUEUSER_DISPLAY_NAME = "displayName";
    public static final String ISSUEUSER_ACTIVE = "active";

    private static final RelationshipType HAVE_PATCH = RelationshipType.withName("jira_have_patch");
    private static final RelationshipType HAVE_ISSUE_COMMENT = RelationshipType.withName("jira_have_issue_comment");
    private static final RelationshipType ISSUE_DUPLICATE = RelationshipType.withName("jira_issue_duplicate");
    private static final RelationshipType IS_ASSIGNEE_OF_ISSUE = RelationshipType.withName("jira_is_assignee_of_issue");
    private static final RelationshipType IS_CREATOR_OF_ISSUE = RelationshipType.withName("jira_is_creator_of_issue");
    private static final RelationshipType IS_REPORTER_OF_ISSUE = RelationshipType.withName("jira_is_reporter_of_issue");
    private static final RelationshipType IS_CREATOR_OF_ISSUECOMMENT = RelationshipType.withName("jira_is_creator_of_issueComment");
    private static final RelationshipType IS_UPDATER_OF_ISSUECOMMENT = RelationshipType.withName("jira_is_updater_of_issueComment");
    private static final RelationshipType IS_CREATOR_OF_PATCH = RelationshipType.withName("jira_is_creator_of_patch");

    private final Neo4jService db;
    private final Logger logger;

    private FileFilter jsonFileFilter = new SuffixFileFilter(new String[]{".json"});
    private FileFilter patchFileFilter = new SuffixFileFilter(new String[]{".patch"});
    private Map<String, Long> userNodeMap = new HashMap<>();
    private List<String> duplicateList = new ArrayList<>(); // "a b"代表a指向b
    private Map<String, Long> issueNodeMap = new HashMap<>();
    private Map<String, Neo4jNode> patchNodeMap = new HashMap<>();

    public JiraGraphBuilder(Neo4jService db, Logger logger) {
        this.db = db;
        this.logger = logger;
    }

    public static void process(SnowGraphContext context, Logger logger) throws JSONException {
        var files = context.getData();
        new JiraGraphBuilder(context.getNeo4jService(), logger).run(files);
    }

    private Stream<Path> safeList(Path dir) {
        var result = Stream.<Path>of();
        try {
            result = Files.list(dir);
        } catch (IOException ignored) {
        }
        return result;
    }

    public void run(Collection<File> files) throws JSONException {
        var dataRootOpt = files.stream().findFirst();
        assert dataRootOpt.isPresent();

        var dataRoot = dataRootOpt.get();
        safeList(dataRoot.toPath())
            .flatMap(this::safeList)
            .filter(p -> p.toFile().isDirectory())
            .flatMap(this::safeList)
            .map(Path::toFile)
            .filter(jsonFileFilter::accept)
            .forEach(f -> {
                try (Transaction tx = db.beginTx()) {
                    jsonHandler(f);
                    tx.success();
                }
            });

        safeList(dataRoot.toPath())
            .flatMap(this::safeList)
            .filter(p -> p.toFile().isDirectory())
            .flatMap(this::safeList)
            .filter(p -> p.getFileName().toString().equals("Patchs"))
            .flatMap(this::safeList)
            .map(Path::toFile)
            .filter(patchFileFilter::accept)
            .filter(f -> patchNodeMap.containsKey(f.getName()))
            .forEach(f -> {
                try (Transaction tx = db.beginTx()) {
                    patchNodeMap.get(f.getName()).setProperty(JiraGraphBuilder.PATCH_CONTENT, FileUtils.readFileToString(f, Charset.defaultCharset()));
                    tx.success();
                } catch (IOException e) {
                    logger.error("Error curred when reading file!", e);
                }
            });

        try (Transaction tx = db.beginTx()) {
            // 建立DUPLICATE关联
            for (String line : duplicateList) {
                String[] eles = line.trim().split("\\s+");
                String id1 = eles[0];
                String id2 = eles[1];
                if (issueNodeMap.containsKey(id1) && issueNodeMap.containsKey(id2))
                    db.createRelationship(issueNodeMap.get(id1), issueNodeMap.get(id2), ISSUE_DUPLICATE);
            }
            tx.success();
        }
    }

    private void jsonHandler(File issueFile) throws JSONException {
        String jsonContent = null;
        try {
            jsonContent = FileUtils.readFileToString(issueFile, Charset.defaultCharset());
        } catch (IOException e) {
            logger.error("Error parsing json file.", e);
        }

        if (jsonContent == null) {
            logger.error("Json is empty!");
            return;
        }

        // 建立Issue实体
        IssueInfo issueInfo = getIssueInfo(jsonContent);
        var node = db.createNode(ISSUE, JiraUtils.buildPropertyMap(issueInfo));
        var nodeId = node.getId();
        issueNodeMap.put(issueInfo.getIssueId(), nodeId);

        // 建立用户实体
        if (jsonContent.length() > 0) {
            JSONObject fields = new JSONObject(jsonContent).getJSONObject("fields");
            Pair<String, Long> assignee = createUserNode(fields, "assignee");
            Pair<String, Long> creator = createUserNode(fields, "creator");
            Pair<String, Long> reporter = createUserNode(fields, "reporter");
            // 建立用户实体与Issue实体之间的关联
            if (assignee != null) {
                node.setProperty(ISSUE_ASSIGNEE_NAME, assignee.getLeft());
                db.createRelationship(assignee.getRight(), nodeId, IS_ASSIGNEE_OF_ISSUE);
            }
            if (creator != null) {
                node.setProperty(ISSUE_CREATOR_NAME, creator.getLeft());
                db.createRelationship(creator.getRight(), nodeId, IS_CREATOR_OF_ISSUE);
            }
            if (reporter != null) {
                node.setProperty(ISSUE_REPORTER_NAME, reporter.getLeft());
                db.createRelationship(reporter.getRight(), nodeId, IS_REPORTER_OF_ISSUE);
            }

            // 记录DUPLICATE关系
            JSONArray jsonIssueLinks = fields.getJSONArray("issuelinks");
            int issueLinkNum = jsonIssueLinks.length();
            for (int i = 0; i < issueLinkNum; i++) {
                JSONObject jsonIssueLink = jsonIssueLinks.getJSONObject(i);
                if (jsonIssueLink.has("inwardIssue")) {
                    String linkIssueId = jsonIssueLink.getJSONObject("inwardIssue").getString("id");
                    duplicateList.add(linkIssueId + " " + issueInfo.getIssueId());
                }
            }

            // 建立评论实体并关联到ISSUE
            JSONArray jsonCommentArr;
            if (!fields.isNull("comment")) {
                jsonCommentArr = fields.getJSONObject("comment").optJSONArray("comments");
                if (jsonCommentArr != null) {
                    int len = jsonCommentArr.length();
                    for (int i = 0; i < len; i++) {
                        JSONObject jsonComment = jsonCommentArr.getJSONObject(i);
                        String id = jsonComment.optString("id");
                        String body = jsonComment.optString("body");
                        Pair<String, Long> author = createUserNode(jsonComment, "author");
                        Pair<String, Long> updateAuthor = createUserNode(jsonComment, "updateAuthor");
                        String createdDate = jsonComment.optString("created");
                        String updatedDate = jsonComment.optString("updated");
                        if (author == null || updateAuthor == null) continue;
                        IssueCommentInfo comment = new IssueCommentInfo(id, body, author.getLeft(), updateAuthor.getLeft(), createdDate, updatedDate);
                        var commentNode = db.createNode(ISSUECOMMENT, JiraUtils.buildPropertyMap(comment));
                        var commentNodeId = commentNode.getId();
                        db.createRelationship(nodeId, commentNodeId, HAVE_ISSUE_COMMENT);
                        commentNode.setProperty(JiraGraphBuilder.ISSUECOMMENT_CREATOR_NAME, author.getLeft());
                        db.createRelationship(author.getRight(), commentNodeId, IS_CREATOR_OF_ISSUECOMMENT);
                        commentNode.setProperty(JiraGraphBuilder.ISSUECOMMENT_UPDATER_NAME, updateAuthor.getLeft());
                        db.createRelationship(updateAuthor.getRight(), commentNodeId, IS_UPDATER_OF_ISSUECOMMENT);
                    }
                }
            }

            // 建立补丁实体并关联到ISSUE
            JSONArray jsonHistoryArr;
            JSONObject root = new JSONObject(jsonContent);
            if (!root.isNull("changelog")) {
                jsonHistoryArr = root.getJSONObject("changelog").optJSONArray("histories");
                if (jsonHistoryArr != null) {
                    int hisNum = jsonHistoryArr.length();
                    for (int i = 0; i < hisNum; i++) {
                        JSONObject history = jsonHistoryArr.getJSONObject(i);
                        JSONArray items = history.optJSONArray("items");
                        if (items == null)
                            continue;
                        int itemNum = items.length();
                        for (int j = 0; j < itemNum; j++) {
                            JSONObject item = items.getJSONObject(j);
                            String to = item.optString("to");
                            String toString = item.optString("toString");
                            // not a patch
                            if (!to.matches("^\\d{1,19}$") || !toString.endsWith(".patch")) continue;

                            String patchName;
                            patchName = toString;
                            Pair<String, Long> author = createUserNode(history, "author");
                            String createdDate = history.optString("created");
                            if (createdDate == null)
                                createdDate = "";

                            PatchInfo patchInfo = new PatchInfo(to, patchName, "", createdDate);
                            var patchNode = db.createNode(PATCH, JiraUtils.buildPropertyMap(patchInfo));
                            var patchNodeId = patchNode.getId();
                            patchNodeMap.put(to, patchNode);
                            db.createRelationship(nodeId, patchNodeId, HAVE_PATCH);
                            if (author != null) {
                                patchNode.setProperty(JiraGraphBuilder.PATCH_CREATOR_NAME, author.getLeft());
                                db.createRelationship(author.getRight(), patchNodeId, IS_CREATOR_OF_PATCH);
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * 解析.json文件，返回IssueInfo。 返回的IssueInfo中不包含crearorName, assigneeName,
     * reporterName
     */
    private IssueInfo getIssueInfo(String jsonContent) throws JSONException {

        IssueInfo issueInfo = new IssueInfo();
        if (jsonContent != null && jsonContent.length() > 0) {
            JSONObject root = new JSONObject(jsonContent);
            String issueId = root.getString("id");
            String issueName = root.getString("key");

            JSONObject fields = root.getJSONObject("fields");

            String type = "";
            if (!fields.isNull("issuetype")) {
                type = fields.getJSONObject("issuetype").optString("name");
            }

            String fixVersions = getVersions(fields, "fixVersions");
            String versions = getVersions(fields, "versions");
            String resolution = "";
            if (!fields.isNull("resolution")) {
                resolution = fields.getJSONObject("resolution").optString("name");
            }

            String priority = "";
            if (!fields.isNull("priority")) {
                priority = fields.getJSONObject("priority").optString("name");
            }

            String status = "";
            if (!fields.isNull("status")) {
                status = fields.getJSONObject("status").optString("name");
            }

            String description = fields.optString("description");
            String summary = fields.optString("summary");

            String resolutionDate = fields.optString("resolutiondate");
            String createDate = fields.optString("created");
            String updateDate = fields.optString("updated");

            // labels
            StringBuilder labels = new StringBuilder();
            JSONArray jsonLabels = fields.optJSONArray("labels");
            if (jsonLabels != null) {
                int len = jsonLabels.length();
                for (int i = 0; i < len; i++) {
                    String label = jsonLabels.optString(i);
                    labels.append(label);
                    if (i != len - 1) {
                        labels.append(",");
                    }
                }
            }

            // components
            StringBuilder components = new StringBuilder();
            JSONArray jsonComponents = fields.optJSONArray("components");
            if (jsonComponents != null) {
                int len = jsonComponents.length();
                for (int i = 0; i < len; i++) {
                    String component = jsonComponents.getJSONObject(i).optString("name");
                    components.append(component);
                    if (i != len - 1) {
                        components.append(",");
                    }
                }
            }

            issueInfo.setIssueId(issueId);
            issueInfo.setIssueName(issueName);
            issueInfo.setType(type);
            issueInfo.setFixVersions(fixVersions);
            issueInfo.setResolution(resolution);
            issueInfo.setResolutionDate(resolutionDate);
            issueInfo.setPriority(priority);
            issueInfo.setLabels(labels.toString());
            issueInfo.setVersions(versions);
            issueInfo.setStatus(status);
            issueInfo.setComponents(components.toString());
            issueInfo.setDescription(description);
            issueInfo.setSummary(summary);
            issueInfo.setCreatedDate(createDate);
            issueInfo.setUpdatedDate(updateDate);
        }


        return issueInfo;
    }

    private String getVersions(JSONObject jsonObj, String key) throws JSONException {
        StringBuilder versions = new StringBuilder();
        JSONArray jsonVersions = jsonObj.optJSONArray(key);
        if (jsonVersions == null) {
            return versions.toString();
        }

        int versionNum = jsonVersions.length();
        for (int i = 0; i < versionNum; i++) {
            JSONObject fixVersion = jsonVersions.getJSONObject(i);
            String version = fixVersion.optString("name");
            versions.append(version);

            if (i != versionNum - 1) {
                versions.append(",");
            }
        }
        return versions.toString();
    }

    private Pair<String, Long> createUserNode(JSONObject jsonObj, String key) throws JSONException {
        if (jsonObj.isNull(key)) return null;

        JSONObject userJsonObj = jsonObj.getJSONObject(key);
        String name = userJsonObj.optString("name");
        String emailAddress = userJsonObj.optString("emailAddress");
        String displayName = userJsonObj.optString("displayName");
        boolean active = userJsonObj.optBoolean("active");

        IssueUserInfo user = new IssueUserInfo(name, decode(emailAddress), displayName, active);
        if (userNodeMap.containsKey(name)) return new ImmutablePair<>(name, userNodeMap.get(name));
        var nodeId = db.createNode(ISSUEUSER, JiraUtils.buildPropertyMap(user)).getId();
        userNodeMap.put(name, nodeId);
        return new ImmutablePair<>(name, nodeId);
    }

    private static String decode(String encodedEmailAddr) {
        if (encodedEmailAddr == null || encodedEmailAddr.isEmpty()) {
            return encodedEmailAddr;
        }

        String decodeATRes = encodedEmailAddr.replaceAll(" at ", " @ ");//decode 'at' to '@'
        String decodeATandDOTRes = decodeATRes.replaceAll(" dot ", " . ");//decode 'dot' to '.'
        //re-decode 'dot' for special cases when sequential dots occur and there is just a blank between two 'dot'
        //e.g.,"jianbin dot dot wang dot at pku dot edu dot cn"
        decodeATandDOTRes = decodeATandDOTRes.replaceAll(" dot ", " . ");//decode 'dot' to '.'

        return decodeATandDOTRes.replaceAll(" ", "");
    }

}
