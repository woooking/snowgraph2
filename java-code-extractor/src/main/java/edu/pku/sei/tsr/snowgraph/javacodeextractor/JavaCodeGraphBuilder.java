package edu.pku.sei.tsr.snowgraph.javacodeextractor;

import edu.pku.sei.tsr.snowgraph.api.ChangeEvent;
import edu.pku.sei.tsr.snowgraph.api.Neo4jServiceFactory;
import edu.pku.sei.tsr.snowgraph.javacodeextractor.entity.JavaProjectInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

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
    private FileFilter javaFileFilter = new SuffixFileFilter(new String[]{".java"});
    private JavaProjectInfo javaProjectInfo;
    private Set<String> srcPathSet = new HashSet<>();
    private Set<String> srcFolderSet = new HashSet<>();

    JavaCodeGraphBuilder() {
        javaProjectInfo = new JavaProjectInfo();
    }

    void process(Neo4jServiceFactory serviceFactory, Collection<File> files) {
        onFilesCreated(serviceFactory, files);
    }

    void update(Neo4jServiceFactory serviceFactory, Collection<ChangeEvent<Path>> changeEvents) {
        var events = changeEvents.stream()
            .collect(
                groupingBy(ChangeEvent::getType,
                    mapping(Function.<ChangeEvent<Path>>identity().andThen(ChangeEvent::getInstance).andThen(Path::toFile), toList())
                )
            );
        onFilesCreated(serviceFactory, events.get(ChangeEvent.Type.CREATED));
    }

    private void onFilesCreated(Neo4jServiceFactory serviceFactory, Collection<File> files) {
        Collection<File> javaFiles = files.stream().filter(javaFileFilter::accept).collect(toList());
        srcPathSet.addAll(javaFiles.stream().map(File::getAbsolutePath).collect(toSet()));
        srcFolderSet.addAll(javaFiles.stream().map(File::getParentFile).map(File::getAbsolutePath).collect(toSet()));
        NameResolver.setSrcPathSet(srcPathSet);
        run(serviceFactory, javaFiles);
    }

    private void onFilesDeleted(Neo4jServiceFactory serviceFactory, Collection<File> files) {
//        Collection<File> javaFiles = files.stream().filter(javaFileFilter::accept).collect(toList());
//        srcPathSet.removeAll(javaFiles.stream().map(File::getAbsolutePath).collect(toSet()));
//        srcFolderSet.removeAll(javaFiles.stream().map(File::getParentFile).map(File::getAbsolutePath).collect(toSet()));
//        NameResolver.setSrcPathSet(srcPathSet);
//        run(serviceFactory, javaFiles);
    }

    private void run(Neo4jServiceFactory serviceFactory, Collection<File> files) {
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
        parser.createASTs(todoFiles, null, new String[]{}, new FileASTRequestor() {
            @Override
            public void acceptAST(String sourceFilePath, CompilationUnit javaUnit) {
                try {
                    javaUnit.accept(new JavaASTVisitor(javaProjectInfo, FileUtils.readFileToString(new File(sourceFilePath), "utf-8")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, null);
        javaProjectInfo.buildRelationsAndSave(serviceFactory);
    }
}


