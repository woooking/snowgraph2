package edu.pku.sei.tsr.snowgraph.codementiondetector;

import edu.pku.sei.tsr.snowgraph.api.InitRegistry;
import edu.pku.sei.tsr.snowgraph.api.PostInitRegistry;
import edu.pku.sei.tsr.snowgraph.api.PreInitRegistry;
import edu.pku.sei.tsr.snowgraph.api.context.SnowGraphContext;
import edu.pku.sei.tsr.snowgraph.api.event.ChangeEvent;
import edu.pku.sei.tsr.snowgraph.api.event.LoadEvent;
import edu.pku.sei.tsr.snowgraph.api.event.ShutDownEvent;
import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jNode;
import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jService;
import edu.pku.sei.tsr.snowgraph.api.plugin.SnowGraphPlugin;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.jsoup.Jsoup;
import org.neo4j.graphdb.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 建立代码实体和其它类型的实体之间的关联关系
 * detectCodeMentionInFlossDocuments: 建立英文文档和代码之间的关联关系
 * detectCodeMentionInDocx: 建立中文文档和代码之间的关联关系（文档里面提到了这个代码）
 * detectCodeMentionInDiff: 建立commits和代码之间的关联关系（add, modify, delete）
 * <p>
 * Preconditions: 已经运行过CodeTokenizer了。
 */

public class CodeMentionDetector implements SnowGraphPlugin {

    public static final RelationshipType CODE_MENTION = RelationshipType.withName("codeMention");
    public static final RelationshipType ADD = RelationshipType.withName("add");
    public static final RelationshipType MODIFY = RelationshipType.withName("modify");
    public static final RelationshipType DELETE = RelationshipType.withName("delete");

    @Override
    public List<String> dependsOn() {
        return List.of("edu.pku.sei.tsr.snowgraph.codetokenizer.CodeTokenizer");
    }

    @Override
    public List<String> optionalDependsOn() {
        return List.of();
    }

    @Override
    public int order() {
        return SnowGraphPlugin.MINER;
    }

    @Override
    public void preInit(PreInitRegistry preInitRegistry) {

    }

    @Override
    public void init(InitRegistry initRegistry) {

    }

    @Override
    public void postInit(PostInitRegistry postInitRegistry) {

    }

    @Override
    public void onLoad(LoadEvent event) {

    }

    @Override
    public void onShutDown(ShutDownEvent event) {

    }

