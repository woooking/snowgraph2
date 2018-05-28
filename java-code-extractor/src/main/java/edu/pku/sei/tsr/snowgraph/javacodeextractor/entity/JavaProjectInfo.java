package edu.pku.sei.tsr.snowgraph.javacodeextractor.entity;

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

    public void buildRelationsAndSave(GraphDatabaseService db) {
        try(var tx = db.beginTx()) {
            classInfoMap.values().forEach(info -> info.getNode(db));
            methodInfoMap.values().forEach(info -> info.getNode(db));
            fieldInfoMap.values().forEach(info -> info.getNode(db));

            methodInfoMap.values().forEach(info -> methodBindingMap.put(info.getMethodBinding(), info));
            classInfoMap.values().forEach(classInfo -> {
                findJavaClassInfo(classInfo.getSuperClassType()).forEach(superClassInfo -> classInfo.getNode(db).createRelationshipTo(superClassInfo.getNode(db), JavaCodeGraphBuilder.EXTEND));
                findJavaClassInfo(classInfo.getSuperInterfaceTypes()).forEach(superInterfaceInfo -> classInfo.getNode(db).createRelationshipTo(superInterfaceInfo.getNode(db), JavaCodeGraphBuilder.IMPLEMENT));
            });
            methodInfoMap.values().forEach(methodInfo -> {
                findJavaClassInfo(methodInfo.getBelongTo()).forEach(owner -> owner.getNode(db).createRelationshipTo(methodInfo.getNode(db), JavaCodeGraphBuilder.HAVE_METHOD));
                findJavaClassInfo(methodInfo.getFullParams()).forEach(param -> methodInfo.getNode(db).createRelationshipTo(param.getNode(db), JavaCodeGraphBuilder.PARAM_TYPE));
                findJavaClassInfo(methodInfo.getFullReturnType()).forEach(rt -> methodInfo.getNode(db).createRelationshipTo(rt.getNode(db), JavaCodeGraphBuilder.RETURN_TYPE));
                findJavaClassInfo(methodInfo.getThrowTypes()).forEach(tr -> methodInfo.getNode(db).createRelationshipTo(tr.getNode(db), JavaCodeGraphBuilder.THROW_TYPE));
                findJavaClassInfo(methodInfo.getFullVariables()).forEach(var -> methodInfo.getNode(db).createRelationshipTo(var.getNode(db), JavaCodeGraphBuilder.VARIABLE_TYPE));
                methodInfo.getMethodCalls().forEach(call -> {
                    if (methodBindingMap.containsKey(call))
                        methodInfo.getNode(db).createRelationshipTo(methodBindingMap.get(call).getNode(db), JavaCodeGraphBuilder.METHOD_CALL);
                });
                findJavaFieldInfo(methodInfo.getFieldAccesses()).forEach(access -> methodInfo.getNode(db).createRelationshipTo(access.getNode(db), JavaCodeGraphBuilder.FIELD_ACCESS));
            });
            fieldInfoMap.values().forEach(fieldInfo -> {
                findJavaClassInfo(fieldInfo.getBelongTo()).forEach(owner -> owner.getNode(db).createRelationshipTo(fieldInfo.getNode(db), JavaCodeGraphBuilder.HAVE_FIELD));
                findJavaClassInfo(fieldInfo.getFullType()).forEach(type -> fieldInfo.getNode(db).createRelationshipTo(type.getNode(db), JavaCodeGraphBuilder.FIELD_TYPE));
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
