package edu.pku.sei.tsr.snowgraph.javacodeextractor.entity;

import edu.pku.sei.tsr.snowgraph.api.GraphEntity;
import org.neo4j.ogm.annotation.*;

import java.util.Set;

@NodeEntity(label = "Class")
public class JavaClassInfo implements GraphEntity {
    @Id
    @GeneratedValue
    private Long id;

    @Override
    public Long getId() {
        return id;
    }

    private String name;
    private String fullName;
    private boolean isInterface;
    private String visibility;
    private boolean isAbstract;
    private boolean isFinal;
    private String comment;
    private String content;

    @Transient
    private final String superClassType;
    @Relationship(type = "extend")
    private Set<JavaClassInfo> superClasses;

    @Transient
    private final String superInterfaceTypes;
    @Relationship(type = "implement")
    private Set<JavaClassInfo> superInterfaces;

    public JavaClassInfo(String name, String fullName, boolean isInterface, String visibility, boolean isAbstract, boolean isFinal, String comment, String content, String superClassType, String superInterfaceTypes) {
        this.name = name;
        this.fullName = fullName;
        this.isInterface = isInterface;
        this.visibility = visibility;
        this.isAbstract = isAbstract;
        this.isFinal = isFinal;
        this.comment = comment;
        this.content = content;
        this.superClassType = superClassType;
        this.superInterfaceTypes = superInterfaceTypes;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public String getVisibility() {
        return visibility;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public String getComment() {
        return comment;
    }

    public String getContent() {
        return content;
    }

    public String getSuperClassType() {
        return superClassType;
    }

    public String getSuperInterfaceTypes() {
        return superInterfaceTypes;
    }

    public Set<JavaClassInfo> getSuperClasses() {
        return superClasses;
    }

    public void setSuperClasses(Set<JavaClassInfo> superClasses) {
        this.superClasses = superClasses;
    }

    public Set<JavaClassInfo> getSuperInterfaces() {
        return superInterfaces;
    }

    public void setSuperInterfaces(Set<JavaClassInfo> superInterfaces) {
        this.superInterfaces = superInterfaces;
    }
}