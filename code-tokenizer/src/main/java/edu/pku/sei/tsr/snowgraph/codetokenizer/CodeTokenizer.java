package edu.pku.sei.tsr.snowgraph.codetokenizer;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import edu.pku.sei.tsr.snowgraph.api.*;
import edu.pku.sei.tsr.snowgraph.api.context.SnowGraphContext;
import edu.pku.sei.tsr.snowgraph.api.plugin.SnowGraphPlugin;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.*;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    }

    @Override
    public void update(SnowGraphContext context, Collection<ChangeEvent<Path>> changeEvents) {

    }

    public static final String IS_TEXT = "isText";
    public static final String TITLE = "_title";
    public static final String TEXT = "_text";
    public static final String CODE_TOKENS = "codeTokens";

    public static void process(GraphDatabaseService db) {
        try (Transaction tx = db.beginTx()) {
            ResourceIterator<Node> nodes = db.findNodes(Label.label("class"));
            nodes.stream().forEach(node -> codeTokenExtraction(node));
            nodes = db.findNodes(Label.label("method"));
            nodes.stream().forEach(node -> codeTokenExtraction(node));
            nodes = db.findNodes(Label.label("field"));
            nodes.stream().forEach(node -> codeTokenExtraction(node));
            tx.success();
        }

        List<List<Node>> nodeSegs = new ArrayList<>();

        try (Transaction tx = db.beginTx()) {
            ResourceIterator<Node> nodes = db.getAllNodes().iterator();
            List<Node> list = new ArrayList<>();
            while (nodes.hasNext()) {
                Node node = nodes.next();
                if (list.size() < 1000)
                    list.add(node);
                else {
                    nodeSegs.add(list);
                    list = new ArrayList<>();
                }
            }
            if (list.size() > 0)
                nodeSegs.add(list);
            tx.success();
        }
        for (List<Node> list : nodeSegs)
            try (Transaction tx = db.beginTx()) {
                for (Node node : list)
                    flossTextExtraction(node);
                tx.success();
            }

        db.shutdown();
    }

    private static void flossTextExtraction(Node node) {
        if (node.hasProperty(TITLE))
            node.removeProperty(TITLE);
        if (node.hasProperty(TEXT))
            node.removeProperty(TEXT);
        if (node.hasProperty(IS_TEXT))
            node.removeProperty(IS_TEXT);

        if (node.hasLabel(Label.label("class")) || node.hasLabel(Label.label("method"))) {
            node.setProperty(TITLE, node.getProperty("fullName"));
            node.setProperty(TEXT, node.getProperty("content"));
            node.setProperty(IS_TEXT, true);
        }

        if (node.hasLabel(Label.label("commit"))) {
            node.setProperty(TITLE, "name");
            node.setProperty(TEXT, node.getProperty("message"));
            node.setProperty(IS_TEXT, true);
        }

        //TODO: ISSUE, EMAIL, STACKOVERFLOW

    }

    private static void codeTokenExtraction(Node node) {
        String content = "";
        if (node.hasProperty("fullName"))
            content += (String) node.getProperty("name");
        Set<String> tokens = tokenization(content);
        node.setProperty(CODE_TOKENS, StringUtils.join(tokens, " "));
    }

    public static Set<String> tokenization(String content) {
        Set<String> r = new HashSet<>();
        content = content.replaceAll("[^\\u4e00-\\u9fa5\\w]+", " ");
        content.trim();
        if (content.length() == 0)
            return r;
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
