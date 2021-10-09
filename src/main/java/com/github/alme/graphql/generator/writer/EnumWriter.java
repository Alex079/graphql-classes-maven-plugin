package com.github.alme.graphql.generator.writer;

import static com.github.alme.graphql.generator.writer.Util.addClassAnnotations;
import static com.github.alme.graphql.generator.writer.Util.writeFile;

import com.github.alme.graphql.generator.dto.GqlConfiguration;
import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.EnumDeclaration;

public class EnumWriter implements StructureWriter {

    @Override
    public void write(GqlContext context, GqlConfiguration configuration) {
        context.getEnumTypes().forEach((enumName, enumType) -> {
            CompilationUnit compilationUnit = new CompilationUnit(configuration.getTypesPackageName());
            EnumDeclaration declaration = compilationUnit.addEnum(enumType.getName());
            addClassAnnotations(declaration, configuration);
            enumType.getMembers().forEach(declaration::addEnumConstant);
            writeFile(compilationUnit.toString(), configuration.getTypesPackagePath(), enumName, context.getLog());
        });
    }

}
