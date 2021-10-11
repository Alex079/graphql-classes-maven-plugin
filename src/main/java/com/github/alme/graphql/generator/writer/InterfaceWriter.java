package com.github.alme.graphql.generator.writer;

import static com.github.alme.graphql.generator.writer.Util.addClassAnnotations;
import static com.github.alme.graphql.generator.writer.Util.addImports;

import com.github.alme.graphql.generator.dto.GqlConfiguration;
import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.io.WriterFactory;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InterfaceWriter implements StructureWriter {

    private final WriterFactory writerFactory;

    @Override
    public void write(GqlContext context, GqlConfiguration configuration) {
        context.getInterfaceTypes().forEach((interfaceName, interfaceType) -> {
            CompilationUnit compilationUnit = new CompilationUnit(configuration.getTypesPackageName());
            addImports(compilationUnit, configuration);
            ClassOrInterfaceDeclaration declaration = compilationUnit.addInterface(interfaceType.getName());
            addClassAnnotations(declaration, configuration);
            interfaceType.getMembers().forEach(declaration::addExtendedType);
            interfaceType.getFields().forEach(gqlField -> {
                declaration
                    .addMethod(Util.getter(gqlField.getName()))
                    .setType(gqlField.getType().getFull())
                    .removeBody();
                MethodDeclaration setter = declaration
                    .addMethod(Util.setter(gqlField.getName()))
                    .removeBody()
                    .addParameter(gqlField.getType().getFull(), "v");
                if (configuration.isUseChainedAccessors()) {
                    setter.setType(interfaceName);
                }
            });
            writerFactory.writeCompilationUnit(compilationUnit, configuration.getTypesPackagePath(), interfaceName);
        });
    }

}
