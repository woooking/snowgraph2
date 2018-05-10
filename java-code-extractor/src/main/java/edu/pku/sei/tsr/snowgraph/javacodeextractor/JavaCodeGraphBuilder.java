package edu.pku.sei.tsr.snowgraph.javacodeextractor;

import edu.pku.sei.tsr.snowgraph.api.Neo4jServiceFactory;
import edu.pku.sei.tsr.snowgraph.javacodeextractor.entity.JavaProjectInfo;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
    private String srcDir;
    private Neo4jServiceFactory serviceFactory;

    static void process(Neo4jServiceFactory serviceFactory, String srcDir) {
        new JavaCodeGraphBuilder(serviceFactory, srcDir).process();
    }

    private JavaCodeGraphBuilder(Neo4jServiceFactory serviceFactory, String srcDir) {
        this.srcDir = srcDir;
        this.serviceFactory = serviceFactory;
    }

    private void process() {
        JavaProjectInfo javaProjectInfo = new JavaProjectInfo();
        Collection<File> javaFiles = FileUtils.listFiles(new File(srcDir), new String[]{"java"}, true);
        Set<String> srcPathSet = new HashSet<>();
        Set<String> srcFolderSet = new HashSet<>();
        for (File javaFile : javaFiles) {
            String srcPath = javaFile.getAbsolutePath();
            String srcFolderPath = javaFile.getParentFile().getAbsolutePath();
            srcPathSet.add(srcPath);
            srcFolderSet.add(srcFolderPath);
        }
        String[] srcPaths = new String[srcPathSet.size()];
        srcPathSet.toArray(srcPaths);
        NameResolver.setSrcPathSet(srcPathSet);
        String[] srcFolderPaths = new String[srcFolderSet.size()];
        srcFolderSet.toArray(srcFolderPaths);
        ASTParser parser = ASTParser.newParser(AST.JLS9);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setEnvironment(null, srcFolderPaths, null, true);
        parser.setResolveBindings(true);
        Map<String, String> options = new Hashtable<>();
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_9);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_9);
        options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
        parser.setCompilerOptions(options);
        parser.setBindingsRecovery(true);
        parser.createASTs(srcPaths, null, new String[]{}, new FileASTRequestor() {
            @Override
            public void acceptAST(String sourceFilePath, CompilationUnit javaUnit) {
                try {
                    javaUnit.accept(new JavaASTVisitor(javaProjectInfo, FileUtils.readFileToString(new File(sourceFilePath), "utf-8")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, null);
        javaProjectInfo.buildRelations();
        javaProjectInfo.save(serviceFactory);
    }
}


