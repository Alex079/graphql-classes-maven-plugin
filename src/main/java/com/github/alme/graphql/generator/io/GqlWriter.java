package com.github.alme.graphql.generator.io;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.stream.Collectors;

import com.github.alme.graphql.generator.dto.GqlConfiguration;
import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.dto.GqlOperation;
import com.github.alme.graphql.generator.writer.EnumWriter;
import com.github.alme.graphql.generator.writer.InterfaceWriter;
import com.github.alme.graphql.generator.writer.UnionWriter;

import org.apache.maven.plugin.MojoExecutionException;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GqlWriter {

	private static final String OPERATION_INTERFACE_TEMPLATE = "OPERATION_INTERFACE";
	private static final String OPERATION_TEMPLATE = "OPERATION";
	private static final String UNNAMED_OPERATION = "Unnamed";
	private static final String INTERFACE_NAME_KEY = "interfaceName";
	private static final String CLASS_NAME_KEY = "className";
	private static final String ANNOTATION_KEY = "jsonProperty";
	private static final String CHAINED_ACCESSORS_KEY = "useChainedAccessors";
	private static final String IMPORT_PACKAGES_KEY = "importPackages";
	private static final String ANNOTATION_VERSION_KEY = "annotationVersion";
	private static final String FILE_EXTENSION = ".java";
	private static final String BASE_PACKAGE_KEY = "basePackage";
	private static final String TYPES_PACKAGE_KEY = "typesPackage";
	private static final String LOG_CANNOT_CREATE = "Cannot create [%s] due to error.";
	private static final Configuration CFG = new Configuration(Configuration.VERSION_2_3_31);

	static {
		CFG.setClassLoaderForTemplateLoading(GqlWriter.class.getClassLoader(), "/templates/java");
		CFG.setDefaultEncoding("UTF-8");
		CFG.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		CFG.setLogTemplateExceptions(false);
		CFG.setWrapUncheckedExceptions(true);
		CFG.setFallbackOnNullLoopVariable(false);
	}

	private final WriterFactory writerFactory;

	public void write(GqlContext context, GqlConfiguration configuration) throws MojoExecutionException {

		try {
			CFG.setSharedVariable(BASE_PACKAGE_KEY, configuration.getBasePackageName());
			CFG.setSharedVariable(TYPES_PACKAGE_KEY, configuration.getTypesPackageName());
			CFG.setSharedVariable(ANNOTATION_KEY, configuration.getJsonPropertyAnnotation());
			CFG.setSharedVariable(CHAINED_ACCESSORS_KEY, configuration.isUseChainedAccessors());
			CFG.setSharedVariable(IMPORT_PACKAGES_KEY, configuration.getImportPackages());
			CFG.setSharedVariable(ANNOTATION_VERSION_KEY, configuration.getGeneratedAnnotationVersion());
		} catch (TemplateModelException e) {
			throw new MojoExecutionException("Cannot set shared variables.", e);
		}
		dumpTypeClasses(context, configuration);
		dumpOperationInterfaces(context, configuration);
		dumpOperationClasses(context, configuration);
		CFG.clearSharedVariables();
	}

	private void dumpTypeClasses(GqlContext context, GqlConfiguration configuration) {
		new EnumWriter().write(context, configuration);
		new InterfaceWriter().write(context, configuration);
		new UnionWriter().write(context, configuration);
//		context.getStructures().forEach((category, structures) ->
//			structures.forEach((name, type) -> {
//				Path path = configuration.getTypesPackagePath().resolve(name + FILE_EXTENSION);
//				try (Writer writer = writerFactory.getWriter(path)) {
//					CFG.getTemplate(category.name()).process(type, writer);
//				} catch (TemplateException | IOException e) {
//					context.getLog().error(String.format(LOG_CANNOT_CREATE, name), e);
//				}
//			})
//		);
	}

	private void dumpOperationInterfaces(GqlContext context, GqlConfiguration configuration) {
		context.getOperations().values().stream()
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
				Path path = configuration.getBasePackagePath().resolve(interfaceName + FILE_EXTENSION);
				try (Writer writer = writerFactory.getWriter(path)) {
					CFG.getTemplate(OPERATION_INTERFACE_TEMPLATE).process(null, writer);
				} catch (TemplateException | IOException e) {
					context.getLog().error(String.format(LOG_CANNOT_CREATE, interfaceName), e);
				}
			});
	}

	private void dumpOperationClasses(GqlContext context, GqlConfiguration configuration) {
		context.getOperations().forEach((name, operation) -> {
			String interfaceName = capitalize(operation.getOperation());
			String className = (name == null ? UNNAMED_OPERATION : capitalize(name)) + interfaceName;
			try {
				CFG.setSharedVariable(CLASS_NAME_KEY, className);
				CFG.setSharedVariable(INTERFACE_NAME_KEY, interfaceName);
			} catch (TemplateModelException e) {
				context.getLog().error(String.format(LOG_CANNOT_CREATE, className), e);
				return;
			}
			Path path = configuration.getBasePackagePath().resolve(className + FILE_EXTENSION);
			try (Writer writer = writerFactory.getWriter(path)) {
				CFG.getTemplate(OPERATION_TEMPLATE).process(operation, writer);
			} catch (TemplateException | IOException e) {
				context.getLog().error(String.format(LOG_CANNOT_CREATE, className), e);
			}
		});
	}

	private static String capitalize(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

}
