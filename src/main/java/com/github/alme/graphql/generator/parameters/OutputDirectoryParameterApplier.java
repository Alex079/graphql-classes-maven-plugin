package com.github.alme.graphql.generator.parameters;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.alme.graphql.generator.dto.GqlConfiguration.GqlConfigurationBuilder;

import org.apache.maven.project.MavenProject;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OutputDirectoryParameterApplier implements ParameterApplier {

	private final MavenProject project;
	private final File outputDirectory;
	private final String packageName;

	private static final String TYPES_SUBPACKAGE = ".types";
	private static final String OPERATIONS_SUBPACKAGE = ".operations";
	private static final String DYNAMIC_OPERATIONS_SUBPACKAGE = OPERATIONS_SUBPACKAGE + ".dynamic";
	private static final String SUBPACKAGE_SEPARATOR = ".";

	@Override
	public void apply(GqlConfigurationBuilder builder) {
		String schemaTypesPackageName = packageName + TYPES_SUBPACKAGE;
		String operationsPackageName = packageName + OPERATIONS_SUBPACKAGE;
		String dynamicOperationsPackageName = packageName + DYNAMIC_OPERATIONS_SUBPACKAGE;
		Path outputRoot = getOutputDirectory(outputDirectory);
		Path schemaTypesPackagePath = outputRoot.resolve(schemaTypesPackageName.replace(SUBPACKAGE_SEPARATOR, File.separator));
		Path operationsPackagePath = outputRoot.resolve(operationsPackageName.replace(SUBPACKAGE_SEPARATOR, File.separator));
		Path dynamicOperationsPackagePath = outputRoot.resolve(dynamicOperationsPackageName.replace(SUBPACKAGE_SEPARATOR, File.separator));
		builder
			.outputRoot(outputRoot)
			.schemaTypesPackageName(schemaTypesPackageName)
			.operationsPackageName(operationsPackageName)
			.dynamicOperationsPackageName(dynamicOperationsPackageName)
			.schemaTypesPackagePath(schemaTypesPackagePath)
			.operationsPackagePath(operationsPackagePath)
			.dynamicOperationsPackagePath(dynamicOperationsPackagePath);
	}

	private Path getOutputDirectory(File outputDirectory) {
		return outputDirectory == null
			? Paths
				.get(project.getBasedir() == null ? "" : project.getBuild().getDirectory())
				.resolve("generated-sources")
				.resolve("java")
			: outputDirectory.toPath();
	}

}
