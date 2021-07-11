package com.github.alme.graphql.generator.writer;

import static com.github.alme.graphql.generator.writer.Util.addClassAnnotations;
import static com.github.alme.graphql.generator.writer.Util.writeFile;

import com.github.alme.graphql.generator.dto.GqlConfiguration;
import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

public class UnionWriter implements StructureWriter {

    @Override
    public void write(GqlContext context, GqlConfiguration configuration) {
        context.getUnionTypes().forEach((unionName, unionType) -> {
            CompilationUnit compilationUnit = new CompilationUnit(configuration.getTypesPackageName());
            ClassOrInterfaceDeclaration declaration = compilationUnit.addInterface(unionType.getName());
            addClassAnnotations(declaration, configuration);
            writeFile(compilationUnit.toString(), configuration.getTypesPackagePath(), unionName, context.getLog());
        });
    }

}
