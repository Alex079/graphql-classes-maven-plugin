package com.github.alme.graphql.generator.writer;

import static com.github.alme.graphql.generator.writer.Util.addClassAnnotations;

import com.github.alme.graphql.generator.dto.GqlConfiguration;
import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.io.WriterFactory;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UnionWriter implements StructureWriter {

    private final WriterFactory writerFactory;

    @Override
    public void write(GqlContext context, GqlConfiguration configuration) {
        context.getUnionTypes().forEach((unionName, unionType) -> {
            CompilationUnit compilationUnit = new CompilationUnit(configuration.getTypesPackageName());
            ClassOrInterfaceDeclaration declaration = compilationUnit.addInterface(unionType.getName());
            addClassAnnotations(declaration, configuration);
            writerFactory.writeCompilationUnit(compilationUnit, configuration.getTypesPackagePath(), unionName);
        });
    }

}
