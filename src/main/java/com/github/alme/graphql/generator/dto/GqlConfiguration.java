package com.github.alme.graphql.generator.dto;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class GqlConfiguration {

	String jsonPropertyAnnotation;
	boolean useChainedAccessors;
	String basePackageName;
	String typesPackageName;
	Path basePackagePath;
	Path typesPackagePath;
	@Singular Set<String> importPackages;
	@Singular Map<String, String> scalars;

}
