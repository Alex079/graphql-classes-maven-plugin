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
	private static final String RESULTS_SUBPACKAGE = ".results";
	private static final String SELECTORS_SUBPACKAGE = ".selectors";
	private static final String SUBPACKAGE_SEPARATOR = ".";

	@Override
	public void apply(GqlConfigurationBuilder builder) {
		String schemaTypesPackageName = packageName + TYPES_SUBPACKAGE;
		String operationsPackageName = packageName;
		String resultsPackageName = packageName + RESULTS_SUBPACKAGE;
		String selectorsPackageName = packageName + SELECTORS_SUBPACKAGE;
		Path outputRoot = getOutputDirectory(outputDirectory);
		Path schemaTypesPackagePath = outputRoot.resolve(schemaTypesPackageName.replace(SUBPACKAGE_SEPARATOR, File.separator));
		Path operationsPackagePath = outputRoot.resolve(operationsPackageName.replace(SUBPACKAGE_SEPARATOR, File.separator));
		Path resultsPackagePath = outputRoot.resolve(resultsPackageName.replace(SUBPACKAGE_SEPARATOR, File.separator));
		Path selectorsPackagePath = outputRoot.resolve(selectorsPackageName.replace(SUBPACKAGE_SEPARATOR, File.separator));
		builder
			.outputRoot(outputRoot)
			.schemaTypesPackageName(schemaTypesPackageName)
			.operationsPackageName(operationsPackageName)
			.resultsPackageName(resultsPackageName)
			.selectorsPackageName(selectorsPackageName)
			.schemaTypesPackagePath(schemaTypesPackagePath)
			.operationsPackagePath(operationsPackagePath)
			.resultsPackagePath(resultsPackagePath)
			.selectorsPackagePath(selectorsPackagePath);
	}

	private Path getOutputDirectory(File outputDirectory) {
		return outputDirectory == null
			? Paths
				.get(project.getBasedir() == null ? "" : project.getBuild().getDirectory())
				.resolve("generated-sources")
				.resolve("graphql-classes")
				.resolve("java")
			: outputDirectory.toPath();
	}

}
