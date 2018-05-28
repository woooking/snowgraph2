package edu.pku.sei.tsr.snowgraph.javacodeextractor.entity;

import com.google.common.base.Preconditions;
import edu.pku.sei.tsr.snowgraph.javacodeextractor.JavaCodeGraphBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

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
    private Node node;

    public JavaClassInfo(String name, String fullName, boolean isInterface, String visibility, boolean isAbstract, boolean isFinal, String comment, String content, String superClassType, String superInterfaceTypes) {
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
    }

    private Node createNode(GraphDatabaseService db) {
        Node node = db.createNode(JavaCodeGraphBuilder.CLASS);
        node.setProperty(JavaCodeGraphBuilder.NAME, name);
        node.setProperty(JavaCodeGraphBuilder.NAME, name);
        node.setProperty(JavaCodeGraphBuilder.FULLNAME, fullName);
        node.setProperty(JavaCodeGraphBuilder.IS_INTERFACE, isInterface);
        node.setProperty(JavaCodeGraphBuilder.VISIBILITY, visibility);
        node.setProperty(JavaCodeGraphBuilder.IS_ABSTRACT, isAbstract);
        node.setProperty(JavaCodeGraphBuilder.IS_FINAL, isFinal);
        node.setProperty(JavaCodeGraphBuilder.COMMENT, comment);
        node.setProperty(JavaCodeGraphBuilder.CONTENT, content);
        return node;
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