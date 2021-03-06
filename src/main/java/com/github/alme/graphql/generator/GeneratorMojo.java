package com.github.alme.graphql.generator;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.alme.graphql.generator.dto.GqlConfiguration;
import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.io.GqlReader;
import com.github.alme.graphql.generator.io.GqlWriter;

import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true, requiresProject = false)
public class GeneratorMojo extends AbstractMojo {

	/**
	 * A set of source files including both schema files and operation files.
	 */
	@Parameter
	private FileSet source;

	/**
	 * <b>This parameter is used when running from command line and is ignored when POM configuration exists.</b>
	 * A directory containing source files. See "source" parameter.
	 */
	@Parameter(property = "gql.sourceDirectory", readonly = true)
	private File sourceDirectoryAlternative;

	/**
	 * <b>This parameter is used when running from command line and is ignored when POM configuration exists.</b>
	 * A set of patterns to include. See "source" parameter.
	 */
	@Parameter(property = "gql.sourceIncludes", readonly = true)
	private Set<String> sourceIncludesAlternative;

	/**
	 * <b>This parameter is used when running from command line and is ignored when POM configuration exists.</b>
	 * A set of patterns to exclude. See "source" parameter.
	 */
	@Parameter(property = "gql.sourceExcludes", readonly = true)
	private Set<String> sourceExcludesAlternative;

	/**
	 * A root directory for generated files
	 */
	@Parameter(property = "gql.outputDirectory")
	private File outputDirectory;

	/**
	 * A name of base package for generated classes
	 */
	@Parameter(property = "gql.packageName", defaultValue = "gql.generated")
	private String packageName;

	/**
	 * A mapping of GraphQL scalars to known java classes
	 */
	@Parameter
	private Map<String, String> scalarMap;

	/**
	 * <b>This parameter is used when running from command line and is ignored when "scalarMap" configuration exists.</b>
	 * A mapping of GraphQL scalars to known java classes formatted as a list of key=value pairs. See "scalarMap" parameter.
	 */
	@Parameter(property = "gql.scalarMap", readonly = true)
	private Set<String> scalarMapAlternative;

	/**
	 * A set of packages to import into generated classes
	 */
	@Parameter(property = "gql.importPackages")
	private Set<String> importPackages;

	/**
	 * An annotation to be used on generated fields to avoid java keywords collisions
	 */
	@Parameter(property = "gql.jsonPropertyAnnotation")
	private String jsonPropertyAnnotation;

	/**
	 * A version of '@Generated' annotation to use on generated classes
	 */
	@Parameter(property = "gql.generatedAnnotationVersion")
	private String generatedAnnotationVersion;

	/**
	 * A flag indicating whether generated setters should return <b>void</b> (when set to false) or <b>this</b> (when set to true)
	 */
	@Parameter(property = "gql.useChainedAccessors", defaultValue = "false")
	private boolean useChainedAccessors;

	/**
	 * A maven project to add newly generated sources into
	 */
	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	private static final String TYPES_SUBPACKAGE = ".types";
	private static final String SUBPACKAGE_SEPARATOR = ".";
	private static final String LIST_SEPARATOR = ",";
	private static final String KEY_VALUE_SEPARATOR = "=";

	private File getSourceDirectory() {
		if (source == null && sourceDirectoryAlternative != null) {
			return sourceDirectoryAlternative;
		}
		if (source != null && source.getDirectory() != null) {
			return new File(source.getDirectory());
		}
		if (project.getBasedir() != null) {
			return project.getBasedir();
		}
		return new File("").getAbsoluteFile();
	}

	private String join(Iterable<? extends CharSequence> i) {
		return i == null ? "" : String.join(LIST_SEPARATOR, i);
	}

	private String getSourceIncludes() {
		return join(source == null ? sourceIncludesAlternative : source.getIncludes());
	}

	private String getSourceExcludes() {
		return join(source == null ? sourceExcludesAlternative : source.getExcludes());
	}

	private Map<String, String> getScalarMap() {
		return scalarMap == null
			? scalarMapAlternative.stream()
				.filter(Objects::nonNull)
				.map(item -> item.split(KEY_VALUE_SEPARATOR, 2))
				.filter(item -> item.length == 2)
				.collect(Collectors.toMap(item -> item[0], item -> item[1]))
			: scalarMap;
	}

	private String getOutputDirectory() {
		return outputDirectory == null
			? Paths
				.get(project.getBasedir() == null ? "" : project.getBuild().getDirectory())
				.resolve("generated-sources")
				.resolve("java")
				.toString()
			: outputDirectory.getPath();
	}

	private List<File> getSourceFiles(File directory, String includes, String excludes) throws MojoExecutionException {
		List<File> sourceFiles;
		try {
			sourceFiles = FileUtils.getFiles(directory, includes, excludes);
		} catch (IOException e) {
			throw new MojoExecutionException("Could not collect source files.", e);
		}
		if (sourceFiles.isEmpty()) {
			throw new MojoExecutionException("Could not find any source files.");
		}
		return sourceFiles;
	}

	@Override
	public void execute() throws MojoExecutionException {
		File directory = getSourceDirectory();
		String includes = getSourceIncludes();
		String excludes = getSourceExcludes();
		getLog().info(format("Source: {directory=[%s], includes=[%s], excludes=[%s]}.", directory, includes, excludes));

		List<File> sourceFiles = getSourceFiles(directory, includes, excludes);
		getLog().info(format("Source files: %s.", sourceFiles));

		String outputRoot = getOutputDirectory();
		getLog().info(format("Output directory: [%s].", outputRoot));

		String typesPackageName = packageName + TYPES_SUBPACKAGE;
		GqlConfiguration configuration = GqlConfiguration.builder()
			.importPackage("java.util")
			.scalar("Int", "Integer")
			.scalar("Float", "Double")
			.scalar("ID", "String")
			.basePackageName(packageName)
			.typesPackageName(typesPackageName)
			.basePackagePath(Paths.get(outputRoot, packageName.replace(SUBPACKAGE_SEPARATOR, File.separator)))
			.typesPackagePath(Paths.get(outputRoot, typesPackageName.replace(SUBPACKAGE_SEPARATOR, File.separator)))
			.importPackages(importPackages)
			.scalars(getScalarMap())
			.jsonPropertyAnnotation(jsonPropertyAnnotation)
			.useChainedAccessors(useChainedAccessors)
			.generatedAnnotationVersion(generatedAnnotationVersion)
			.build();
		getLog().info(format("Current configuration: %s.", configuration));

		GqlContext context = new GqlContext(getLog(), configuration.getScalars());
		new GqlReader().read(context, sourceFiles);
		getLog().debug(format("Current context: %s.", context));
		new GqlWriter().write(context, configuration);
		getLog().info("Generation is done.");

		project.addCompileSourceRoot(outputRoot);
	}

}
