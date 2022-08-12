package com.github.alme.graphql.generator.io;

import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.groupingBy;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Collection;

import com.github.alme.graphql.generator.dto.GqlConfiguration;
import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.dto.GqlOperation;
import com.github.alme.graphql.generator.dto.GqlSelection;
import com.github.alme.graphql.generator.dto.GqlStructure;
import com.github.alme.graphql.generator.dto.Structure;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GqlWriter {

	private static final String APPENDER_TEMPLATE = "APPENDER";
	private static final String OPERATION_INTERFACE_TEMPLATE = "OPERATION_INTERFACE";
	private static final String DEFINED_OPERATION_TEMPLATE = "DEFINED_OPERATION";
	private static final String DEFINED_OPERATION_VARIABLES_TEMPLATE = "DEFINED_OPERATION_VARIABLES";
	private static final String DEFINED_OPERATION_RESULT_TEMPLATE = "DEFINED_OPERATION_RESULT";
	private static final String DYNAMIC_OPERATION_TEMPLATE = "DYNAMIC_OPERATION";
	private static final String DYNAMIC_OPERATION_SELECTOR_TEMPLATE = "DYNAMIC_OPERATION_SELECTOR";
	private static final String DYNAMIC_OPERATION_RESULT_TEMPLATE = "DYNAMIC_OPERATION_RESULT";
	private static final String APPENDER_CLASS_NAME = "GraphQlAppender";
	private static final String UNNAMED_OPERATION = "Unnamed";
	private static final String DYNAMIC_OPERATION = "Dynamic";
	private static final String INTERFACE_NAME_KEY = "interfaceName";
	private static final String CLASS_NAME_KEY = "className";
	private static final String JSON_PROPERTY_KEY = "jsonProperty";
	private static final String PROPERTY_PREFIX_KEY = "propertyPrefix";
	private static final String PROPERTY_SUFFIX_KEY = "propertySuffix";
	private static final String METHOD_CHAINING_KEY = "methodChaining";
	private static final String DTO_BUILDER_KEY = "dtoBuilder";
	private static final String IMPORT_PACKAGES_KEY = "importPackages";
	private static final String GENERATED_ANNOTATION_KEY = "generatedAnnotation";
	private static final String FILE_EXTENSION = ".java";
	private static final String SCHEMA_TYPES_PACKAGE_KEY = "schemaTypesPackage";
	private static final String OPERATIONS_PACKAGE_KEY = "operationsPackage";
	private static final String RESULTS_PACKAGE_KEY = "resultsPackage";
	private static final String SELECTORS_PACKAGE_KEY = "selectorsPackage";
	private static final String CURRENT_PACKAGE_KEY = "currentPackage";
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
			CFG.setSharedVariable(RESULTS_PACKAGE_KEY, configuration.getResultsPackageName());
			CFG.setSharedVariable(SELECTORS_PACKAGE_KEY, configuration.getSelectorsPackageName());
			CFG.setSharedVariable(JSON_PROPERTY_KEY, configuration.getJsonPropertyAnnotation());
			CFG.setSharedVariable(PROPERTY_PREFIX_KEY, configuration.getJsonPropertyPrefix());
			CFG.setSharedVariable(PROPERTY_SUFFIX_KEY, configuration.getJsonPropertySuffix());
			CFG.setSharedVariable(METHOD_CHAINING_KEY, configuration.isGenerateMethodChaining());
			CFG.setSharedVariable(DTO_BUILDER_KEY, configuration.isGenerateDtoBuilder());
			CFG.setSharedVariable(IMPORT_PACKAGES_KEY, configuration.getImportPackages());
			CFG.setSharedVariable(GENERATED_ANNOTATION_KEY, configuration.getGeneratedAnnotation());
		} catch (TemplateModelException e) {
			throw new MojoExecutionException("Cannot set shared variables.", e);
		}
		Log log = context.getLog();
		boolean generateSchemaInputTypes = configuration.isGenerateSchemaInputTypes();
		boolean generateSchemaOtherTypes = configuration.isGenerateSchemaOtherTypes();
		if (generateSchemaInputTypes) {
			context.getInputObjectTypes().forEach((name, type) -> makeStructure(log, configuration, Structure.INPUT_OBJECT, name, type));
		}
		if (generateSchemaInputTypes || generateSchemaOtherTypes) {
			context.getEnumTypes().forEach((name, type) -> makeStructure(log, configuration, Structure.ENUM, name, type));
		}
		if (generateSchemaOtherTypes) {
			context.getInterfaceTypes().forEach((name, type) -> makeStructure(log, configuration, Structure.INTERFACE, name, type));
			context.getUnionTypes().forEach((name, type) -> makeStructure(log, configuration, Structure.UNION, name, type));
			context.getObjectTypes().forEach((name, type) -> makeStructure(log, configuration, Structure.OBJECT, name, type));
		}
		boolean generateDefinedOperations = configuration.isGenerateDefinedOperations() && !context.getDefinedOperations().isEmpty();
		boolean generateDynamicOperations = configuration.isGenerateDynamicOperations() && !context.getDynamicOperations().isEmpty();
		if (generateDefinedOperations || generateDynamicOperations) {
			context.getSchema().keySet().stream()
				.map(GqlWriter::firstUpper)
				.forEach(interfaceName -> makeOperationInterface(log, configuration, interfaceName));
		}
		if (generateDefinedOperations) {
			context.getDefinedOperations().forEach((name, operation) -> makeDefinedOperation(log, configuration, name, operation));
		}
		if (generateDynamicOperations) {
			context.getDynamicOperations().forEach(operation -> makeDynamicOperation(log, configuration, operation));
			context.getDynamicSelections().forEach((typeName, selections) -> {
				makeDynamicOperationResult(log, configuration, typeName, selections);
				makeDynamicOperationSelector(log, configuration, typeName, selections);
			});
		}
		if (
			(generateSchemaInputTypes && !context.getInputObjectTypes().isEmpty()) ||
			(generateDynamicOperations && !context.getDynamicOperations().isEmpty())
		) {
			makeSharedClasses(log, configuration);
		}
		CFG.clearSharedVariables();
	}

	private void makeStructure(Log log, GqlConfiguration configuration, Structure category, String name, GqlStructure type) {
		Path path = configuration.getSchemaTypesPackagePath().resolve(name + FILE_EXTENSION);
		try (Writer writer = writerFactory.getWriter(path)) {
			CFG.getTemplate(category.name()).process(type, writer);
		} catch (TemplateException | IOException e) {
			log.error(String.format(LOG_CANNOT_CREATE, path), e);
		}
	}

	private void makeSharedClasses(Log log, GqlConfiguration configuration) {
		Path path = configuration.getOperationsPackagePath().resolve(APPENDER_CLASS_NAME + FILE_EXTENSION);
		try (Writer writer = writerFactory.getWriter(path)) {
			CFG.getTemplate(APPENDER_TEMPLATE).process(null, writer);
		} catch (TemplateException | IOException e) {
			log.error(String.format(LOG_CANNOT_CREATE, path), e);
		}
	}

	private void makeOperationInterface(Log log, GqlConfiguration configuration, String interfaceName) {
		Path path = configuration.getOperationsPackagePath().resolve(interfaceName + FILE_EXTENSION);
		try (Writer writer = writerFactory.getWriter(path)) {
			CFG.getTemplate(OPERATION_INTERFACE_TEMPLATE).process(singletonMap(INTERFACE_NAME_KEY, interfaceName), writer);
		} catch (TemplateException | IOException e) {
			log.error(String.format(LOG_CANNOT_CREATE, path), e);
		}
	}

	private void makeDefinedOperation(Log log, GqlConfiguration configuration, String name, GqlOperation operation) {
		String interfaceName = firstUpper(operation.getOperation());
		String className = (name == null ? UNNAMED_OPERATION : firstUpper(name)) + interfaceName;
		String currentPackageName = configuration.getOperationsPackageName()+"."+firstLower(className);
		Path currentPackagePath = configuration.getOperationsPackagePath().resolve(firstLower(className));
		Path path = currentPackagePath.resolve(className + FILE_EXTENSION);
		try {
			CFG.setSharedVariable(CLASS_NAME_KEY, className);
			CFG.setSharedVariable(INTERFACE_NAME_KEY, interfaceName);
			CFG.setSharedVariable(CURRENT_PACKAGE_KEY, currentPackageName);
		} catch (TemplateModelException e) {
			log.error(String.format(LOG_CANNOT_CREATE, path), e);
			return;
		}
		try (Writer writer = writerFactory.getWriter(path)) {
			CFG.getTemplate(DEFINED_OPERATION_TEMPLATE).process(operation, writer);
		} catch (TemplateException | IOException e) {
			log.error(String.format(LOG_CANNOT_CREATE, path), e);
			return;
		}
		if (!operation.getVariables().isEmpty()) {
			makeOperationVariables(log, currentPackagePath, currentPackageName, name, operation);
		}
		if (!operation.getSelections().isEmpty()) {
			makeDefinedOperationResult(log, currentPackagePath, currentPackageName, className, operation.getSelections());
		}
	}

	private void makeOperationVariables(Log log, Path packagePath, String packageName, String name, GqlOperation operation) {
		String interfaceName = firstUpper(operation.getOperation());
		String className = (name == null ? UNNAMED_OPERATION : firstUpper(name)) + interfaceName + "Variables";
		Path path = packagePath.resolve(className + FILE_EXTENSION);
		try {
			CFG.setSharedVariable(CLASS_NAME_KEY, className);
			CFG.setSharedVariable(CURRENT_PACKAGE_KEY, packageName);
		} catch (TemplateModelException e) {
			log.error(String.format(LOG_CANNOT_CREATE, path), e);
			return;
		}
		try (Writer writer = writerFactory.getWriter(path)) {
			CFG.getTemplate(DEFINED_OPERATION_VARIABLES_TEMPLATE).process(operation, writer);
		} catch (TemplateException | IOException e) {
			log.error(String.format(LOG_CANNOT_CREATE, path), e);
		}
	}

	private void makeDefinedOperationResult(Log log, Path packagePath, String packageName, String typeName, Collection<GqlSelection> selections) {
		String className = typeName + "Result";
		Path path = packagePath.resolve(className + FILE_EXTENSION);
		try {
			CFG.setSharedVariable(CLASS_NAME_KEY, className);
			CFG.setSharedVariable(CURRENT_PACKAGE_KEY, packageName);
		} catch (TemplateModelException e) {
			log.error(String.format(LOG_CANNOT_CREATE, path), e);
			return;
		}
		try (Writer writer = writerFactory.getWriter(path)) {
			CFG.getTemplate(DEFINED_OPERATION_RESULT_TEMPLATE).process(singletonMap("selections", selections), writer);
		} catch (TemplateException | IOException e) {
			log.error(String.format(LOG_CANNOT_CREATE, path), e);
		}
		selections.forEach(selection -> {
			if (selection.getSelections() == null || selection.getSelections().isEmpty()) {
				return;
			}
			makeDefinedOperationResult(log,
				packagePath.resolve(selection.getTitle()),
				packageName + "." + selection.getTitle(),
				selection.getType().getInner(),
				selection.getSelections());
		});
	}

	private void makeDynamicOperation(Log log, GqlConfiguration configuration, GqlOperation operation) {
		String interfaceName = firstUpper(operation.getOperation());
		String className = DYNAMIC_OPERATION + interfaceName;
		String currentPackageName = configuration.getOperationsPackageName()+"."+operation.getOperation();
		Path currentPackagePath = configuration.getOperationsPackagePath().resolve(operation.getOperation());
		Path path = currentPackagePath.resolve(className + FILE_EXTENSION);
		try {
			CFG.setSharedVariable(CLASS_NAME_KEY, className);
			CFG.setSharedVariable(INTERFACE_NAME_KEY, interfaceName);
			CFG.setSharedVariable(CURRENT_PACKAGE_KEY, currentPackageName);
		} catch (TemplateModelException e) {
			log.error(String.format(LOG_CANNOT_CREATE, path), e);
			return;
		}
		try (Writer writer = writerFactory.getWriter(path)) {
			CFG.getTemplate(DYNAMIC_OPERATION_TEMPLATE).process(operation, writer);
		} catch (TemplateException | IOException e) {
			log.error(String.format(LOG_CANNOT_CREATE, path), e);
		}
	}

	private void makeDynamicOperationSelector(Log log, GqlConfiguration configuration, String typeName, Collection<GqlSelection> selections) {
		String currentPackageName = configuration.getSelectorsPackageName();
		Path currentPackagePath = configuration.getSelectorsPackagePath();
		String className = typeName + "Selector";
		Path path = currentPackagePath.resolve(className + FILE_EXTENSION);
		try {
			CFG.setSharedVariable(CLASS_NAME_KEY, className);
			CFG.setSharedVariable(CURRENT_PACKAGE_KEY, currentPackageName);
		} catch (TemplateModelException e) {
			log.error(String.format(LOG_CANNOT_CREATE, path), e);
			return;
		}
		try (Writer writer = writerFactory.getWriter(path)) {
			CFG.getTemplate(DYNAMIC_OPERATION_SELECTOR_TEMPLATE).process(
				singletonMap("selections", selections.stream().collect(groupingBy(GqlSelection::getFragmentTypeName))),
				writer);
		} catch (TemplateException | IOException e) {
			log.error(String.format(LOG_CANNOT_CREATE, path), e);
		}
	}

	private void makeDynamicOperationResult(Log log, GqlConfiguration configuration, String typeName, Collection<GqlSelection> selections) {
		String currentPackageName = configuration.getResultsPackageName();
		Path currentPackagePath = configuration.getResultsPackagePath();
		String className = typeName + "Result";
		Path path = currentPackagePath.resolve(className + FILE_EXTENSION);
		try {
			CFG.setSharedVariable(CLASS_NAME_KEY, className);
			CFG.setSharedVariable(CURRENT_PACKAGE_KEY, currentPackageName);
		} catch (TemplateModelException e) {
			log.error(String.format(LOG_CANNOT_CREATE, path), e);
			return;
		}
		try (Writer writer = writerFactory.getWriter(path)) {
			CFG.getTemplate(DYNAMIC_OPERATION_RESULT_TEMPLATE).process(singletonMap("selections", selections), writer);
		} catch (TemplateException | IOException e) {
			log.error(String.format(LOG_CANNOT_CREATE, path), e);
		}
	}

	private static String firstUpper(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	private static String firstLower(String s) {
		return s.substring(0, 1).toLowerCase() + s.substring(1);
	}

}
