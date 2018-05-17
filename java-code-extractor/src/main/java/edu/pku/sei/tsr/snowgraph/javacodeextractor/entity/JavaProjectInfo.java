package edu.pku.sei.tsr.snowgraph.javacodeextractor.entity;

import com.google.common.collect.ImmutableMap;
import edu.pku.sei.tsr.snowgraph.api.Neo4jServiceFactory;
import org.eclipse.jdt.core.dom.IMethodBinding;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JavaProjectInfo {
    private Map<String, JavaClassInfo> classInfoMap = new HashMap<>();
    private Map<String, JavaMethodInfo> methodInfoMap = new HashMap<>();
    private Map<String, JavaFieldInfo> fieldInfoMap = new HashMap<>();

    private Map<String, JavaClassInfo> newClassInfoMap = new HashMap<>();
    private Map<String, JavaMethodInfo> newMethodInfoMap = new HashMap<>();
    private Map<String, JavaFieldInfo> newFieldInfoMap = new HashMap<>();

    private Map<IMethodBinding, JavaMethodInfo> methodBindingMap = new HashMap<>();
    private Map<IMethodBinding, JavaMethodInfo> newMethodBindingMap = new HashMap<>();

    private <K, V> Set<V> mapFrom(Collection<K> set, Map<K, V> map1) {
        return set.stream()
            .map(map1::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    public void addClassInfo(JavaClassInfo info) {
        newClassInfoMap.put(info.getFullName(), info);
    }

    public void addMethodInfo(JavaMethodInfo info) {
        newMethodInfoMap.put(info.getFullName(), info);
        newMethodBindingMap.put(info.getMethodBinding(), info);
    }

    public void addFieldInfo(JavaFieldInfo info) {
        newFieldInfoMap.put(info.getFullName(), info);
    }

    private void buildRelations() {
        newClassInfoMap.values().forEach(classInfo -> {
            classInfo.setSuperClasses(findJavaClassInfo(classInfo.getSuperClassType()));
            classInfo.setSuperInterfaces(findJavaClassInfo(classInfo.getSuperInterfaceTypes()));
        });
        newMethodInfoMap.values().forEach(methodInfo -> {
            methodInfo.setBelongToSet(findJavaClassInfo(methodInfo.getBelongTo()));
            methodInfo.setFullParamSet(findJavaClassInfo(methodInfo.getFullParams()));
            methodInfo.setFullReturnTypeSet(findJavaClassInfo(methodInfo.getFullReturnType()));
            methodInfo.setThrowTypeSet(findJavaClassInfo(methodInfo.getThrowTypes()));
            methodInfo.setFullVariableSet(findJavaClassInfo(methodInfo.getFullVariables()));
            methodInfo.setMethodCallSet(mapFrom(methodInfo.getMethodCalls(), ImmutableMap.<IMethodBinding, JavaMethodInfo>builder().putAll(methodBindingMap).putAll(newMethodBindingMap).build()));
            methodInfo.setFieldAccessSet(findJavaFieldInfo(methodInfo.getFieldAccesses()));
        });
        newFieldInfoMap.values().forEach(fieldInfo -> {
            fieldInfo.setBelongToSet(findJavaClassInfo(fieldInfo.getBelongTo()));
            fieldInfo.setFullTypeSet(findJavaClassInfo(fieldInfo.getFullType()));
        });
    }

    public void buildRelationsAndSave(Neo4jServiceFactory serviceFactory) {
        buildRelations();

        var classInfoService = serviceFactory.createService(JavaClassInfo.class);
        var methodInfoService = serviceFactory.createService(JavaMethodInfo.class);
        var fieldInfoService = serviceFactory.createService(JavaFieldInfo.class);

        classInfoService.saveAll(newClassInfoMap.values());
        methodInfoService.saveAll(newMethodInfoMap.values());
        fieldInfoService.saveAll(newFieldInfoMap.values());

        classInfoMap.putAll(newClassInfoMap);
        methodInfoMap.putAll(newMethodInfoMap);
        fieldInfoMap.putAll(newFieldInfoMap);
        methodBindingMap.putAll(newMethodBindingMap);

        newClassInfoMap.clear();
        newMethodInfoMap.clear();
        newFieldInfoMap.clear();
        newMethodBindingMap.clear();
    }

    private Set<JavaClassInfo> findJavaClassInfo(String str) {
        String[] tokens = str.split("[^\\w.]+");
        return Arrays.stream(tokens)
            .flatMap(token -> Stream.of(classInfoMap.get(token), newClassInfoMap.get(token)))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    private Set<JavaFieldInfo> findJavaFieldInfo(String str) {
        String[] tokens = str.split("[^\\w.]+");
        return Arrays.stream(tokens)
            .flatMap(token -> Stream.of(fieldInfoMap.get(token), newFieldInfoMap.get(token)))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

}
