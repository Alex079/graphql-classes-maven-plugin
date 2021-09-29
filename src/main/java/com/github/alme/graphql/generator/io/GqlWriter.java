package com.github.alme.graphql.generator.io;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.stream.Collectors;

import com.github.alme.graphql.generator.dto.GqlConfiguration;
import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.dto.GqlOperation;

import org.apache.maven.plugin.MojoExecutionException;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GqlWriter {

	private static final String OPERATION_INTERFACE_TEMPLATE = "OPERATION_INTERFACE";
	private static final String DEFINED_OPERATION_TEMPLATE = "DEFINED_OPERATION";
	private static final String DYNAMIC_OPERATION_TEMPLATE = "DYNAMIC_OPERATION";
	private static final String UNNAMED_OPERATION = "Unnamed";
	private static final String DYNAMIC_OPERATION = "Dynamic";
	private static final String INTERFACE_NAME_KEY = "interfaceName";
	private static final String CLASS_NAME_KEY = "className";
	private static final String TYPE_NAME_KEY = "typeName";
	private static final String JSON_PROPERTY_KEY = "jsonProperty";
	private static final String METHOD_CHAINING_KEY = "methodChaining";
	private static final String DTO_BUILDER_KEY = "dtoBuilder";
	private static final String IMPORT_PACKAGES_KEY = "importPackages";
	private static final String GENERATED_ANNOTATION_KEY = "generatedAnnotation";
	private static final String FILE_EXTENSION = ".java";
	private static final String SCHEMA_TYPES_PACKAGE_KEY = "schemaTypesPackage";
	private static final String OPERATIONS_PACKAGE_KEY = "operationsPackage";
	private static final String DEFINED_OPERATIONS_PACKAGE_KEY = "definedOperationsPackage";
	private static final String DYNAMIC_OPERATIONS_PACKAGE_KEY = "dynamicOperationsPackage";
	private static final String LOG_CANNOT_CREATE = "Cannot create [%s].";
	private static final Configuration CFG = new Configuration(Configuration.VERSION_2_3_31);

	static {
		CFG.setClassLoaderForTemplateLoading(GqlWriter.class.getClassLoader(), "/templates/java");
		CFG.setDefaultEncoding("UTF-8");
		CFG.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		CFG.setLogTemplateExceptions(false);
		CFG.setWrapUncheckedExceptions(true);
		CFG.setFallbackOnNullLoopVariable(false);
		CFG.setTabSize(4);
	}

	private final WriterFactory writerFactory;

	public void write(GqlContext context, GqlConfiguration configuration) throws MojoExecutionException {

		try {
			CFG.setSharedVariable(SCHEMA_TYPES_PACKAGE_KEY, configuration.getSchemaTypesPackageName());
			CFG.setSharedVariable(OPERATIONS_PACKAGE_KEY, configuration.getOperationsPackageName());
			CFG.setSharedVariable(DEFINED_OPERATIONS_PACKAGE_KEY, configuration.getDefinedOperationsPackageName());
			CFG.setSharedVariable(DYNAMIC_OPERATIONS_PACKAGE_KEY, configuration.getDynamicOperationsPackageName());
			CFG.setSharedVariable(JSON_PROPERTY_KEY, configuration.getJsonPropertyAnnotation());
			CFG.setSharedVariable(METHOD_CHAINING_KEY, configuration.isGenerateMethodChaining());
			CFG.setSharedVariable(DTO_BUILDER_KEY, configuration.isGenerateDtoBuilder());
			CFG.setSharedVariable(IMPORT_PACKAGES_KEY, configuration.getImportPackages());
			CFG.setSharedVariable(GENERATED_ANNOTATION_KEY, configuration.getGeneratedAnnotation());
		} catch (TemplateModelException e) {
			throw new MojoExecutionException("Cannot set shared variables.", e);
		}
		saveSchemaTypes(context, configuration);
		boolean generateDefinedOperations = configuration.isGenerateDefinedOperations();
		boolean generateDynamicOperations = configuration.isGenerateDynamicOperations();
		if (generateDefinedOperations || generateDynamicOperations) {
			saveOperationInterfaces(context, configuration);
		}
		if (generateDefinedOperations) {
			saveDefinedOperations(context, configuration);
		}
		if (generateDynamicOperations) {
			saveDynamicOperations(context, configuration);
		}
		CFG.clearSharedVariables();
	}

	private void saveSchemaTypes(GqlContext context, GqlConfiguration configuration) {
		context.getStructures().forEach((category, structures) ->
			structures.forEach((name, type) -> {
				Path path = configuration.getSchemaTypesPackagePath().resolve(name + FILE_EXTENSION);
				try (Writer writer = writerFactory.getWriter(path)) {
					CFG.getTemplate(category.name()).process(type, writer);
				} catch (TemplateException | IOException e) {
					context.getLog().error(String.format(LOG_CANNOT_CREATE, name), e);
				}
			})
		);
	}

	private void saveOperationInterfaces(GqlContext context, GqlConfiguration configuration) {
		context.getDefinedOperations().values().stream()
			.map(GqlOperation::getOperation)
			.map(GqlWriter::capitalize)
			.collect(Collectors.toSet())
			.forEach(interfaceName -> {
				try {
					CFG.setSharedVariable(INTERFACE_NAME_KEY, interfaceName);
				} catch (TemplateModelException e) {
					context.getLog().error(String.format(LOG_CANNOT_CREATE, interfaceName), e);
					return;
				}
				Path path = configuration.getOperationsPackagePath().resolve(interfaceName + FILE_EXTENSION);
				try (Writer writer = writerFactory.getWriter(path)) {
					CFG.getTemplate(OPERATION_INTERFACE_TEMPLATE).process(null, writer);
				} catch (TemplateException | IOException e) {
					context.getLog().error(String.format(LOG_CANNOT_CREATE, interfaceName), e);
				}
			});
	}

	private void saveDefinedOperations(GqlContext context, GqlConfiguration configuration) {
		context.getDefinedOperations().forEach((name, operation) -> {
			String interfaceName = capitalize(operation.getOperation());
			String className = (name == null ? UNNAMED_OPERATION : capitalize(name)) + interfaceName;
			try {
				CFG.setSharedVariable(CLASS_NAME_KEY, className);
				CFG.setSharedVariable(INTERFACE_NAME_KEY, interfaceName);
			} catch (TemplateModelException e) {
				context.getLog().error(String.format(LOG_CANNOT_CREATE, className), e);
				return;
			}
			Path path = configuration.getDefinedOperationsPackagePath().resolve(className + FILE_EXTENSION);
			try (Writer writer = writerFactory.getWriter(path)) {
				CFG.getTemplate(DEFINED_OPERATION_TEMPLATE).process(operation, writer);
			} catch (TemplateException | IOException e) {
				context.getLog().error(String.format(LOG_CANNOT_CREATE, className), e);
			}
		});
	}

	private void saveDynamicOperations(GqlContext context, GqlConfiguration configuration) {
		context.getSchema().forEach((operation, typeName) -> {
			String interfaceName = capitalize(operation);
			String className = DYNAMIC_OPERATION + interfaceName;
			try {
				CFG.setSharedVariable(CLASS_NAME_KEY, className);
				CFG.setSharedVariable(INTERFACE_NAME_KEY, interfaceName);
				CFG.setSharedVariable(TYPE_NAME_KEY, typeName);
			} catch (TemplateModelException e) {
				context.getLog().error(String.format(LOG_CANNOT_CREATE, className), e);
				return;
			}
			Path path = configuration.getDynamicOperationsPackagePath().resolve(className + FILE_EXTENSION);
			try (Writer writer = writerFactory.getWriter(path)) {
				CFG.getTemplate(DYNAMIC_OPERATION_TEMPLATE).process(context, writer);
			} catch (TemplateException | IOException e) {
				context.getLog().error(String.format(LOG_CANNOT_CREATE, className), e);
			}
		});
	}

	private static String capitalize(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

}
