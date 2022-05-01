package com.github.alme.graphql.generator;

import static java.lang.String.format;

import java.io.File;
import java.util.Map;
import java.util.Set;

import com.github.alme.graphql.generator.dto.GqlConfiguration;
import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.io.GqlReader;
import com.github.alme.graphql.generator.io.GqlWriter;
import com.github.alme.graphql.generator.io.ReaderFactory;
import com.github.alme.graphql.generator.io.WriterFactory;
import com.github.alme.graphql.generator.parameters.DataObjectEnhancementTypeParameterApplier;
import com.github.alme.graphql.generator.parameters.GeneratedAnnotationParameterApplier;
import com.github.alme.graphql.generator.parameters.OutputTypesParameterApplier;
import com.github.alme.graphql.generator.parameters.OutputDirectoryParameterApplier;
import com.github.alme.graphql.generator.parameters.ParserOptionsParameterApplier;
import com.github.alme.graphql.generator.parameters.ScalarMapParameterApplier;
import com.github.alme.graphql.generator.parameters.SourceParameterApplier;

import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

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
	 * The type of data object enhancement.
	 * Can be empty or take one of the following values:
	 * METHOD_CHAINING (data object setters will return 'this' instead of 'void'),
	 * BUILDER (data objects will use builder pattern)
	 */
	@Parameter(property = "gql.dataObjectEnhancement")
	private GqlConfiguration.DataObjectEnhancementType dataObjectEnhancement;

	/**
	 * A set of output types
	 * Can be empty or take values from the following list:
	 * SCHEMA_TYPES (all the types defined in GraphQL schema files),
	 * DEFINED_OPERATIONS (all the operations defined in input files),
	 * DYNAMIC_OPERATIONS (one operation per schema entry allowing to construct operations at runtime)
	 */
	@Parameter(property = "gql.generatedOutputTypes")
	private Set<GqlConfiguration.GeneratedOutputType> generatedOutputTypes;

	@Parameter(property = "gql.parserMaxTokens")
	private Integer parserMaxTokens;

	/**
	 * A maven project to add newly generated sources into
	 */
	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	@Override
	public void execute() throws MojoExecutionException {
		GqlConfiguration configuration = GqlConfiguration.builder()
			.accept(new SourceParameterApplier(project, source, sourceDirectoryAlternative, sourceIncludesAlternative, sourceExcludesAlternative))
			.accept(new OutputDirectoryParameterApplier(project, outputDirectory, packageName))
			.accept(new ScalarMapParameterApplier(scalarMap, scalarMapAlternative))
			.accept(new DataObjectEnhancementTypeParameterApplier(dataObjectEnhancement))
			.accept(new OutputTypesParameterApplier(generatedOutputTypes))
			.accept(new GeneratedAnnotationParameterApplier(generatedAnnotationVersion))
			.accept(new ParserOptionsParameterApplier(parserMaxTokens))
			.importPackages(importPackages)
			.jsonPropertyAnnotation(jsonPropertyAnnotation)
			.build();

		getLog().info(format("Current configuration: %s.", configuration));

		GqlContext context = new GqlContext(getLog(), configuration.getScalars());
		ReaderFactory readerFactory = new ReaderFactory(configuration.getSourceFiles(), getLog());
		WriterFactory writerFactory = new WriterFactory();

		new GqlReader(readerFactory).read(context, configuration);
		getLog().debug(format("Current context: %s.", context));

		new GqlWriter(writerFactory).write(context, configuration);
		getLog().info("Generation is done.");

		String outputRoot = configuration.getOutputRoot().toString();
		project.addCompileSourceRoot(outputRoot);
		project.addTestCompileSourceRoot(outputRoot);
	}

}
