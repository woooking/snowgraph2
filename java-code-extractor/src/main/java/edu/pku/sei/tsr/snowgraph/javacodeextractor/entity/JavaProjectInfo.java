package edu.pku.sei.tsr.snowgraph.javacodeextractor.entity;

import edu.pku.sei.tsr.snowgraph.api.Neo4jService;
import edu.pku.sei.tsr.snowgraph.javacodeextractor.JavaCodeGraphBuilder;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class JavaProjectInfo {

    private Map<String, JavaClassInfo> classInfoMap = new HashMap<>();
    private Map<String, JavaMethodInfo> methodInfoMap = new HashMap<>();
    private Map<String, JavaFieldInfo> fieldInfoMap = new HashMap<>();

    private Map<IMethodBinding, JavaMethodInfo> methodBindingMap = new HashMap<>();

    public void addClassInfo(JavaClassInfo info) {
        classInfoMap.put(info.getFullName(), info);
    }

    public void addMethodInfo(JavaMethodInfo info) {
        methodInfoMap.put(info.getFullName(), info);
        methodBindingMap.put(info.getMethodBinding(), info);
    }

    public void addFieldInfo(JavaFieldInfo info) {
        fieldInfoMap.put(info.getFullName(), info);
    }

    public void buildRelationsAndSave(Neo4jService db) {
        try(var tx = db.beginTx()) {
            methodInfoMap.values().forEach(info -> methodBindingMap.put(info.getMethodBinding(), info));
            classInfoMap.values().forEach(classInfo -> {
                findJavaClassInfo(classInfo.getSuperClassType()).forEach(superClassInfo -> db.createRelationship(classInfo.getNodeId(), superClassInfo.getNodeId(), JavaCodeGraphBuilder.EXTEND));
                findJavaClassInfo(classInfo.getSuperInterfaceTypes()).forEach(superInterfaceInfo -> db.createRelationship(classInfo.getNodeId(), superInterfaceInfo.getNodeId(), JavaCodeGraphBuilder.IMPLEMENT));
            });
            methodInfoMap.values().forEach(methodInfo -> {
                findJavaClassInfo(methodInfo.getBelongTo()).forEach(owner -> db.createRelationship(owner.getNodeId(), methodInfo.getNodeId(), JavaCodeGraphBuilder.HAVE_METHOD));
                findJavaClassInfo(methodInfo.getFullParams()).forEach(param -> db.createRelationship(methodInfo.getNodeId(), param.getNodeId(), JavaCodeGraphBuilder.PARAM_TYPE));
                findJavaClassInfo(methodInfo.getFullReturnType()).forEach(rt -> db.createRelationship(methodInfo.getNodeId(), rt.getNodeId(), JavaCodeGraphBuilder.RETURN_TYPE));
                findJavaClassInfo(methodInfo.getThrowTypes()).forEach(tr -> db.createRelationship(methodInfo.getNodeId(), tr.getNodeId(), JavaCodeGraphBuilder.THROW_TYPE));
                findJavaClassInfo(methodInfo.getFullVariables()).forEach(var -> db.createRelationship(methodInfo.getNodeId(), var.getNodeId(), JavaCodeGraphBuilder.VARIABLE_TYPE));
                methodInfo.getMethodCalls().forEach(call -> {
                    if (methodBindingMap.containsKey(call))
                        db.createRelationship(methodInfo.getNodeId(), methodBindingMap.get(call).getNodeId(), JavaCodeGraphBuilder.METHOD_CALL);
                });
                findJavaFieldInfo(methodInfo.getFieldAccesses()).forEach(access -> db.createRelationship(methodInfo.getNodeId(), access.getNodeId(), JavaCodeGraphBuilder.FIELD_ACCESS));
            });
            fieldInfoMap.values().forEach(fieldInfo -> {
                findJavaClassInfo(fieldInfo.getBelongTo()).forEach(owner -> db.createRelationship(owner.getNodeId(), fieldInfo.getNodeId(), JavaCodeGraphBuilder.HAVE_FIELD));
                findJavaClassInfo(fieldInfo.getFullType()).forEach(type -> db.createRelationship(fieldInfo.getNodeId(), type.getNodeId(), JavaCodeGraphBuilder.FIELD_TYPE));
            });
            tx.success();
        }
    }

    private Set<JavaClassInfo> findJavaClassInfo(String str) {
        Set<JavaClassInfo> r = new HashSet<>();
        String[] tokens = str.split("[^\\w\\.]+");
        for (String token : tokens)
            if (classInfoMap.containsKey(token))
                r.add(classInfoMap.get(token));
        return r;
    }

    private Set<JavaFieldInfo> findJavaFieldInfo(String str) {
        Set<JavaFieldInfo> r = new HashSet<>();
        String[] tokens = str.split("[^\\w\\.]+");
        for (String token : tokens)
            if (fieldInfoMap.containsKey(token))
                r.add(fieldInfoMap.get(token));
        return r;
    }

}
