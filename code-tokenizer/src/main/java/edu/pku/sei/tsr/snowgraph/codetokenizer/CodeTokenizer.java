package edu.pku.sei.tsr.snowgraph.codetokenizer;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import edu.pku.sei.tsr.snowgraph.api.InitRegistry;
import edu.pku.sei.tsr.snowgraph.api.PostInitRegistry;
import edu.pku.sei.tsr.snowgraph.api.PreInitRegistry;
import edu.pku.sei.tsr.snowgraph.api.context.SnowGraphContext;
import edu.pku.sei.tsr.snowgraph.api.event.ChangeEvent;
import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jNode;
import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jService;
import edu.pku.sei.tsr.snowgraph.api.plugin.SnowGraphPlugin;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Transaction;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class CodeTokenizer implements SnowGraphPlugin {
    @Override
    public List<String> dependsOn() {
        return List.of();
    }

    @Override
    public List<String> optionalDependsOn() {
        return List.of("edu.pku.sei.tsr.snowgraph.javacodeextractor.JavaCodeExtractor");
    }

    @Override
    public int order() {
        return SnowGraphPlugin.EXTRACTOR;
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
    public void run(SnowGraphContext context) {
        process(context.getNeo4jService());
    }

    @Override
    public void update(SnowGraphContext context, Collection<ChangeEvent<Path>> changedFiles, Collection<ChangeEvent<Long>> changedNodes, Collection<ChangeEvent<Long>> changedRelationships) {
        update(context.getNeo4jService(), changedNodes);
    }

    public static final String IS_TEXT = "isText";
    public static final String TITLE = "_title";
    public static final String TEXT = "_text";
    public static final String CODE_TOKENS = "codeTokens";

    public static void process(Neo4jService db) {
        try (Transaction tx = db.beginTx()) {
            var nodes = db.findNodes("Class");
            nodes.forEach(CodeTokenizer::codeTokenExtraction);
            nodes = db.findNodes("Method");
            nodes.forEach(CodeTokenizer::codeTokenExtraction);
            nodes = db.findNodes("Field");
            nodes.forEach(CodeTokenizer::codeTokenExtraction);
            tx.success();
        }

        List<List<Neo4jNode>> nodeSegs;

        try (Transaction tx = db.beginTx()) {
            var nodes = db.getAllNodes();
            nodeSegs = nodes.collect(CountCollector.create(1000));
            tx.success();
        }
        for (List<Neo4jNode> list : nodeSegs)
            try (Transaction tx = db.beginTx()) {
                list.forEach(CodeTokenizer::flossTextExtraction);
                tx.success();
            }
    }

    public static void update(Neo4jService db, Collection<ChangeEvent<Long>> changedNodes) {
        List<Neo4jNode> neo4jNodes;
        try(Transaction tx = db.beginTx()) {
            neo4jNodes = changedNodes.stream()
                .map(p -> new ChangeEvent<>(p.getType(), db.getNodeById(p.getInstance())))
                .filter(n -> n.getType() != ChangeEvent.Type.DELETED)
                .map(ChangeEvent::getInstance)
                .collect(Collectors.toList());
        }

        try (Transaction tx = db.beginTx()) {
            neo4jNodes.stream()
                .filter(n -> n.hasLabel("Class") || n.hasLabel("Method") || n.hasLabel("Field"))
                .forEach(CodeTokenizer::codeTokenExtraction);
            tx.success();
        }

        List<List<Neo4jNode>> nodeSegs;

        try (Transaction tx = db.beginTx()) {
            nodeSegs = neo4jNodes.stream().collect(CountCollector.create(1000));
            tx.success();
        }
        for (List<Neo4jNode> list : nodeSegs)
            try (Transaction tx = db.beginTx()) {
                list.forEach(CodeTokenizer::flossTextExtraction);
                tx.success();
            }
    }

    private static void flossTextExtraction(Neo4jNode node) {
        if (node.hasProperty(TITLE))
            node.removeProperty(TITLE);
        if (node.hasProperty(TEXT))
            node.removeProperty(TEXT);
        if (node.hasProperty(IS_TEXT))
            node.removeProperty(IS_TEXT);

        if (node.hasLabel("Class") || node.hasLabel("Method")) {
            node.setProperty(TITLE, node.getProperty("fullName"));
            node.setProperty(TEXT, node.getProperty("content"));
            node.setProperty(IS_TEXT, true);
        }

        if (node.hasLabel("Commit")) {
            node.setProperty(TITLE, "name");
            node.setProperty(TEXT, node.getProperty("message"));
            node.setProperty(IS_TEXT, true);
        }

        //TODO: ISSUE, EMAIL, STACKOVERFLOW

    }

    private static void codeTokenExtraction(Neo4jNode node) {
        String content = "";
        if (node.hasProperty("fullName"))
            content += (String) node.getProperty("name");
        Set<String> tokens = tokenization(content);
        node.setProperty(CODE_TOKENS, StringUtils.join(tokens, " "));
    }

    public static Set<String> tokenization(String content) {
        Set<String> r = new HashSet<>();
        content = content.replaceAll("[^\\u4e00-\\u9fa5\\w]+", " ");
        content = content.trim();
        if (content.length() == 0) return r;
        List<Term> terms = HanLP.segment(content);
        for (Term term : terms) {
            String word = term.word;
            if (word.matches("\\w+")) {
                List<String> tokens = englishTokenization(word);
                r.add(word.toLowerCase());
                r.addAll(tokens);
            } else if (word.matches("[\\u4e00-\\u9fa5]+"))
                r.add(word);
        }
        return r;
    }

    /**
     * IndexLDAException_conf --> indexldaexception + index + lda + exception + conf
     */
    private static List<String> englishTokenization(String word) {
        List<String> tokens = new ArrayList<>();
        String[] eles = word.trim().split("[^A-Za-z]+");
        for (String e : eles) {
            List<String> humps = camelSplit(e);
            tokens.add(e.toLowerCase());
            if (humps.size() > 0)
                tokens.addAll(humps);
        }
        return tokens;
    }

    /**
     * IndexLDAException --> index + lda + exception
     */
    private static List<String> camelSplit(String e) {
        List<String> r = new ArrayList<>();
        Matcher m = Pattern.compile("^([a-z]+)|([A-Z][a-z]+)|([A-Z]+(?=([A-Z]|$)))").matcher(e);
        if (m.find()) {
            String s = m.group().toLowerCase();
            r.add(s);
            if (s.length() < e.length())
                r.addAll(camelSplit(e.substring(s.length())));
        }
        return r;
    }
}
