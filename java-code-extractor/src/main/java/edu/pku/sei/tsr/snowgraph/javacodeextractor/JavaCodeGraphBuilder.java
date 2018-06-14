package edu.pku.sei.tsr.snowgraph.javacodeextractor;

import edu.pku.sei.tsr.snowgraph.api.event.ChangeEvent;
import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jService;
import edu.pku.sei.tsr.snowgraph.javacodeextractor.entity.JavaProjectInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.neo4j.graphdb.RelationshipType;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * 解析java源代码，抽取出代码实体以及这些代码实体之间的静态依赖关系，并将它们存储到neo4j图数据库中。
 * <p>
 * Class实体示例：
 * name: UnixStat
 * fullName: zstorg.apache.tools.zip.UnixStat
 * content, comment, isAbstract, isFinal, isInterface, visibility
 * <p>
 * Method实体示例：
 * name: error
 * fullName: cn.edu.pku.sei.tsr.service.ras.util.ZipGenerator.error( String msg, boolean quit )
 * paramType: String msg, boolean quit
 * returnType: void
 * content, comment, isAbstract, isConstructor, isFinal, isStatic, isSynchronized, visibility
 * <p>
 * Field实体示例：
 * name: STRATEGY_ASSIGN
 * fullName: cn.edu.pku.sei.tsr.entity.ConfigurationItem.STRATEGY_ASSIGN
 * isFinal, isStatic, type, visibility
 */

public class JavaCodeGraphBuilder {
    public static final String CLASS = "Class";
    public static final String METHOD = "Method";
    public static final String FIELD = "Field";
    public static final RelationshipType EXTEND = RelationshipType.withName("extend");
    public static final RelationshipType IMPLEMENT = RelationshipType.withName("implement");
    public static final RelationshipType HAVE_METHOD = RelationshipType.withName("haveMethod");
    public static final RelationshipType PARAM_TYPE = RelationshipType.withName("paramType");
    public static final RelationshipType RETURN_TYPE = RelationshipType.withName("returnType");
    public static final RelationshipType THROW_TYPE = RelationshipType.withName("throwType");
    public static final RelationshipType METHOD_CALL = RelationshipType.withName("methodCall");
    public static final RelationshipType VARIABLE_TYPE = RelationshipType.withName("variableType");
    public static final RelationshipType HAVE_FIELD = RelationshipType.withName("haveField");
    public static final RelationshipType FIELD_TYPE = RelationshipType.withName("fieldType");
    public static final RelationshipType FIELD_ACCESS = RelationshipType.withName("fieldAccess");
    public static final String NAME = "name";
    public static final String FULLNAME = "fullName";
    public static final String IS_INTERFACE = "isInterface";
    public static final String VISIBILITY = "visibility";
    public static final String IS_ABSTRACT = "isAbstract";
    public static final String IS_FINAL="isFinal";
    public static final String COMMENT="comment";
    public static final String CONTENT="content";
    public static final String RETURN_TYPE_STR="returnType";
    public static final String TYPE_STR="type";
    public static final String PARAM_TYPE_STR="paramType";
    public static final String IS_CONSTRUCTOR="isConstructor";
    public static final String IS_STATIC="isStatic";
    public static final String IS_SYNCHRONIZED="isSynchronized";
    private static FileFilter javaFileFilter = new SuffixFileFilter(new String[]{".java"});

    private final Logger logger;

    private JavaProjectInfo javaProjectInfo;
    private Set<String> srcPathSet = new HashSet<>();
    private Set<String> srcFolderSet = new HashSet<>();

    JavaCodeGraphBuilder(Logger logger) {
        this.logger = logger;
        this.javaProjectInfo = new JavaProjectInfo();
    }

    void process(Neo4jService db, Collection<File> files) {
        var codes = files.stream()
            .flatMap(p -> FileUtils.listFiles(p, null, true).stream())
            .collect(Collectors.toList());
        onFilesCreated(db, codes);
    }

    void update(Neo4jService db, Collection<ChangeEvent<Path>> changeEvents) {
        var events = changeEvents.stream()
            .collect(
                groupingBy(ChangeEvent::getType,
                    mapping(Function.<ChangeEvent<Path>>identity().andThen(ChangeEvent::getInstance).andThen(Path::toFile), toList())
                )
            );
        if (events.containsKey(ChangeEvent.Type.CREATED)) onFilesCreated(db, events.get(ChangeEvent.Type.CREATED));
        if (events.containsKey(ChangeEvent.Type.DELETED)) onFilesDeleted(db, events.get(ChangeEvent.Type.DELETED));
        // TODO: MODIFIED
    }

    @SuppressWarnings("unchecked")
    void onLoad(File saveFile) {
        try (var ois = new ObjectInputStream(new FileInputStream(saveFile))) {
            javaProjectInfo = (JavaProjectInfo) ois.readObject();
            srcPathSet = (Set<String>) ois.readObject();
            srcFolderSet = (Set<String>) ois.readObject();
        } catch (FileNotFoundException e) {
            // Do nothing
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Error occurred when loading java project!", e);
        }
    }

    void onSave(File saveFile) {
        try (var oos = new ObjectOutputStream(new FileOutputStream(saveFile))) {
            oos.writeObject(javaProjectInfo);
            oos.writeObject(srcPathSet);
            oos.writeObject(srcFolderSet);
        } catch (IOException e) {
            logger.error("Error occurred when saving java project!", e);
        }
    }

    private void onFilesCreated(Neo4jService db, Collection<File> files) {
        Collection<File> javaFiles = files.stream().filter(javaFileFilter::accept).collect(toList());
        srcPathSet.addAll(javaFiles.stream().map(File::getAbsolutePath).collect(toSet()));
        srcFolderSet.addAll(javaFiles.stream().map(File::getParentFile).map(File::getAbsolutePath).collect(toSet()));
        NameResolver.setSrcPathSet(srcPathSet);
        run(db, javaFiles);
    }

    private void onFilesDeleted(Neo4jService db, Collection<File> files) {
        Collection<File> javaFiles = files.stream().filter(javaFileFilter::accept).collect(toList());
        srcPathSet.removeAll(javaFiles.stream().map(File::getAbsolutePath).collect(toSet()));
        javaProjectInfo.removeFiles(db, files);
    }

    private void run(Neo4jService db, Collection<File> files) {
        String[] todoFiles = new String[files.size()];
        files.stream().map(File::getAbsolutePath).collect(toSet()).toArray(todoFiles);
        String[] srcFolderPaths = new String[srcFolderSet.size()];
        srcFolderSet.toArray(srcFolderPaths);
        ASTParser parser;
        parser = ASTParser.newParser(AST.JLS9);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        Map<String, String> options = new Hashtable<>();
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_9);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_9);
        options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
        parser.setCompilerOptions(options);
        parser.setBindingsRecovery(true);
        parser.setEnvironment(null, srcFolderPaths, null, true);
        try (var tx = db.beginTx()) {
            parser.createASTs(todoFiles, null, new String[]{}, new FileASTRequestor() {
                @Override
                public void acceptAST(String sourceFilePath, CompilationUnit javaUnit) {
                    try {
                        javaUnit.accept(new JavaASTVisitor(db, javaProjectInfo, FileUtils.readFileToString(new File(sourceFilePath), "utf-8"), sourceFilePath));
                    } catch (IOException e) {
                        logger.error("", e);
                    }
                }
            }, null);
            tx.success();
        }
        javaProjectInfo.buildRelationsAndSave(db);
    }

}


