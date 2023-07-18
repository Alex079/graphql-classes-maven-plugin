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
import com.github.alme.graphql.generator.parameters.AliasMapParameterApplier;
import com.github.alme.graphql.generator.parameters.DataObjectEnhancementTypeParameterApplier;
import com.github.alme.graphql.generator.parameters.FieldTransformationApplier;
import com.github.alme.graphql.generator.parameters.GeneratedAnnotationParameterApplier;
import com.github.alme.graphql.generator.parameters.OutputDirectoryParameterApplier;
import com.github.alme.graphql.generator.parameters.OutputTypesParameterApplier;
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
	 * Set of source files including both schema files and operation files.
	 */
	@Parameter
	private FileSet source;

	/**
	 * Set of source files including both schema files and operation files.
	 */
	@Parameter
	private Set<FileSet> sources;

	/**
	 * <b>This parameter is used when running from command line and is ignored when POM configuration exists.</b><br/>
	 * Directory containing source files.<br/>
	 * <u>Default</u>: current directory
	 */
	@Parameter(property = "gql.sourceDirectory")
	private String sourceDirectoryAlternative;

	/**
	 * <b>This parameter is used when running from command line and is ignored when POM configuration exists.</b><br/>
	 * Set of patterns to include. See "source" parameter.
	 */
	@Parameter(property = "gql.sourceIncludes")
	private Set<String> sourceIncludesAlternative;

	/**
	 * <b>This parameter is used when running from command line and is ignored when POM configuration exists.</b><br/>
	 * Set of patterns to exclude. See "source" parameter.
	 */
	@Parameter(property = "gql.sourceExcludes")
	private Set<String> sourceExcludesAlternative;

	/**
	 * The root directory for generated files.<br/>
	 * <u>Default</u>: "${project.build.directory}/generated-sources/java"
	 */
	@Parameter(property = "gql.outputDirectory")
	private File outputDirectory;

	/**
	 * Name of the base package for generated classes.<br/>
	 * <u>Default</u>: "gql.generated"
	 */
	@Parameter(property = "gql.packageName", defaultValue = "gql.generated")
	private String packageName;

	/**
	 * Mapping of GraphQL scalars to known java classes.<br/>
	 * <u>Default</u>:<br/>Int -> Integer<br/>Float -> Double<br/>ID -> String
	 */
	@Parameter
	private Map<String, String> scalarMap;

	/**
	 * <b>This parameter is used when running from command line and is ignored when "scalarMap" configuration exists.</b><br/>
	 * Mapping of GraphQL scalars to Java classes formatted as a list of key=value pairs. See "scalarMap" parameter.<br/>
	 * <u>Default</u>: "Int=Integer,Float=Double,ID=String".
	 */
	@Parameter(property = "gql.scalarMap")
	private Set<String> scalarMapAlternative;

	/**
	 * Mapping of GraphQL field names to GraphQL field aliases.
	 * Can be used to avoid Java keyword collisions for dynamic operations only.
	 */
	@Parameter
	private Map<String, String> aliasMap;

	/**
	 * <b>This parameter is used when running from command line and is ignored when "aliasMap" configuration exists.</b><br/>
	 * Mapping of GraphQL field names to GraphQL field aliases formatted as a list of key=value pairs. See "aliasMap" parameter.
	 * Can be used to avoid Java keyword collisions for dynamic operations only.
	 */
	@Parameter(property = "gql.aliasMap")
	private Set<String> aliasMapAlternative;

	/**
	 * Set of packages to import into generated classes.
	 */
	@Parameter(property = "gql.importPackages")
	private Set<String> importPackages;

	/**
	 * Annotation to be used on generated private fields.
	 */
	@Parameter(property = "gql.jsonPropertyAnnotation")
	private String jsonPropertyAnnotation;

	/**
	 * Prefix to be added to generated private field names to avoid Java keywords collisions.
	 */
	@Parameter(property = "gql.privateFieldPrefix")
	private String privateFieldPrefix;

	/**
	 * Suffix to be added to generated private field names to avoid Java keywords collisions.<br/>
	 * <u>Default</u>: "__", when "jsonPropertyAnnotation" is set
	 */
	@Parameter(property = "gql.privateFieldSuffix")
	private String privateFieldSuffix;

	/**
	 * Version of "@Generated" annotation to use on generated classes (i.e. "1.8", "11", "15").
	 */
	@Parameter(property = "gql.generatedAnnotationVersion")
	private String generatedAnnotationVersion;

	/**
	 * The type of data object enhancement.
	 * Can be empty or take one of the following values:
	 * METHOD_CHAINING (data object setters will return 'this' instead of 'void'),
	 * BUILDER (data objects will use builder pattern),
	 * VALUE (data objects will use value pattern).
	 */
	@Parameter(property = "gql.dataObjectEnhancement")
	private GqlConfiguration.DataObjectEnhancementType dataObjectEnhancement;

	/**
	 * A set of output types.
	 * Can be empty or take one or many values from the following list:
	 * SCHEMA_TYPES (all the types defined in GraphQL schema files),
	 * DEFINED_OPERATIONS (all the operations defined in input files),
	 * DYNAMIC_OPERATIONS (one operation per schema entry allowing to construct operations at runtime).<br/>
	 * <u>Default</u>: DEFINED_OPERATIONS
	 */
	@Parameter(property = "gql.generatedOutputTypes")
	private Set<GqlConfiguration.GeneratedOutputType> generatedOutputTypes;

	/**
	 * Maximum number of tokens to process by the GraphQL engine.
	 * @see graphql.parser.ParserOptions#MAX_QUERY_TOKENS
	 */
	@Parameter(property = "gql.parserMaxTokens")
	private Integer parserMaxTokens;

	/**
	 * Maven project to add newly generated sources into.
	 */
	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	@Override
	public void execute() throws MojoExecutionException {
		GqlConfiguration configuration = GqlConfiguration.builder()
			.accept(new SourceParameterApplier(project, sources, source,
				sourceDirectoryAlternative, sourceIncludesAlternative, sourceExcludesAlternative))
			.accept(new OutputDirectoryParameterApplier(project, outputDirectory, packageName))
			.accept(new ScalarMapParameterApplier(scalarMap, scalarMapAlternative))
			.accept(new AliasMapParameterApplier(aliasMap, aliasMapAlternative))
			.accept(new DataObjectEnhancementTypeParameterApplier(dataObjectEnhancement))
			.accept(new OutputTypesParameterApplier(generatedOutputTypes))
			.accept(new GeneratedAnnotationParameterApplier(generatedAnnotationVersion))
			.accept(new ParserOptionsParameterApplier(parserMaxTokens))
			.accept(new FieldTransformationApplier(jsonPropertyAnnotation, privateFieldPrefix, privateFieldSuffix))
			.importPackages(importPackages)
			.build();

		getLog().info(format("Current configuration: %s.", configuration));

		GqlContext context = new GqlContext(getLog(), configuration.getScalars(), configuration.getAliases());
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