    @Override
    public void run(SnowGraphContext context) {
        try {
            process(context.getNeo4jService());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(SnowGraphContext context, Collection<ChangeEvent<Path>> changedFiles, Collection<ChangeEvent<Long>> changedNodes, Collection<ChangeEvent<Long>> changedRelationships) {

    }

    public void process(Neo4jService db) throws IOException {
        detectCodeMentionInFlossDocuments(db);
        detectCodeMentionInDocx(db);
        detectCodeMentionInDiff(db);
    }

    private void detectCodeMentionInFlossDocuments(Neo4jService db) {
        CodeIndexes codeIndexes = new CodeIndexes(db);
        var textNodes = new HashSet<Neo4jNode>();
        try (Transaction tx = db.beginTx()) {
            db.getAllNodes().forEach(node -> {
                if (!node.hasProperty("isText") || !(boolean) node.getProperty("isText")) return;
                if (node.hasLabel("Class") || node.hasLabel("Method") || node.hasLabel("Field")) return;
                textNodes.add(node);
            });
            textNodes.forEach(srcNode -> {
                String text = (String) srcNode.getProperty("_title");
                text += " " + srcNode.getProperty("_text");
                String content = Jsoup.parse(text).text();
                Set<String> lexes = new HashSet<>();
                Collections.addAll(lexes, content.toLowerCase().split("\\W+"));
                var resultNodes = new HashSet<Neo4jNode>();
                //类/接口
                for (String typeShortName : codeIndexes.typeShortNameMap.keySet())
                    if (lexes.contains(typeShortName.toLowerCase()))
                        for (long id : codeIndexes.typeShortNameMap.get(typeShortName))
                            resultNodes.add(db.getNodeById(id));

                for (String methodShortName : codeIndexes.methodShortNameMap.keySet()) {
                    //后接小括号，不要构造函数
                    if (methodShortName.charAt(0) < 'a' || methodShortName.charAt(0) > 'z' || !(lexes.contains(methodShortName.toLowerCase()) && content.contains(methodShortName + "(")))
                        continue;
                    boolean flag = false;
                    //无歧义
                    if (codeIndexes.methodShortNameMap.get(methodShortName).size() == 1) {
                        for (long id : codeIndexes.methodShortNameMap.get(methodShortName))
                            resultNodes.add(db.getNodeById(id));
                        flag = true;
                    }
                    //主类在
                    for (long methodNodeId : codeIndexes.methodShortNameMap.get(methodShortName)) {
                        var methodNode = db.getNodeById(methodNodeId);
                        if (resultNodes.contains(methodNode.getRelationships("haveMethod", Direction.INCOMING).iterator().next().getStartNode())) {
                            resultNodes.add(methodNode);
                            flag = true;
                        }
                    }
                    //歧义不多
                    if (!flag && codeIndexes.methodShortNameMap.get(methodShortName).size() <= 5)
                        for (long id : codeIndexes.methodShortNameMap.get(methodShortName))
                            resultNodes.add(db.getNodeById(id));
                }
                for (var rNode : resultNodes) {
                    db.createRelationship(srcNode.getId(), rNode.getId(), CODE_MENTION);
                }

            });
            tx.success();
        }
    }

    private void detectCodeMentionInDocx(Neo4jService db) throws IOException {
        final String CONTENT_FIELD = "content";
        final String ID_FIELD = "id";
        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter iwriter = new IndexWriter(directory, config);
        try (Transaction tx = db.beginTx()) {
            db.findNodes("Docx").forEach(docxNode -> {
                String content = (String) docxNode.getProperty("content");
                content = content.replaceAll("\\W+", " ").toLowerCase();
                Document document = new Document();
                document.add(new StringField(ID_FIELD, "" + docxNode.getId(), Field.Store.YES));
                document.add(new TextField(CONTENT_FIELD, content, Field.Store.YES));
                try {
                    iwriter.addDocument(document);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            tx.success();
        }
        iwriter.close();
        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);
        QueryParser parser = new QueryParser(CONTENT_FIELD, analyzer);
        try (Transaction tx = db.beginTx()) {
            db.findNodes("Method").forEach(methodNode -> {
                String name = (String) methodNode.getProperty("fullName");
                String[] eles = name.substring(0, name.indexOf("(")).split("\\.");
                if (eles[eles.length - 1].equals(eles[eles.length - 2])) return;
                String q = eles[eles.length - 1].toLowerCase() + " AND " + eles[eles.length - 2].toLowerCase();
                try {
                    var query = parser.parse(q);
                    ScoreDoc[] hits = isearcher.search(query, 10000).scoreDocs;
                    if (hits.length > 0 && hits.length < 20) {
                        for (ScoreDoc hit : hits) {
                            var docxNode = db.getNodeById(Long.parseLong(ireader.document(hit.doc).get(ID_FIELD)));
                            db.createRelationship(methodNode.getId(), docxNode.getId(), CODE_MENTION);
                        }
                    }
                } catch (org.apache.lucene.queryparser.classic.ParseException | IOException e) {
                    e.printStackTrace();
                }

            });
            tx.success();
        }
        try (Transaction tx = db.beginTx()) {
            db.findNodes("Class").forEach(classNode -> {
                String name = (String) classNode.getProperty("name");
                String q = name.toLowerCase();
                try {
                    var query = parser.parse(q);
                    ScoreDoc[] hits = isearcher.search(query, 10000).scoreDocs;
                    if (hits.length > 0 && hits.length < 20) {
                        for (ScoreDoc hit : hits) {
                            var docxNode = db.getNodeById(Long.parseLong(ireader.document(hit.doc).get(ID_FIELD)));
                            db.createRelationship(classNode.getId(), docxNode.getId(), CODE_MENTION);
                        }
                    }
                } catch (org.apache.lucene.queryparser.classic.ParseException | IOException e) {
                    e.printStackTrace();
                }

            });
            tx.success();
        }
    }

    private void detectCodeMentionInDiff(Neo4jService db) {
        Map<String, Neo4jNode> classMap = new HashMap<>();
        Pattern pattern = Pattern.compile("(ADD|MODIFY|DELETE)\\s+(\\S+)\\s+to\\s+(\\S+)");
        try (Transaction tx = db.beginTx()) {
            db.findNodes("Class").forEach(classNode -> {
                String fullName = (String) classNode.getProperty("fullName");
                String sig = fullName.replace('.', '/') + ".java";
                classMap.put(sig, classNode);
            });
            db.findNodes("Commit").forEach(commit -> {
                String diffSummary = (String) commit.getProperty("diffSummary");
                Matcher matcher = pattern.matcher(diffSummary);
                while (matcher.find()) {
                    String relStr = matcher.group(1);
                    String srcPath = matcher.group(2);
                    String dstPath = matcher.group(3);
                    RelationshipType relType = relStr.equals("ADD") ? ADD : relStr.equals("MODIFY") ? MODIFY : DELETE;
                    for (String sig : classMap.keySet())
                        if (srcPath.contains(sig) || dstPath.contains(sig)) {
                            db.createRelationship(commit.getId(), classMap.get(sig).getId(), relType);
                        }
                }
            });
            tx.success();
        }
    }

}
