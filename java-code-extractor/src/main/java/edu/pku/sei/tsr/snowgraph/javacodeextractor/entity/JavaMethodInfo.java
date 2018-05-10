package edu.pku.sei.tsr.snowgraph.javacodeextractor.entity;

import edu.pku.sei.tsr.snowgraph.api.GraphEntity;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.neo4j.ogm.annotation.*;

import java.util.Set;

@NodeEntity(label = "Method")
public class JavaMethodInfo implements GraphEntity {
    @Id
    @GeneratedValue
    public Long id;

    @Override
    public Long getId() {
        return id;
    }

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
    @Property(name = "paramType")
    private String params;

    @Transient
    private IMethodBinding methodBinding;

    @Transient
    private String belongTo;
    @Relationship(type = "belongTo", direction = Relationship.INCOMING)
    private Set<JavaClassInfo> belongToSet;
    @Transient
    private String fullParams;
    @Relationship(type = "paramType")
    private Set<JavaClassInfo> fullParamSet;
    @Transient
    private String fullReturnType;
    @Relationship(type = "returnType")
    private Set<JavaClassInfo> fullReturnTypeSet;
    @Transient
    private String throwTypes;
    @Relationship(type = "throwType")
    private Set<JavaClassInfo> throwTypeSet;
    @Transient
    private String fullVariables;
    @Relationship(type = "variableType")
    private Set<JavaClassInfo> fullVariableSet;
    @Transient
    private Set<IMethodBinding> methodCalls;
    @Relationship(type = "methodCall")
    private Set<JavaMethodInfo> methodCallSet;
    @Transient
    private String fieldAccesses;
    @Relationship(type = "fieldAccess")
    private Set<JavaFieldInfo> fieldAccessSet;

    public JavaMethodInfo(String name, String fullName, String returnType, String visibility, boolean isConstruct, boolean isAbstract,
                          boolean isFinal, boolean isStatic, boolean isSynchronized, String content, String comment, String params, IMethodBinding methodBinding,
                          String fullReturnType, String belongTo, String fullParams, String fullVariables, Set<IMethodBinding> methodCalls, String fieldAccesses, String throwTypes) {
        this.name = name;
        this.fullName = fullName;
        this.returnType = returnType;
        this.visibility = visibility;
        this.isConstruct = isConstruct;
        this.isAbstract = isAbstract;
        this.isFinal = isFinal;
        this.isStatic = isStatic;
        this.isSynchronized = isSynchronized;
        this.content = content;
        this.comment = comment;
        this.params = params;
        this.methodBinding = methodBinding;
        this.fullReturnType = fullReturnType;
        this.belongTo = belongTo;
        this.fullParams = fullParams;
        this.fullVariables = fullVariables;
        this.methodCalls = methodCalls;
        this.fieldAccesses = fieldAccesses;
        this.throwTypes = throwTypes;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public String getReturnType() {
        return returnType;
    }

    public String getVisibility() {
        return visibility;
    }

    public boolean isConstruct() {
        return isConstruct;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isSynchronized() {
        return isSynchronized;
    }

    public String getContent() {
        return content;
    }

    public String getComment() {
        return comment;
    }

    public String getParams() {
        return params;
    }

    public IMethodBinding getMethodBinding() {
        return methodBinding;
    }

    public String getFullReturnType() {
        return fullReturnType;
    }

    public String getBelongTo() {
        return belongTo;
    }

    public String getFullParams() {
        return fullParams;
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

    public String getThrowTypes() {
        return throwTypes;
    }

    public Set<JavaClassInfo> getBelongToSet() {
        return belongToSet;
    }

    public void setBelongToSet(Set<JavaClassInfo> belongToSet) {
        this.belongToSet = belongToSet;
    }

    public Set<JavaClassInfo> getFullParamSet() {
        return fullParamSet;
    }

    public void setFullParamSet(Set<JavaClassInfo> fullParamSet) {
        this.fullParamSet = fullParamSet;
    }

    public Set<JavaClassInfo> getFullReturnTypeSet() {
        return fullReturnTypeSet;
    }

    public void setFullReturnTypeSet(Set<JavaClassInfo> fullReturnTypeSet) {
        this.fullReturnTypeSet = fullReturnTypeSet;
    }

    public Set<JavaClassInfo> getThrowTypeSet() {
        return throwTypeSet;
    }

    public void setThrowTypeSet(Set<JavaClassInfo> throwTypeSet) {
        this.throwTypeSet = throwTypeSet;
    }

    public Set<JavaClassInfo> getFullVariableSet() {
        return fullVariableSet;
    }

    public void setFullVariableSet(Set<JavaClassInfo> fullVariableSet) {
        this.fullVariableSet = fullVariableSet;
    }

    public Set<JavaMethodInfo> getMethodCallSet() {
        return methodCallSet;
    }

    public void setMethodCallSet(Set<JavaMethodInfo> methodCallSet) {
        this.methodCallSet = methodCallSet;
    }

    public Set<JavaFieldInfo> getFieldAccessSet() {
        return fieldAccessSet;
    }

    public void setFieldAccessSet(Set<JavaFieldInfo> fieldAccessSet) {
        this.fieldAccessSet = fieldAccessSet;
    }
}