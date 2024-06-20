package com.github.alme.graphql.generator.io;

import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.groupingBy;

import java.nio.charset.StandardCharsets;

import com.github.alme.graphql.generator.dto.GqlConfiguration;
import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.dto.GqlSelection;
import com.github.alme.graphql.generator.dto.Structure;
import com.github.alme.graphql.generator.io.creator.AppenderFileCreator;
import com.github.alme.graphql.generator.io.creator.DefinedOperationFileCreator;
import com.github.alme.graphql.generator.io.creator.DefinedOperationResultFileCreator;
import com.github.alme.graphql.generator.io.creator.DefinedOperationVariablesFileCreator;
import com.github.alme.graphql.generator.io.creator.DynamicOperationFileCreator;
import com.github.alme.graphql.generator.io.creator.DynamicOperationResultFileCreator;
import com.github.alme.graphql.generator.io.creator.DynamicOperationSelectorFileCreator;
import com.github.alme.graphql.generator.io.creator.OperationInterfaceFileCreator;
import com.github.alme.graphql.generator.io.creator.StructureFileCreator;

import org.apache.maven.plugin.MojoExecutionException;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModelException;

public class GqlWriter {

	private static final String APPENDER_CLASS_NAME = "GraphQlAppender";
	private static final String JSON_PROPERTY_KEY = "jsonProperty";
	private static final String PROPERTY_PREFIX_KEY = "propertyPrefix";
	private static final String PROPERTY_SUFFIX_KEY = "propertySuffix";
	private static final String DTO_METHOD_CHAINING_KEY = "dtoMethodChaining";
	private static final String DTO_BUILDER_KEY = "dtoBuilder";
	private static final String DTO_SETTERS_KEY = "dtoSetters";
	private static final String DTO_CONSTRUCTOR_KEY = "dtoConstructor";
	private static final String IMPORT_PACKAGES_KEY = "importPackages";
	private static final String GENERATED_ANNOTATION_KEY = "generatedAnnotation";
	private static final String SCHEMA_TYPES_PACKAGE_KEY = "schemaTypesPackage";
	private static final String OPERATIONS_PACKAGE_KEY = "operationsPackage";
	private static final String DEFINED_OPERATIONS_PACKAGE_KEY = "definedOperationsPackage";
	private static final String DYNAMIC_OPERATIONS_PACKAGE_KEY = "dynamicOperationsPackage";

	private final WriterFactory writerFactory;
	private final Configuration freemarker;

	public GqlWriter(WriterFactory writerFactory) {
		this.writerFactory = writerFactory;
		freemarker = new Configuration(Configuration.VERSION_2_3_32);
		freemarker.setClassLoaderForTemplateLoading(GqlWriter.class.getClassLoader(), "/templates/java");
		freemarker.setDefaultEncoding(StandardCharsets.UTF_8.displayName());
		freemarker.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		freemarker.setLogTemplateExceptions(false);
		freemarker.setWrapUncheckedExceptions(true);
		freemarker.setFallbackOnNullLoopVariable(false);
		freemarker.setTabSize(4);
	}

	public void write(GqlContext context, GqlConfiguration configuration) throws MojoExecutionException {
		setTemplateVariables(configuration);
		createSharedClasses(context, configuration);
		createSchemaTypes(context, configuration);
		createOperationInterfaces(context, configuration);
		createDefinedOperations(context, configuration);
		createDynamicOperations(context, configuration);
		clearTemplateVariables();
	}

	private void setTemplateVariables(GqlConfiguration configuration) throws MojoExecutionException {
		try {
			freemarker.setSharedVariable(SCHEMA_TYPES_PACKAGE_KEY, configuration.getSchemaTypesPackageName());
			freemarker.setSharedVariable(OPERATIONS_PACKAGE_KEY, configuration.getOperationsPackageName());
			freemarker.setSharedVariable(DEFINED_OPERATIONS_PACKAGE_KEY, configuration.getDefinedOperationsPackageName());
			freemarker.setSharedVariable(DYNAMIC_OPERATIONS_PACKAGE_KEY, configuration.getDynamicOperationsPackageName());
			freemarker.setSharedVariable(JSON_PROPERTY_KEY, configuration.getJsonPropertyAnnotation());
			freemarker.setSharedVariable(PROPERTY_PREFIX_KEY, configuration.getJsonPropertyPrefix());
			freemarker.setSharedVariable(PROPERTY_SUFFIX_KEY, configuration.getJsonPropertySuffix());
			freemarker.setSharedVariable(DTO_METHOD_CHAINING_KEY, configuration.isGenerateDtoMethodChaining());
			freemarker.setSharedVariable(DTO_BUILDER_KEY, configuration.isGenerateDtoBuilder());
			freemarker.setSharedVariable(DTO_SETTERS_KEY, configuration.isGenerateDtoSetters());
			freemarker.setSharedVariable(DTO_CONSTRUCTOR_KEY, configuration.isGenerateDtoConstructor());
			freemarker.setSharedVariable(IMPORT_PACKAGES_KEY, configuration.getImportPackages());
			freemarker.setSharedVariable(GENERATED_ANNOTATION_KEY, configuration.getGeneratedAnnotation());
		} catch (TemplateModelException e) {
			throw new MojoExecutionException("Cannot set shared variables.", e);
		}
	}

	private void clearTemplateVariables() {
		freemarker.clearSharedVariables();
	}

