package edu.pku.sei.tsr.snowgraph.codementiondetector;

import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jService;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Transaction;

import java.util.*;
import java.util.stream.Collectors;

public class CodeIndexes {

    public Map<String, Long> typeMap = new HashMap<>();
    private Map<Long, String> idToTypeNameMap = new HashMap<>();
    private Map<String, Set<Long>> methodMap = new HashMap<>();
    private Map<Long, String> idToMethodNameMap = new HashMap<>();
    public Map<String, Set<Long>> typeShortNameMap = new HashMap<>();
    private Map<String, Set<Long>> methodMidNameMap = new HashMap<>();
    public Map<String, Set<Long>> methodShortNameMap = new HashMap<>();

    public CodeIndexes(Neo4jService db) {
        try (Transaction tx = db.beginTx()) {
            var codeNodes = db.getAllNodes()
                .filter(n -> n.hasLabel("Class") || n.hasLabel("Method"))
                .collect(Collectors.toSet());

            for (var codeNode : codeNodes) {
                String name = "";
                boolean type = true;
                if (codeNode.hasLabel("Class")) name = (String) codeNode.getProperty("fullName");
                if (codeNode.hasLabel("Method")) {
                    //System.out.println(codeNode.getProperty("fullName"));
                    //TODO:存在重复节点没有任何边关系，出现异常需解决
                    if (codeNode.hasRelationship("haveMethod", Direction.INCOMING)) {
                        name = codeNode.getRelationships("haveMethod", Direction.INCOMING).findFirst().get().getStartNode().getProperty("fullName")
                            + "." + codeNode.getProperty("name");
                        type = false;
                    }

                }
                if (name.contains("$"))
                    continue;
                if (type) {
                    typeMap.put(name, codeNode.getId());
                    idToTypeNameMap.put(codeNode.getId(), name);
                    String shortName = name;
                    int p = shortName.lastIndexOf('.');
                    if (p > 0)
                        shortName = shortName.substring(p + 1, shortName.length());
                    if (!typeShortNameMap.containsKey(shortName))
                        typeShortNameMap.put(shortName, new HashSet<>());
                    typeShortNameMap.get(shortName).add(codeNode.getId());
                } else {
                    if (!methodMap.containsKey(name))
                        methodMap.put(name, new HashSet<>());
                    methodMap.get(name).add(codeNode.getId());
                    idToMethodNameMap.put(codeNode.getId(), name);
                    int p1 = name.lastIndexOf('.');
                    int p2 = name.lastIndexOf('.', p1 - 1);
                    String midName, shortName;
                    if (p2 > 0) {
                        midName = name.substring(p2 + 1);
                        shortName = name.substring(p1 + 1);
                    } else {
                        midName = name;
                        shortName = name.substring(p1 + 1);
                    }
                    if (!methodMidNameMap.containsKey(midName))
                        methodMidNameMap.put(midName, new HashSet<>());
                    methodMidNameMap.get(midName).add(codeNode.getId());
                    if (!methodShortNameMap.containsKey(shortName))
                        methodShortNameMap.put(shortName, new HashSet<>());
                    methodShortNameMap.get(shortName).add(codeNode.getId());
                }
            }

            tx.success();
        }
    }

}