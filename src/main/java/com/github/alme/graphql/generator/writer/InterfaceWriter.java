package com.github.alme.graphql.generator.writer;

import static com.github.alme.graphql.generator.writer.Util.addClassAnnotations;
import static com.github.alme.graphql.generator.writer.Util.addImports;
import static com.github.alme.graphql.generator.writer.Util.writeFile;

import com.github.alme.graphql.generator.dto.GqlConfiguration;
import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

public class InterfaceWriter implements StructureWriter {

    @Override
    public void write(GqlContext context, GqlConfiguration configuration) {
        context.getInterfaceTypes().forEach((interfaceName, interfaceType) -> {
            CompilationUnit compilationUnit = new CompilationUnit(configuration.getTypesPackageName());
            addImports(compilationUnit, configuration);
            ClassOrInterfaceDeclaration declaration = compilationUnit.addInterface(interfaceType.getName());
            addClassAnnotations(declaration, configuration);
            interfaceType.getMembers().forEach(declaration::addExtendedType);
            interfaceType.getFields().forEach(gqlField -> {
                addGetter(declaration, gqlField)
                    .removeBody();
                MethodDeclaration setter = declaration
                    .addMethod(Util.setter(gqlField.getName()))
                    .removeBody()
                    .addParameter(gqlField.getType().getFull(), "v");
                if (configuration.isUseChainedAccessors()) {
                    setter.setType(interfaceName);
                }
            });
            writeFile(compilationUnit.toString(), configuration.getTypesPackagePath(), interfaceName, context.getLog());
        });
    }

    private MethodDeclaration addGetter(ClassOrInterfaceDeclaration declaration, com.github.alme.graphql.generator.dto.GqlField gqlField) {
        return declaration
            .addMethod(Util.getter(gqlField.getName()))
            .setType(gqlField.getType().getFull());
    }

}
