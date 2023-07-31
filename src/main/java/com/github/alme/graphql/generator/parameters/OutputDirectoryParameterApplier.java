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
	private static final String DEFINED_SUBPACKAGE = ".defined";
	private static final String DYNAMIC_SUBPACKAGE = ".dynamic";

	@Override
	public void apply(GqlConfigurationBuilder builder) {
		builder
			.outputRoot(getOutputDirectory(outputDirectory))
			.operationsPackageName(packageName)
			.schemaTypesPackageName(packageName + TYPES_SUBPACKAGE)
			.definedOperationsPackageName(packageName + DEFINED_SUBPACKAGE)
			.dynamicOperationsPackageName(packageName + DYNAMIC_SUBPACKAGE);
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
