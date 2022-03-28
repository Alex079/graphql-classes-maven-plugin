package com.github.alme.graphql.generator.dto;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.github.alme.graphql.generator.parameters.ParameterApplier;

import org.apache.maven.plugin.MojoExecutionException;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class GqlConfiguration {

	Collection<Path> sourceFiles;
	@Singular Set<String> importPackages;
	@Singular Map<String, String> scalars;
	boolean generateMethodChaining;
	boolean generateDtoBuilder;
	boolean generateDefinedOperations;
	boolean generateDynamicOperations;
	String jsonPropertyAnnotation;
	String generatedAnnotation;
	String schemaTypesPackageName;
	String operationsPackageName;
	String dynamicOperationsPackageName;
	Path outputRoot;
	Path schemaTypesPackagePath;
	Path operationsPackagePath;
	Path dynamicOperationsPackagePath;

	public static class GqlConfigurationBuilder {
		public GqlConfigurationBuilder accept(ParameterApplier parameterApplier) throws MojoExecutionException {
			parameterApplier.apply(this);
			return this;
		}
	}

	public enum OperationWrapperType {
		DEFINED, DYNAMIC
	}

	public enum DataObjectEnhancementType {
		METHOD_CHAINING, BUILDER
	}

}
