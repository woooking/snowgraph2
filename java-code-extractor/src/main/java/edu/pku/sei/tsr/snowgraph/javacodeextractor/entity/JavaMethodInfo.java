package edu.pku.sei.tsr.snowgraph.javacodeextractor.entity;

import com.google.common.base.Preconditions;
import edu.pku.sei.tsr.snowgraph.javacodeextractor.JavaCodeGraphBuilder;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import java.util.Set;

public class JavaMethodInfo {

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

    private IMethodBinding methodBinding;
    private String fullReturnType;
    private String belongTo;
    private String fullParams;
    private String fullVariables;
    private Set<IMethodBinding> methodCalls;
    private String fieldAccesses;
    private String throwTypes;
    private Node node;

    public JavaMethodInfo(String name, String fullName, String returnType, String visibility, boolean isConstruct, boolean isAbstract,
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
        this.methodBinding = methodBinding;
        Preconditions.checkArgument(fullReturnType != null);
        this.fullReturnType = fullReturnType;
        Preconditions.checkArgument(belongTo != null);
        this.belongTo = belongTo;
        Preconditions.checkArgument(fullParams != null);
        this.fullParams = fullParams;
        Preconditions.checkArgument(fullVariables != null);
        this.fullVariables = fullVariables;
        Preconditions.checkArgument(methodCalls != null);
        this.methodCalls = methodCalls;
        Preconditions.checkArgument(fieldAccesses != null);
        this.fieldAccesses = fieldAccesses;
        Preconditions.checkArgument(throwTypes != null);
        this.throwTypes = throwTypes;
    }

    private Node createNode(GraphDatabaseService db) {
        Node node = db.createNode(JavaCodeGraphBuilder.METHOD);
        node.setProperty(JavaCodeGraphBuilder.NAME, name);
        node.setProperty(JavaCodeGraphBuilder.FULLNAME, fullName);
        node.setProperty(JavaCodeGraphBuilder.RETURN_TYPE_STR, returnType);
        node.setProperty(JavaCodeGraphBuilder.VISIBILITY, visibility);
        node.setProperty(JavaCodeGraphBuilder.IS_CONSTRUCTOR, isConstruct);
        node.setProperty(JavaCodeGraphBuilder.IS_ABSTRACT, isAbstract);
        node.setProperty(JavaCodeGraphBuilder.IS_STATIC, isStatic);
        node.setProperty(JavaCodeGraphBuilder.IS_FINAL, isFinal);
        node.setProperty(JavaCodeGraphBuilder.IS_SYNCHRONIZED, isSynchronized);
        node.setProperty(JavaCodeGraphBuilder.CONTENT, content);
        node.setProperty(JavaCodeGraphBuilder.COMMENT, comment);
        node.setProperty(JavaCodeGraphBuilder.PARAM_TYPE_STR, params);
        return node;
    }

    public String getFullName() {
        return fullName;
    }

    public IMethodBinding getMethodBinding() {
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

    public Set<IMethodBinding> getMethodCalls() {
        return methodCalls;
    }

    public String getFieldAccesses() {
        return fieldAccesses;
    }

    public Node getNode(GraphDatabaseService db) {
        if (node == null) {
            synchronized (this) {
                if (node == null) {
                    node = createNode(db);
                }
            }
        }
        return node;
    }
}