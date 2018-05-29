package edu.pku.sei.tsr.snowgraph.javacodeextractor.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import edu.pku.sei.tsr.snowgraph.api.Neo4jService;
import edu.pku.sei.tsr.snowgraph.javacodeextractor.JavaCodeGraphBuilder;

public class JavaClassInfo {

    private final String name;
    private final String fullName;
    private final boolean isInterface;
    private final String visibility;
    private final boolean isAbstract;
    private final boolean isFinal;
    private final String comment;
    private final String content;

    private final String superClassType;
    private final String superInterfaceTypes;
    private long nodeId;

    public JavaClassInfo(Neo4jService db, String name, String fullName, boolean isInterface, String visibility, boolean isAbstract, boolean isFinal, String comment, String content, String superClassType, String superInterfaceTypes) {
        Preconditions.checkArgument(name != null);
        this.name = name;
        Preconditions.checkArgument(fullName != null);
        this.fullName = fullName;
        this.isInterface = isInterface;
        Preconditions.checkArgument(visibility != null);
        this.visibility = visibility;
        this.isAbstract = isAbstract;
        this.isFinal = isFinal;
        Preconditions.checkArgument(comment != null);
        this.comment = comment;
        Preconditions.checkArgument(content != null);
        this.content = content;
        Preconditions.checkArgument(superClassType != null);
        this.superClassType = superClassType;
        Preconditions.checkArgument(superInterfaceTypes != null);
        this.superInterfaceTypes = superInterfaceTypes;
        nodeId = createNode(db);
    }

    private long createNode(Neo4jService db) {
        var properties = ImmutableMap.<String, Object>builder()
            .put(JavaCodeGraphBuilder.NAME, name)
            .put(JavaCodeGraphBuilder.FULLNAME, fullName)
            .put(JavaCodeGraphBuilder.IS_INTERFACE, isInterface)
            .put(JavaCodeGraphBuilder.VISIBILITY, visibility)
            .put(JavaCodeGraphBuilder.IS_ABSTRACT, isAbstract)
            .put(JavaCodeGraphBuilder.IS_FINAL, isFinal)
            .put(JavaCodeGraphBuilder.COMMENT, comment)
            .put(JavaCodeGraphBuilder.CONTENT, content)
            .build();
        nodeId = db.createNode(JavaCodeGraphBuilder.CLASS, properties);
        return nodeId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getSuperClassType() {
        return superClassType;
    }

    public String getSuperInterfaceTypes() {
        return superInterfaceTypes;
    }

    public long getNodeId() {
        return nodeId;
    }
}