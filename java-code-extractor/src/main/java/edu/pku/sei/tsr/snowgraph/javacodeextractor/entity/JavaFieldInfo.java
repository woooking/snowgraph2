package edu.pku.sei.tsr.snowgraph.javacodeextractor.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jService;
import edu.pku.sei.tsr.snowgraph.javacodeextractor.JavaCodeGraphBuilder;

public class JavaFieldInfo {

    private String name;
    private String fullName;
    private String type;
    private String visibility;
    private boolean isStatic;
    private boolean isFinal;
    private String comment;

    private String belongTo;
    private String fullType;
    private long nodeId;

    public JavaFieldInfo(Neo4jService db, String name, String fullName, String type, String visibility, boolean isStatic, boolean isFinal, String comment, String belongTo, String fullType) {
        Preconditions.checkArgument(name != null);
        this.name = name;
        Preconditions.checkArgument(fullName != null);
        this.fullName = fullName;
        Preconditions.checkArgument(type != null);
        this.type = type;
        Preconditions.checkArgument(visibility != null);
        this.visibility = visibility;
        this.isStatic = isStatic;
        this.isFinal = isFinal;
        Preconditions.checkArgument(comment != null);
        this.comment = comment;
        Preconditions.checkArgument(belongTo != null);
        this.belongTo = belongTo;
        Preconditions.checkArgument(fullType != null);
        this.fullType = fullType;
        nodeId = createNode(db);
    }

    private long createNode(Neo4jService db) {
        var properties = ImmutableMap.<String, Object>builder()
            .put(JavaCodeGraphBuilder.NAME, name)
            .put(JavaCodeGraphBuilder.FULLNAME, fullName)
            .put(JavaCodeGraphBuilder.TYPE_STR, type)
            .put(JavaCodeGraphBuilder.VISIBILITY, visibility)
            .put(JavaCodeGraphBuilder.IS_STATIC, isStatic)
            .put(JavaCodeGraphBuilder.IS_FINAL, isFinal)
            .put(JavaCodeGraphBuilder.COMMENT, comment)
            .build();
        nodeId = db.createNode(JavaCodeGraphBuilder.FIELD, properties).getId();
        return nodeId;

    }

    public String getFullName() {
        return fullName;
    }

    public String getBelongTo() {
        return belongTo;
    }

    public String getFullType() {
        return fullType;
    }

    public long getNodeId() {
        return nodeId;
    }
}