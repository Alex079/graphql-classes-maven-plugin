package com.github.alme.graphql.generator.io;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.maven.plugin.MojoExecutionException;

import com.github.alme.graphql.generator.dto.Context;
import com.github.alme.graphql.generator.dto.GqlOperation;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModelException;

public class GqlWriter {

	private static final String OPERATION_INTERFACE_TEMPLATE = "OPERATION_INTERFACE";
	private static final String OPERATION_TEMPLATE = "OPERATION";
	private static final String UNNAMED_OPERATION = "Unnamed";
	private static final String INTERFACE_NAME_KEY = "interfaceName";
	private static final String CLASS_NAME_KEY = "className";
	private static final String ANNOTATION_KEY = "jsonProperty";
	private static final String FILE_EXTENSION = ".java";
	private static final String BASE_PACKAGE_KEY = "basePackage";
	private static final String TYPES_PACKAGE_KEY = "typesPackage";
	private static final String TYPES_SUBPACKAGE = "types";
	private static final String SUBPACKAGE_SEPARATOR = ".";
	private static final String LOG_CANNOT_CREATE = "Cannot create [%s] due to error.";
	private static final Configuration CFG = new Configuration(Configuration.VERSION_2_3_30);
	static {
		CFG.setClassLoaderForTemplateLoading(GqlWriter.class.getClassLoader(), "/templates/java");
		CFG.setDefaultEncoding("UTF-8");
		CFG.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		CFG.setLogTemplateExceptions(false);
		CFG.setWrapUncheckedExceptions(true);
		CFG.setFallbackOnNullLoopVariable(false);
	}

	public void write(Context ctx, String rootOutputDir, String basePackageName) throws MojoExecutionException {
		String typesPackageName = basePackageName + SUBPACKAGE_SEPARATOR + TYPES_SUBPACKAGE;
		Path basePackageFolder = Paths.get(rootOutputDir, basePackageName.replace(SUBPACKAGE_SEPARATOR, File.separator));
		Path typesPackageFolder = Paths.get(rootOutputDir, typesPackageName.replace(SUBPACKAGE_SEPARATOR, File.separator));
		try {
			Files.createDirectories(typesPackageFolder);
		} catch (IOException e) {
			throw new MojoExecutionException(String.format(LOG_CANNOT_CREATE, typesPackageFolder), e);
		}

		try {
			Map<String, String> m = new HashMap<>();
			m.put(BASE_PACKAGE_KEY, basePackageName);
			m.put(TYPES_PACKAGE_KEY, typesPackageName);
			m.put(ANNOTATION_KEY, ctx.getJsonPropertyAnnotation());
			CFG.setSharedVariables(m);
		} catch (TemplateModelException e) {
			throw new MojoExecutionException("Cannot set shared variables.", e);
		}
		dumpTypeClasses(ctx, typesPackageFolder);
		dumpOperationInterfaces(ctx, basePackageFolder);
		dumpOperationClasses(ctx, basePackageFolder);
		CFG.clearSharedVariables();
	}

	private void dumpTypeClasses(Context ctx, Path typesPackageFolder) {
		ctx.getStructures().forEach((category, structures) ->
			structures.forEach((name, i) -> {
				Path path = Paths.get(typesPackageFolder.toString(), name + FILE_EXTENSION);
				try (Writer writer = Files.newBufferedWriter(path)) {
					CFG.getTemplate(category.name()).process(i, writer);
				} catch (TemplateException | IOException e) {
					ctx.getLog().error(String.format(LOG_CANNOT_CREATE, name), e);
				}
			})
		);
	}

	private void dumpOperationInterfaces(Context ctx, Path basePackageFolder) {
		ctx.getOperations().values().stream()
		.map(GqlOperation::getOperation)
		.map(GqlWriter::capitalize)
		.collect(Collectors.toSet()).forEach((name) -> {
			try {
				CFG.setSharedVariable(INTERFACE_NAME_KEY, name);
			} catch (TemplateModelException e) {
				ctx.getLog().error(String.format(LOG_CANNOT_CREATE, name), e);
				return;
			}
			Path path = Paths.get(basePackageFolder.toString(), name + FILE_EXTENSION);
			try (Writer writer = Files.newBufferedWriter(path)) {
				CFG.getTemplate(OPERATION_INTERFACE_TEMPLATE).process(null, writer);
			} catch (TemplateException | IOException e) {
				ctx.getLog().error(String.format(LOG_CANNOT_CREATE, name), e);
			}
		});
	}

	private void dumpOperationClasses(Context ctx, Path basePackageFolder) {
		ctx.getOperations().forEach((name, operation) -> {
			String interfaceName = capitalize(operation.getOperation());
			name = (name == null ? UNNAMED_OPERATION : capitalize(name)) + interfaceName;
			try {
				CFG.setSharedVariable(CLASS_NAME_KEY, name);
				CFG.setSharedVariable(INTERFACE_NAME_KEY, interfaceName);
			} catch (TemplateModelException e) {
				ctx.getLog().error(String.format(LOG_CANNOT_CREATE, name), e);
				return;
			}
			Path path = Paths.get(basePackageFolder.toString(), name + FILE_EXTENSION);
			try (Writer writer = Files.newBufferedWriter(path)) {
				CFG.getTemplate(OPERATION_TEMPLATE).process(operation, writer);
			} catch (TemplateException | IOException e) {
				ctx.getLog().error(String.format(LOG_CANNOT_CREATE, name), e);
			}
		});
	}

	private static String capitalize(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

}
