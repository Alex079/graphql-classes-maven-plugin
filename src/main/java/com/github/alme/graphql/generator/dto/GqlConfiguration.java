package com.github.alme.graphql.generator.dto;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.github.alme.graphql.generator.parameters.ParameterApplier;

import org.apache.maven.plugin.MojoExecutionException;

import graphql.parser.ParserOptions;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class GqlConfiguration {

	Collection<Path> sourceFiles;
	@Singular Set<String> importPackages;
	@Singular Map<String, String> scalars;
	@Singular Map<String, String> aliases;
	boolean generateDtoMethodChaining;
	boolean generateDtoBuilder;
	boolean generateDtoSetters;
	boolean generateDtoConstructor;
	boolean generateDefinedOperations;
	boolean generateDynamicOperations;
	boolean generateSchemaInputTypes;
	boolean generateSchemaOtherTypes;
	String jsonPropertyAnnotation;
	String jsonPropertyPrefix;
	String jsonPropertySuffix;
	String generatedAnnotation;
	String schemaTypesPackageName;
	String operationsPackageName;
	String definedOperationsPackageName;
	String dynamicOperationsPackageName;
	Path outputRoot;
	ParserOptions parserOptions;

	public static class GqlConfigurationBuilder {
		public GqlConfigurationBuilder accept(ParameterApplier parameterApplier) throws MojoExecutionException {
			parameterApplier.apply(this);
			return this;
		}
	}

	public enum GeneratedOutputType {
		SCHEMA_TYPES,
		DEFINED_OPERATIONS,
		DYNAMIC_OPERATIONS,
	}

	public enum DataObjectEnhancementType {
		METHOD_CHAINING,
		BUILDER,
		VALUE,
	}

}
