package edu.pku.sei.tsr.snowgraph.javacodeextractor;

import edu.pku.sei.tsr.snowgraph.api.neo4j.Neo4jService;
import edu.pku.sei.tsr.snowgraph.javacodeextractor.entity.JavaClassInfo;
import edu.pku.sei.tsr.snowgraph.javacodeextractor.entity.JavaFieldInfo;
import edu.pku.sei.tsr.snowgraph.javacodeextractor.entity.JavaMethodInfo;
import edu.pku.sei.tsr.snowgraph.javacodeextractor.entity.JavaProjectInfo;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JavaASTVisitor extends ASTVisitor {

    private final JavaProjectInfo javaProjectInfo;
    private final String sourceContent;
    private final Neo4jService db;
    private final String path;

    public JavaASTVisitor(Neo4jService db, JavaProjectInfo javaProjectInfo, String sourceContent, String path) {
        this.db = db;
        this.javaProjectInfo = javaProjectInfo;
        this.sourceContent = sourceContent;
        this.path = path;
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        JavaClassInfo javaClassInfo = createJavaClassInfo(node);
        javaProjectInfo.addClassInfo(path, javaClassInfo);

        MethodDeclaration[] methodDeclarations = node.getMethods();
        for (MethodDeclaration methodDeclaration : methodDeclarations) {
            JavaMethodInfo javaMethodInfo = createJavaMethodInfo(methodDeclaration, javaClassInfo.getFullName());
            if (javaMethodInfo != null)
                javaProjectInfo.addMethodInfo(path, javaMethodInfo);
        }

        FieldDeclaration[] fieldDeclarations = node.getFields();
        for (FieldDeclaration fieldDeclaration : fieldDeclarations) {
            List<JavaFieldInfo> javaFieldInfos = createJavaFieldInfos(fieldDeclaration, javaClassInfo.getFullName());
            for (JavaFieldInfo javaFieldInfo : javaFieldInfos)
                javaProjectInfo.addFieldInfo(path, javaFieldInfo);
        }
        return false;
    }

    private JavaClassInfo createJavaClassInfo(TypeDeclaration node) {
        String name = node.getName().getFullyQualifiedName();
        String fullName = NameResolver.getFullName(node);
        boolean isInterface = node.isInterface();
        String visibility = JavaASTVisitor.getVisibility(node.getModifiers());
        boolean isAbstract = Modifier.isAbstract(node.getModifiers());
        boolean isFinal = Modifier.isFinal(node.getModifiers());
        String comment = node.getJavadoc() == null ? "" : sourceContent.substring(node.getJavadoc().getStartPosition(), node.getJavadoc().getStartPosition() + node.getJavadoc().getLength());
        String content = sourceContent.substring(node.getStartPosition(), node.getStartPosition() + node.getLength());
        String superClassType = node.getSuperclassType() == null ? "java.lang.Object" : NameResolver.getFullName(node.getSuperclassType());
        String superInterfaceTypes = String.join(", ", (List<String>) node.superInterfaceTypes().stream().map(n -> NameResolver.getFullName((Type) n)).collect(Collectors.toList()));
        return new JavaClassInfo(db, name, fullName, isInterface, visibility, isAbstract, isFinal, comment, content, superClassType, superInterfaceTypes);
    }

    private JavaMethodInfo createJavaMethodInfo(MethodDeclaration node, String belongTo) {
        IMethodBinding methodBinding = node.resolveBinding();
        if (methodBinding == null)
            return null;
        String name = node.getName().getFullyQualifiedName();
        Type type = node.getReturnType2();
        String returnType = type == null ? "void" : type.toString();
        String fullReturnType = NameResolver.getFullName(type);
        String visibility = getVisibility(node.getModifiers());
        boolean isConstruct = node.isConstructor();
        boolean isAbstract = Modifier.isAbstract(node.getModifiers());
        boolean isFinal = Modifier.isFinal(node.getModifiers());
        boolean isStatic = Modifier.isStatic(node.getModifiers());
        boolean isSynchronized = Modifier.isSynchronized(node.getModifiers());
        String content = sourceContent.substring(node.getStartPosition(), node.getStartPosition() + node.getLength());
        String comment = node.getJavadoc() == null ? "" : sourceContent.substring(node.getJavadoc().getStartPosition(), node.getJavadoc().getStartPosition() + node.getJavadoc().getLength());
        String params = String.join(", ", (List<String>) node.parameters().stream().map(n -> {
            SingleVariableDeclaration param = (SingleVariableDeclaration) n;
            return (Modifier.isFinal(param.getModifiers()) ? "final " : "") + param.getType().toString() + " " + param.getName().getFullyQualifiedName();
        }).collect(Collectors.toList()));
        String fullName = belongTo + "." + name + "( " + params + " )";
        String fullParams = String.join(", ", (List<String>) node.parameters().stream().map(n -> {
            SingleVariableDeclaration param = (SingleVariableDeclaration) n;
            return NameResolver.getFullName(param.getType());
        }).collect(Collectors.toList()));
        String throwTypes = String.join(", ", (List<String>) node.thrownExceptionTypes().stream().map(n -> NameResolver.getFullName((Type) n)).collect(Collectors.toList()));
        Set<IMethodBinding> methodCalls = new HashSet<>();
        StringBuilder fullVariables = new StringBuilder();
        StringBuilder fieldAccesses = new StringBuilder();
        parseMethodBody(methodCalls, fullVariables, fieldAccesses, node.getBody());
        JavaMethodInfo info = new JavaMethodInfo(db, name, fullName, returnType, visibility, isConstruct, isAbstract,
            isFinal, isStatic, isSynchronized, content, comment, params, methodBinding,
            fullReturnType, belongTo, fullParams, fullVariables.toString(), methodCalls, fieldAccesses.toString(), throwTypes);
        return info;
    }

    private List<JavaFieldInfo> createJavaFieldInfos(FieldDeclaration node, String belongTo) {
        List<JavaFieldInfo> r = new ArrayList<>();
        String type = node.getType().toString();
        String fullType = NameResolver.getFullName(node.getType());
        String visibility = getVisibility(node.getModifiers());
        boolean isStatic = Modifier.isStatic(node.getModifiers());
        boolean isFinal = Modifier.isFinal(node.getModifiers());
        String comment = node.getJavadoc() == null ? "" : sourceContent.substring(node.getJavadoc().getStartPosition(), node.getJavadoc().getStartPosition() + node.getJavadoc().getLength());
        node.fragments().forEach(n -> {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) n;
            String name = fragment.getName().getFullyQualifiedName();
            String fullName = belongTo + "." + name;
            r.add(new JavaFieldInfo(db, name, fullName, type, visibility, isStatic, isFinal, comment, belongTo, fullType));
        });
        return r;
    }

    private void parseMethodBody(Set<IMethodBinding> methodCalls, StringBuilder fullVariables, StringBuilder fieldAccesses, Block methodBody) {
        if (methodBody == null)
            return;
        List<Statement> statementList = methodBody.statements();
        List<Statement> statements = new ArrayList<>();
        for (int i = 0; i < statementList.size(); i++) {
            statements.add(statementList.get(i));
        }
        for (int i = 0; i < statements.size(); i++) {

            if (statements.get(i).getNodeType() == ASTNode.BLOCK) {
                List<Statement> blockStatements = ((Block) statements.get(i)).statements();
                for (int j = 0; j < blockStatements.size(); j++) {
                    statements.add(i + j + 1, blockStatements.get(j));
                }
                continue;
            }
            if (statements.get(i).getNodeType() == ASTNode.ASSERT_STATEMENT) {
                Expression expression = ((AssertStatement) statements.get(i)).getExpression();
                if (expression != null) {
                    parseExpression(methodCalls, fieldAccesses, expression);
                }
                expression = ((AssertStatement) statements.get(i)).getMessage();
                if (expression != null) {
                    parseExpression(methodCalls, fieldAccesses, expression);
                }
            }

            if (statements.get(i).getNodeType() == ASTNode.DO_STATEMENT) {
                Expression expression = ((DoStatement) statements.get(i)).getExpression();
                if (expression != null) {
                    parseExpression(methodCalls, fieldAccesses, expression);
                }
                Statement doBody = ((DoStatement) statements.get(i)).getBody();
                if (doBody != null) {
                    statements.add(i + 1, doBody);
                }
            }
            if (statements.get(i).getNodeType() == ASTNode.ENHANCED_FOR_STATEMENT) {
                Expression expression = ((EnhancedForStatement) statements.get(i)).getExpression();
                Type type = ((EnhancedForStatement) statements.get(i)).getParameter().getType();
                fullVariables.append(NameResolver.getFullName(type) + ", ");
                if (expression != null) {
                    parseExpression(methodCalls, fieldAccesses, expression);
                }
                Statement forBody = ((EnhancedForStatement) statements.get(i)).getBody();
                if (forBody != null) {
                    statements.add(i + 1, forBody);
                }
            }
            if (statements.get(i).getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
                Expression expression = ((ExpressionStatement) statements.get(i)).getExpression();
                if (expression != null) {
                    parseExpression(methodCalls, fieldAccesses, expression);
                }
            }
            if (statements.get(i).getNodeType() == ASTNode.FOR_STATEMENT) {
                List<Expression> list = ((ForStatement) statements.get(i)).initializers();
                for (int j = 0; j < list.size(); j++) {
                    parseExpression(methodCalls, fieldAccesses, list.get(j));
                }
                Expression expression = ((ForStatement) statements.get(i)).getExpression();
                if (expression != null) {
                    parseExpression(methodCalls, fieldAccesses, expression);
                }
                Statement forBody = ((ForStatement) statements.get(i)).getBody();
                if (forBody != null) {
                    statements.add(i + 1, forBody);
                }
            }
            if (statements.get(i).getNodeType() == ASTNode.IF_STATEMENT) {
                Expression expression = ((IfStatement) statements.get(i)).getExpression();
                if (expression != null) {
                    parseExpression(methodCalls, fieldAccesses, expression);
                }
                Statement thenStatement = ((IfStatement) statements.get(i)).getThenStatement();
                Statement elseStatement = ((IfStatement) statements.get(i)).getElseStatement();
                if (elseStatement != null) {
                    statements.add(i + 1, elseStatement);
                }
                if (thenStatement != null) {
                    statements.add(i + 1, thenStatement);
                }
            }
            if (statements.get(i).getNodeType() == ASTNode.RETURN_STATEMENT) {
                Expression expression = ((ReturnStatement) statements.get(i)).getExpression();
                if (expression != null) {
                    parseExpression(methodCalls, fieldAccesses, expression);
                }
            }
            if (statements.get(i).getNodeType() == ASTNode.SWITCH_STATEMENT) {
                Expression expression = ((SwitchStatement) statements.get(i)).getExpression();
                if (expression != null) {
                    parseExpression(methodCalls, fieldAccesses, expression);
                }
                List<Statement> switchStatements = ((SwitchStatement) statements.get(i)).statements();
                for (int j = 0; j < switchStatements.size(); j++) {
                    statements.add(i + j + 1, switchStatements.get(j));
                }
            }
            if (statements.get(i).getNodeType() == ASTNode.THROW_STATEMENT) {
                Expression expression = ((ThrowStatement) statements.get(i)).getExpression();
                if (expression != null) {
                    parseExpression(methodCalls, fieldAccesses, expression);
                }
            }
            if (statements.get(i).getNodeType() == ASTNode.TRY_STATEMENT) {
                Statement tryStatement = ((TryStatement) statements.get(i)).getBody();
                if (tryStatement != null) {
                    statements.add(i + 1, tryStatement);
                }
                continue;
            }
            if (statements.get(i).getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT) {
                Type type = ((VariableDeclarationStatement) statements.get(i)).getType();
                fullVariables.append(NameResolver.getFullName(type) + ", ");
                ((VariableDeclarationStatement) statements.get(i)).fragments().forEach(n -> parseExpression(methodCalls, fieldAccesses, ((VariableDeclaration) n).getInitializer()));
            }
            if (statements.get(i).getNodeType() == ASTNode.WHILE_STATEMENT) {
                Expression expression = ((WhileStatement) statements.get(i)).getExpression();
                if (expression != null) {
                    parseExpression(methodCalls, fieldAccesses, expression);
                }
                Statement whileBody = ((WhileStatement) statements.get(i)).getBody();
                if (whileBody != null) {
                    statements.add(i + 1, whileBody);
                }
            }
        }
    }

    private class ExpressionVisitor extends ASTVisitor {
        private final Set<IMethodBinding> methodCalls;
        private final StringBuilder fieldAccesses;

        private ExpressionVisitor(Set<IMethodBinding> methodCalls, StringBuilder fieldAccesses) {
            this.methodCalls = methodCalls;
            this.fieldAccesses = fieldAccesses;
        }

        @Override
        public boolean visit(MethodInvocation node) {
            IMethodBinding methodBinding = node.resolveMethodBinding();
            if (methodBinding != null) methodCalls.add(methodBinding);
            return true;
        }

        @Override
        public boolean visit(QualifiedName node) {
            if (node.getQualifier().resolveTypeBinding() != null) {
                String name = node.getQualifier().resolveTypeBinding().getQualifiedName() + "." + node.getName().getIdentifier();
                fieldAccesses.append(name).append(", ");
            }
            return true;
        }

    }

    private void parseExpression(Set<IMethodBinding> methodCalls, StringBuilder fieldAccesses, Expression expression) {
        if (expression == null) {
            return;
        }
        expression.accept(new ExpressionVisitor(methodCalls, fieldAccesses));
    }

    private static String getVisibility(int modifiers) {
        if (Modifier.isPrivate(modifiers))
            return "private";
        if (Modifier.isProtected(modifiers))
            return "protected";
        if (Modifier.isPublic(modifiers))
            return "public";
        return "package";
    }

}