	private void createSharedClasses(GqlContext context, GqlConfiguration configuration) {
		if ((configuration.isGenerateSchemaInputTypes() && !context.getInputObjectTypes().isEmpty()) ||
			(configuration.isGenerateDynamicOperations() && !context.getDynamicOperations().isEmpty())
		) {
			new AppenderFileCreator(writerFactory, freemarker)
				.createFile(configuration.getOperationsPackageName(), APPENDER_CLASS_NAME, null);
		}
	}

	private void createSchemaTypes(GqlContext context, GqlConfiguration configuration) {
		String packageName = configuration.getSchemaTypesPackageName();
		int count = 0;
		if (configuration.isGenerateSchemaInputTypes()) {
			var inputObjectFileCreator = new StructureFileCreator(writerFactory, freemarker, Structure.INPUT_OBJECT);
			context.getInputObjectTypes()
				.forEach((className, gqlStructure) -> inputObjectFileCreator
					.createFile(packageName, className, gqlStructure));
			count += context.getInputObjectTypes().size();
		}
		if (configuration.isGenerateSchemaInputTypes() || configuration.isGenerateSchemaOtherTypes()) {
			var enumFileCreator = new StructureFileCreator(writerFactory, freemarker, Structure.ENUM);
			context.getEnumTypes()
				.forEach((className, gqlStructure) -> enumFileCreator
					.createFile(packageName, className, gqlStructure));
			count += context.getEnumTypes().size();
		}
		if (configuration.isGenerateSchemaOtherTypes()) {
			var interfaceFileCreator = new StructureFileCreator(writerFactory, freemarker, Structure.INTERFACE);
			context.getInterfaceTypes()
				.forEach((className, gqlStructure) -> interfaceFileCreator
					.createFile(packageName, className, gqlStructure));
			count += context.getInterfaceTypes().size();
			var objectFileCreator = new StructureFileCreator(writerFactory, freemarker, Structure.OBJECT);
			context.getObjectTypes()
				.forEach((className, gqlStructure) -> objectFileCreator
					.createFile(packageName, className, gqlStructure));
			count += context.getObjectTypes().size();
		}
		if (count > 0) {
			context.getLog().info("Finished creating %d schema type class(es).".formatted(count));
		}
	}

	private void createOperationInterfaces(GqlContext context, GqlConfiguration configuration) {
		if ((configuration.isGenerateDefinedOperations() && !context.getDefinedOperations().isEmpty()) ||
			(configuration.isGenerateDynamicOperations() && !context.getDynamicOperations().isEmpty())
		) {
			var operationInterfaceFileCreator = new OperationInterfaceFileCreator(writerFactory, freemarker);
			context.getOperations().keySet().stream()
				.map(Util::firstUpper)
				.forEach(interfaceName -> operationInterfaceFileCreator
					.createFile(configuration.getOperationsPackageName(), interfaceName, singletonMap("javadoc", context.getSchemaJavadoc())));
		}
	}

	private void createDefinedOperations(GqlContext context, GqlConfiguration configuration) {
		if (configuration.isGenerateDefinedOperations() && !context.getDefinedOperations().isEmpty()) {
			var definedOperationFileCreator = new DefinedOperationFileCreator(writerFactory, freemarker);
			var definedOperationVariablesFileCreator = new DefinedOperationVariablesFileCreator(writerFactory, freemarker);
			context.getDefinedOperations().forEach((operationName, operation) -> {
				String packageName = configuration.getDefinedOperationsPackageName() + "." + Util.firstLower(operationName);
				definedOperationFileCreator.createFile(packageName, operationName, operation);
				if (!operation.getVariables().isEmpty()) {
					definedOperationVariablesFileCreator.createFile(packageName, operationName + "Variables", operation);
				}
			});
			var definedOperationResultFileCreator = new DefinedOperationResultFileCreator(writerFactory, freemarker);
			context.getDefinedSelections().forEach((operationName, typeMap) -> {
				String packageName = configuration.getDefinedOperationsPackageName() + "." + Util.firstLower(operationName) + ".results";
				typeMap.forEach((typeName, selections) -> definedOperationResultFileCreator
					.createFile(packageName, typeName, singletonMap("selections", selections)));
			});
			context.getDefinedOperations().forEach((operationName, operation) ->
				context.getLog().info("Finished creating %d class(es) for %s operation.".formatted(
					(operation.getVariables().isEmpty() ? 1 : 2) + context.getDefinedSelections().get(operationName).size(),
					operationName)));
		}
	}

	private void createDynamicOperations(GqlContext context, GqlConfiguration configuration) {
		if (configuration.isGenerateDynamicOperations() && !context.getDynamicOperations().isEmpty()) {
			String packageName = configuration.getDynamicOperationsPackageName();
			var dynamicOperationFileCreator = new DynamicOperationFileCreator(writerFactory, freemarker);
			context.getDynamicOperations()
				.forEach((className, operation) -> dynamicOperationFileCreator.createFile(packageName, className, operation));
			var dynamicOperationResultFileCreator = new DynamicOperationResultFileCreator(writerFactory, freemarker);
			var dynamicOperationSelectorFileCreator = new DynamicOperationSelectorFileCreator(writerFactory, freemarker);
			context.getDynamicSelections()
				.forEach((className, selections) -> {
					dynamicOperationResultFileCreator.createFile(packageName + ".results", className, singletonMap("selections", selections));
					dynamicOperationSelectorFileCreator.createFile(packageName + ".selectors", className + "Selector",
						singletonMap("selections", selections.stream().collect(groupingBy(GqlSelection::getFragmentTypeName))));
				});
			context.getLog().info("Finished creating %d dynamic operation(s) with %d common class(es).".formatted(
				context.getDynamicOperations().size(),
				context.getDynamicSelections().size() * 2));
		}
	}

}
