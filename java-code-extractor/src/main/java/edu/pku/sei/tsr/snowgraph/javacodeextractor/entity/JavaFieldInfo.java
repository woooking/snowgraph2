package edu.pku.sei.tsr.snowgraph.javacodeextractor.entity;

import edu.pku.sei.tsr.snowgraph.api.GraphEntity;
import edu.pku.sei.tsr.snowgraph.javacodeextractor.JavaCodeGraphBuilder;
import org.neo4j.ogm.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@NodeEntity(label = "Field")
public class JavaFieldInfo implements GraphEntity {
    @Id
    @GeneratedValue
    public Long id;

    @Override
    public Long getId() {
        return id;
    }

    private String name;
    private String fullName;
    private String type;
    private String visibility;
    private boolean isStatic;
    private boolean isFinal;
    private String comment;

    @Transient
    private String belongTo;
    @Relationship(type = "belongTo")
    private Set<JavaClassInfo> belongToSet;
    @Transient
    private String fullType;
    @Relationship(type = "fieldType", direction = Relationship.INCOMING)
    private Set<JavaClassInfo> fullTypeSet;

    public JavaFieldInfo(String name, String fullName, String type, String visibility, boolean isStatic, boolean isFinal, String comment, String belongTo, String fullType) {
        this.name = name;
        this.fullName = fullName;
        this.type = type;
        this.visibility = visibility;
        this.isStatic = isStatic;
        this.isFinal = isFinal;
        this.comment = comment;
        this.belongTo = belongTo;
        this.fullType = fullType;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public String getType() {
        return type;
    }

    public String getVisibility() {
        return visibility;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public String getComment() {
        return comment;
    }

    public String getBelongTo() {
        return belongTo;
    }

    public String getFullType() {
        return fullType;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<JavaClassInfo> getBelongToSet() {
        return belongToSet;
    }

    public void setBelongToSet(Set<JavaClassInfo> belongToSet) {
        this.belongToSet = belongToSet;
    }

    public Set<JavaClassInfo> getFullTypeSet() {
        return fullTypeSet;
    }

    public void setFullTypeSet(Set<JavaClassInfo> fullTypeSet) {
        this.fullTypeSet = fullTypeSet;
    }
}