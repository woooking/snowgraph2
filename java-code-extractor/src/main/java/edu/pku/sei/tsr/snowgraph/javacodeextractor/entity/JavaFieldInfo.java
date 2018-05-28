package edu.pku.sei.tsr.snowgraph.javacodeextractor.entity;

import com.google.common.base.Preconditions;
import edu.pku.sei.tsr.snowgraph.javacodeextractor.JavaCodeGraphBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

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
    private Node node;

    public JavaFieldInfo(String name, String fullName, String type, String visibility, boolean isStatic, boolean isFinal, String comment, String belongTo, String fullType) {
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
    }

    private Node createNode(GraphDatabaseService db) {
        Node node = db.createNode(JavaCodeGraphBuilder.FIELD);
        node.setProperty(JavaCodeGraphBuilder.NAME, name);
        node.setProperty(JavaCodeGraphBuilder.FULLNAME, fullName);
        node.setProperty(JavaCodeGraphBuilder.TYPE_STR, type);
        node.setProperty(JavaCodeGraphBuilder.VISIBILITY, visibility);
        node.setProperty(JavaCodeGraphBuilder.IS_STATIC, isStatic);
        node.setProperty(JavaCodeGraphBuilder.IS_FINAL, isFinal);
        node.setProperty(JavaCodeGraphBuilder.COMMENT, comment);
        return node;
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