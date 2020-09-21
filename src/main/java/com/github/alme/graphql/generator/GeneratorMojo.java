package com.github.alme.graphql.generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.alme.graphql.generator.dto.Context;
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
	 * Set "directory" value of source file set. See "source" parameter.
	 */
	@Parameter(property = "gql.sourceDirectory")
	private File sourceDirectoryAlternative;

	/**
	 * <b>This parameter is used when running from command line and is ignored when POM configuration exists.</b>
	 * Set "includes" value of source file set. See "source" parameter.
	 */
	@Parameter(property = "gql.sourceIncludes")
	private List<String> sourceIncludesAlternative;

	/**
	 * <b>This parameter is used when running from command line and is ignored when POM configuration exists.</b>
	 * Set "excludes" value of source file set. See "source" parameter.
	 */
	@Parameter(property = "gql.sourceExcludes")
	private List<String> sourceExcludesAlternative;

	/**
	 * A root directory to create files in
	 */
	@Parameter(property = "gql.outputDirectory")
	private File outputDirectory;

	/**
	 * A name of the package to create files in
	 */
	@Parameter(property = "gql.packageName", defaultValue = "gql.generated")
	private String packageName;

	/**
	 * A mapping of GraphQL scalars to known java classes
	 */
	@Parameter
	private Map<String, String> scalarMap;

	/**
	 * <b>This parameter is used when running from command line and is ignored when POM configuration exists.</b>
	 * See "scalarMap" parameter.
	 */
	@Parameter(property = "gql.scalarMap")
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
	 * A flag indicating whether generated setters should return <b>void</b> (when set to false) or <b>this</b> (when set to true)
	 */
	@Parameter(property = "gql.useChainedAccessors", defaultValue = "false")
	private boolean useChainedAccessors;

	/**
	 * A maven project to add newly generated sources into
	 */
	@Parameter(defaultValue="${project}", readonly = true)
	private MavenProject project;

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

	private List<String> getSourceIncludes() {
		if (source == null) {
			return sourceIncludesAlternative == null ? Collections.emptyList() : sourceIncludesAlternative;
		}
		return source.getIncludes() == null ? Collections.emptyList() : source.getIncludes();
	}

	private List<String> getSourceExcludes() {
		if (source == null) {
			return sourceExcludesAlternative == null ? Collections.emptyList() : sourceExcludesAlternative;
		}
		return source.getExcludes() == null ? Collections.emptyList() : source.getExcludes();
	}

	@Override
	public void execute() throws MojoExecutionException {
		File directory = getSourceDirectory();
		List<String> includes = getSourceIncludes();
		List<String> excludes = getSourceExcludes();
		getLog().info(String.format("Source: {directory=[%s], includes=%s, excludes=%s}.", directory, includes, excludes));
		List<File> sourceFiles;
		try {
			sourceFiles = FileUtils.getFiles(directory, String.join(",", includes), String.join(",", excludes));
		} catch (IOException e) {
			throw new MojoExecutionException("Could not collect source files.", e);
		}
		if (sourceFiles.isEmpty()) {
			throw new MojoExecutionException("Could not find any source files.");
		}
		getLog().info(String.format("Source files: %s.", sourceFiles.toString()));

		Context ctx = new Context(getLog());

		if (scalarMap == null) {
			ctx.getScalarMap().putAll(scalarMapAlternative.stream()
				.filter(Objects::nonNull)
				.map(item -> item.split("=", 2))
				.filter(item -> item.length == 2)
				.collect(Collectors.toMap(item -> item[0], item -> item[1])));
		}
		else {
			ctx.getScalarMap().putAll(scalarMap);
		}
		getLog().info(String.format("Scalar types mapping rules: %s.", ctx.getScalarMap()));

		if (importPackages != null) {
			ctx.getImportPackages().addAll(importPackages);
		}
		getLog().info(String.format("Packages to import: %s.", ctx.getImportPackages()));

		if (jsonPropertyAnnotation != null) {
			ctx.setJsonPropertyAnnotation(jsonPropertyAnnotation);
			getLog().info(String.format("Annotating serializable properties with [%s].", jsonPropertyAnnotation));
		}

		ctx.setUseChainedAccessors(useChainedAccessors);
		getLog().info(String.format("Chained accessors: %b.", useChainedAccessors));

		String outputRoot;
		if (outputDirectory == null) {
			outputRoot = Paths
				.get(project.getBasedir() == null ? "" : project.getBuild().getDirectory())
				.resolve("generated-sources")
				.resolve("java")
				.toString();
		}
		else {
			outputRoot = outputDirectory.getPath();
		}
		getLog().info(String.format("Output directory: [%s].", outputRoot));

		getLog().info(String.format("Output package name: [%s].", packageName));

		new GqlReader().read(ctx, sourceFiles);
		getLog().debug(String.format("Current context: %s.", ctx));
		new GqlWriter().write(ctx, outputRoot, packageName);
		getLog().info("Generation is done.");
		project.addCompileSourceRoot(outputRoot);
	}

}
