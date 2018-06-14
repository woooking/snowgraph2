package edu.pku.sei.tsr.snowgraph.javacodeextractor.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jService;
import edu.pku.sei.tsr.snowgraph.javacodeextractor.JavaCodeGraphBuilder;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

public class JavaMethodInfo implements Serializable {

    private String name;
    private String fullName;
    private String returnType;
    private String visibility;
    private boolean isConstruct;
    private boolean isAbstract;
    private boolean isFinal;
    private boolean isStatic;
    private boolean isSynchronized;
    private String content;
    private String comment;
    private String params;

    private String methodBinding;
    private String fullReturnType;
    private String belongTo;
    private String fullParams;
    private String fullVariables;
    private Set<String> methodCalls;
    private String fieldAccesses;
    private String throwTypes;
    private long nodeId;

    public JavaMethodInfo(Neo4jService db, String name, String fullName, String returnType, String visibility, boolean isConstruct, boolean isAbstract,
                          boolean isFinal, boolean isStatic, boolean isSynchronized, String content, String comment, String params, IMethodBinding methodBinding,
                          String fullReturnType, String belongTo, String fullParams, String fullVariables, Set<IMethodBinding> methodCalls, String fieldAccesses, String throwTypes) {
        Preconditions.checkArgument(name != null);
        this.name = name;
        Preconditions.checkArgument(fullName != null);
        this.fullName = fullName;
        Preconditions.checkArgument(returnType != null);
        this.returnType = returnType;
        Preconditions.checkArgument(visibility != null);
        this.visibility = visibility;
        this.isConstruct = isConstruct;
        this.isAbstract = isAbstract;
        this.isFinal = isFinal;
        this.isStatic = isStatic;
        this.isSynchronized = isSynchronized;
        Preconditions.checkArgument(content != null);
        this.content = content;
        Preconditions.checkArgument(comment != null);
        this.comment = comment;
        Preconditions.checkArgument(params != null);
        this.params = params;
        Preconditions.checkArgument(methodBinding != null);
        this.methodBinding = methodBinding.getKey();
        Preconditions.checkArgument(fullReturnType != null);
        this.fullReturnType = fullReturnType;
        Preconditions.checkArgument(belongTo != null);
        this.belongTo = belongTo;
        Preconditions.checkArgument(fullParams != null);
        this.fullParams = fullParams;
        Preconditions.checkArgument(fullVariables != null);
        this.fullVariables = fullVariables;
        Preconditions.checkArgument(methodCalls != null);
        this.methodCalls = methodCalls.stream().map(IBinding::getKey).collect(Collectors.toSet());
        Preconditions.checkArgument(fieldAccesses != null);
        this.fieldAccesses = fieldAccesses;
        Preconditions.checkArgument(throwTypes != null);
        this.throwTypes = throwTypes;
        nodeId = createNode(db);
    }

    private long createNode(Neo4jService db) {
        var properties = ImmutableMap.<String, Object>builder()
            .put(JavaCodeGraphBuilder.NAME, name)
            .put(JavaCodeGraphBuilder.FULLNAME, fullName)
            .put(JavaCodeGraphBuilder.RETURN_TYPE_STR, returnType)
            .put(JavaCodeGraphBuilder.VISIBILITY, visibility)
            .put(JavaCodeGraphBuilder.IS_CONSTRUCTOR, isConstruct)
            .put(JavaCodeGraphBuilder.IS_ABSTRACT, isAbstract)
            .put(JavaCodeGraphBuilder.IS_STATIC, isStatic)
            .put(JavaCodeGraphBuilder.IS_FINAL, isFinal)
            .put(JavaCodeGraphBuilder.IS_SYNCHRONIZED, isSynchronized)
            .put(JavaCodeGraphBuilder.CONTENT, content)
            .put(JavaCodeGraphBuilder.COMMENT, comment)
            .put(JavaCodeGraphBuilder.PARAM_TYPE_STR, params)
            .build();
        nodeId = db.createNode(JavaCodeGraphBuilder.METHOD, properties).getId();
        return nodeId;


    }

    public String getFullName() {
        return fullName;
    }

    public String getMethodBinding() {
        return methodBinding;
    }

    public String getBelongTo() {
        return belongTo;
    }

    public String getFullParams() {
        return fullParams;
    }

    public String getFullReturnType() {
        return fullReturnType;
    }

    public String getThrowTypes() {
        return throwTypes;
    }

    public String getFullVariables() {
        return fullVariables;
    }

    public Set<String> getMethodCalls() {
        return methodCalls;
    }

    public String getFieldAccesses() {
        return fieldAccesses;
    }

    public long getNodeId() {
        return nodeId;
    }
}