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
	private static final String SUBPACKAGE_SEPARATOR = ".";

	@Override
	public void apply(GqlConfigurationBuilder builder) {
		String schemaTypesPackageName = packageName + TYPES_SUBPACKAGE;
		String operationsPackageName = packageName;
		Path outputRoot = getOutputDirectory(outputDirectory);
		Path schemaTypesPackagePath = outputRoot.resolve(schemaTypesPackageName.replace(SUBPACKAGE_SEPARATOR, File.separator));
		Path operationsPackagePath = outputRoot.resolve(operationsPackageName.replace(SUBPACKAGE_SEPARATOR, File.separator));
		builder
			.outputRoot(outputRoot)
			.schemaTypesPackageName(schemaTypesPackageName)
			.operationsPackageName(operationsPackageName)
			.schemaTypesPackagePath(schemaTypesPackagePath)
			.operationsPackagePath(operationsPackagePath);
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
