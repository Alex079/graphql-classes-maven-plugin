package com.github.alme.graphql.generator.parameters;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.alme.graphql.generator.dto.GqlConfiguration.GqlConfigurationBuilder;

import org.apache.maven.project.MavenProject;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OutputParameterApplier implements ParameterApplier {

	private final MavenProject project;
	private final File outputDirectory;
	private final String packageName;

	private static final String TYPES_SUBPACKAGE = ".types";
	private static final String SUBPACKAGE_SEPARATOR = ".";

	@Override
	public void apply(GqlConfigurationBuilder builder) {
		String typesPackageName = packageName + TYPES_SUBPACKAGE;
		Path outputRoot = getOutputDirectory(outputDirectory);
		builder
			.outputRoot(outputRoot)
			.basePackageName(packageName)
			.typesPackageName(typesPackageName)
			.basePackagePath(outputRoot.resolve(packageName.replace(SUBPACKAGE_SEPARATOR, File.separator)))
			.typesPackagePath(outputRoot.resolve(typesPackageName.replace(SUBPACKAGE_SEPARATOR, File.separator)));
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
