package edu.pku.sei.tsr.snowgraph.javacodeextractor.entity;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jNode;
import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jRelationship;
import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jService;
import edu.pku.sei.tsr.snowgraph.javacodeextractor.JavaCodeGraphBuilder;
import org.eclipse.jdt.core.dom.IMethodBinding;

import java.io.File;
import java.io.Serializable;
import java.util.*;


public class JavaProjectInfo implements Serializable {
    private Multimap<String, JavaClassInfo> classesInFile = HashMultimap.create();
    private Multimap<String, JavaMethodInfo> methodsInFile = HashMultimap.create();
    private Multimap<String, JavaFieldInfo> fieldsInFile = HashMultimap.create();

    private Map<String, JavaClassInfo> classInfoMap = new HashMap<>();
    private Map<String, JavaMethodInfo> methodInfoMap = new HashMap<>();
    private Map<String, JavaFieldInfo> fieldInfoMap = new HashMap<>();

    private Map<String, JavaClassInfo> newClassInfoMap = new HashMap<>();
    private Map<String, JavaMethodInfo> newMethodInfoMap = new HashMap<>();
    private Map<String, JavaFieldInfo> newFieldInfoMap = new HashMap<>();

    private Map<String, JavaMethodInfo> methodBindingMap = new HashMap<>();

    private void removeNode(Neo4jService db, long nodeId) {
        var node = db.getNodeById(nodeId);
        node.getRelationships().forEach(Neo4jRelationship::delete);
        node.delete();
    }

    public void removeFiles(Neo4jService db, Collection<File> deletedFiles) {
        try(var tx = db.beginTx()) {
            deletedFiles.forEach(deletedFile -> {
                classesInFile.get(deletedFile.getPath()).forEach(info -> {
                    classInfoMap.remove(info.getFullName());
                    removeNode(db, info.getNodeId());
                });
                methodsInFile.get(deletedFile.getPath()).forEach(info -> {
                    methodInfoMap.remove(info.getFullName());
                    removeNode(db, info.getNodeId());
                });
                fieldsInFile.get(deletedFile.getPath()).forEach(info -> {
                    fieldInfoMap.remove(info.getFullName());
                    removeNode(db, info.getNodeId());
                });
                classesInFile.removeAll(deletedFile.getPath());
                methodsInFile.removeAll(deletedFile.getPath());
                fieldsInFile.removeAll(deletedFile.getPath());
            });
            tx.success();
        }
    }

    public void addClassInfo(String path, JavaClassInfo info) {
        classesInFile.put(path, info);
        newClassInfoMap.put(info.getFullName(), info);
    }

    public void addMethodInfo(String path, JavaMethodInfo info) {
        methodsInFile.put(path, info);
        newMethodInfoMap.put(info.getFullName(), info);
        methodBindingMap.put(info.getMethodBinding(), info);
    }

    public void addFieldInfo(String path, JavaFieldInfo info) {
        fieldsInFile.put(path, info);
        newFieldInfoMap.put(info.getFullName(), info);
    }

    public void buildRelationsAndSave(Neo4jService db) {
        try(var tx = db.beginTx()) {
            newClassInfoMap.values().forEach(classInfo -> {
                findJavaClassInfo(classInfo.getSuperClassType()).forEach(superClassInfo -> db.createRelationship(classInfo.getNodeId(), superClassInfo.getNodeId(), JavaCodeGraphBuilder.EXTEND));
                findJavaClassInfo(classInfo.getSuperInterfaceTypes()).forEach(superInterfaceInfo -> db.createRelationship(classInfo.getNodeId(), superInterfaceInfo.getNodeId(), JavaCodeGraphBuilder.IMPLEMENT));
            });
            newMethodInfoMap.values().forEach(methodInfo -> {
                findJavaClassInfo(methodInfo.getBelongTo()).forEach(owner -> db.createRelationship(owner.getNodeId(), methodInfo.getNodeId(), JavaCodeGraphBuilder.HAVE_METHOD));
                findJavaClassInfo(methodInfo.getFullParams()).forEach(param -> db.createRelationship(methodInfo.getNodeId(), param.getNodeId(), JavaCodeGraphBuilder.PARAM_TYPE));
                findJavaClassInfo(methodInfo.getFullReturnType()).forEach(rt -> db.createRelationship(methodInfo.getNodeId(), rt.getNodeId(), JavaCodeGraphBuilder.RETURN_TYPE));
                findJavaClassInfo(methodInfo.getThrowTypes()).forEach(tr -> db.createRelationship(methodInfo.getNodeId(), tr.getNodeId(), JavaCodeGraphBuilder.THROW_TYPE));
                findJavaClassInfo(methodInfo.getFullVariables()).forEach(var -> db.createRelationship(methodInfo.getNodeId(), var.getNodeId(), JavaCodeGraphBuilder.VARIABLE_TYPE));
                methodInfo.getMethodCalls().forEach(call -> {
                    if (methodBindingMap.containsKey(call)) db.createRelationship(methodInfo.getNodeId(), methodBindingMap.get(call).getNodeId(), JavaCodeGraphBuilder.METHOD_CALL);
                });
                findJavaFieldInfo(methodInfo.getFieldAccesses()).forEach(access -> db.createRelationship(methodInfo.getNodeId(), access.getNodeId(), JavaCodeGraphBuilder.FIELD_ACCESS));
            });
            newFieldInfoMap.values().forEach(fieldInfo -> {
                findJavaClassInfo(fieldInfo.getBelongTo()).forEach(owner -> db.createRelationship(owner.getNodeId(), fieldInfo.getNodeId(), JavaCodeGraphBuilder.HAVE_FIELD));
                findJavaClassInfo(fieldInfo.getFullType()).forEach(type -> db.createRelationship(fieldInfo.getNodeId(), type.getNodeId(), JavaCodeGraphBuilder.FIELD_TYPE));
            });
            tx.success();
            classInfoMap.putAll(newClassInfoMap);
            methodInfoMap.putAll(newMethodInfoMap);
            fieldInfoMap.putAll(newFieldInfoMap);
            newClassInfoMap.clear();
            newMethodInfoMap.clear();
            newFieldInfoMap.clear();
        }
    }

    private Set<JavaClassInfo> findJavaClassInfo(String str) {
        Set<JavaClassInfo> r = new HashSet<>();
        String[] tokens = str.split("[^\\w\\.]+");
        for (String token : tokens) {
            if (classInfoMap.containsKey(token)) r.add(classInfoMap.get(token));
            if (newClassInfoMap.containsKey(token)) r.add(newClassInfoMap.get(token));
        }
        return r;
    }

    private Set<JavaFieldInfo> findJavaFieldInfo(String str) {
        Set<JavaFieldInfo> r = new HashSet<>();
        String[] tokens = str.split("[^\\w\\.]+");
        for (String token : tokens) {
            if (fieldInfoMap.containsKey(token)) r.add(fieldInfoMap.get(token));
            if (newFieldInfoMap.containsKey(token)) r.add(newFieldInfoMap.get(token));
        }
        return r;
    }

}
