package edu.pku.sei.tsr.snowgraph.javacodeextractor.entity;

import edu.pku.sei.tsr.snowgraph.api.Neo4jServiceFactory;
import org.eclipse.jdt.core.dom.IMethodBinding;

import java.util.*;
import java.util.stream.Collectors;


public class JavaProjectInfo {
    private Map<String, JavaClassInfo> classInfoMap = new HashMap<>();
    private Map<String, JavaMethodInfo> methodInfoMap = new HashMap<>();
    private Map<String, JavaFieldInfo> fieldInfoMap = new HashMap<>();

    private Map<IMethodBinding, JavaMethodInfo> methodBindingMap = new HashMap<>();

    private <K, V> Set<V> mapFrom(Collection<K> set, Map<K, V> map) {
        return set.stream()
            .map(map::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

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

    public void buildRelations() {
        methodInfoMap.values().forEach(info->methodBindingMap.put(info.getMethodBinding(),info));
        classInfoMap.values().forEach(classInfo -> {
            classInfo.setSuperClasses(findJavaClassInfo(classInfo.getSuperClassType()));
            classInfo.setSuperInterfaces(findJavaClassInfo(classInfo.getSuperInterfaceTypes()));
        });
        methodInfoMap.values().forEach(methodInfo -> {
            methodInfo.setBelongToSet(findJavaClassInfo(methodInfo.getBelongTo()));
            methodInfo.setFullParamSet(findJavaClassInfo(methodInfo.getFullParams()));
            methodInfo.setFullReturnTypeSet(findJavaClassInfo(methodInfo.getFullReturnType()));
            methodInfo.setThrowTypeSet(findJavaClassInfo(methodInfo.getThrowTypes()));
            methodInfo.setFullVariableSet(findJavaClassInfo(methodInfo.getFullVariables()));
            methodInfo.setMethodCallSet(mapFrom(methodInfo.getMethodCalls(), methodBindingMap));
            methodInfo.setFieldAccessSet(findJavaFieldInfo(methodInfo.getFieldAccesses()));
        });
        fieldInfoMap.values().forEach(fieldInfo -> {
            fieldInfo.setBelongToSet(findJavaClassInfo(fieldInfo.getBelongTo()));
            fieldInfo.setFullTypeSet(findJavaClassInfo(fieldInfo.getFullType()));
        });
    }

    public void save(Neo4jServiceFactory serviceFactory) {
        var classInfoService = serviceFactory.createService(JavaClassInfo.class);
        var methodInfoService = serviceFactory.createService(JavaMethodInfo.class);
        var fieldInfoService = serviceFactory.createService(JavaFieldInfo.class);

        classInfoService.saveAll(classInfoMap.values());
        methodInfoService.saveAll(methodInfoMap.values());
        fieldInfoService.saveAll(fieldInfoMap.values());
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
