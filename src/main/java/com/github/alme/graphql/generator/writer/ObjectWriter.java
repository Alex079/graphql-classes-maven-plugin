package com.github.alme.graphql.generator.writer;

import static com.github.alme.graphql.generator.writer.Util.addClassAnnotations;
import static com.github.alme.graphql.generator.writer.Util.addImports;
import static com.github.alme.graphql.generator.writer.Util.writeFile;

import com.github.alme.graphql.generator.dto.GqlConfiguration;
import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;

public class ObjectWriter implements StructureWriter {

    @Override
    public void write(GqlContext context, GqlConfiguration configuration) {
        context.getObjectTypes().forEach((objectName, objectType) -> {
            CompilationUnit compilationUnit = new CompilationUnit(configuration.getTypesPackageName());
            addImports(compilationUnit, configuration);
            ClassOrInterfaceDeclaration declaration = compilationUnit.addClass(objectType.getName());
            addClassAnnotations(declaration, configuration);
            objectType.getMembers().forEach(declaration::addImplementedType);
            String fieldAnnotation = configuration.getJsonPropertyAnnotation();
            objectType.getFields().forEach(gqlField -> {
                String fieldName = gqlField.getName();
                if (fieldAnnotation != null) {
                    fieldName += "__";
                }
                FieldDeclaration field = declaration.addPrivateField(gqlField.getType().getFull(), fieldName);
                if (fieldAnnotation != null) {
                    field.addSingleMemberAnnotation(fieldAnnotation, new StringLiteralExpr(gqlField.getName()));
                }
                declaration
                    .addMethod(Util.getter(gqlField.getName()), Modifier.Keyword.PUBLIC)
                    .setType(gqlField.getType().getFull())
                    .createBody()
                    .addStatement(new ReturnStmt(fieldName));
                MethodDeclaration setter = declaration
                    .addMethod(Util.setter(gqlField.getName()))
                    .addParameter(gqlField.getType().getFull(), "v");
                BlockStmt setterBody = setter
                    .createBody()
                    .addStatement(new AssignExpr(new NameExpr(fieldName), new NameExpr("v"), AssignExpr.Operator.ASSIGN));
                if (configuration.isUseChainedAccessors()) {
                    setter.setType(objectName);
                    setterBody.addStatement(new ReturnStmt(new ThisExpr()));
                }
            });
            writeFile(compilationUnit.toString(), configuration.getTypesPackagePath(), objectName, context.getLog());
        });
    }

}
