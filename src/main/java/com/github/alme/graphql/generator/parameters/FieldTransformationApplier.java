package com.github.alme.graphql.generator.parameters;

import com.github.alme.graphql.generator.dto.GqlConfiguration;

import org.apache.maven.plugin.MojoExecutionException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FieldTransformationApplier implements ParameterApplier {

	private final String annotation;
	private final String prefix;
	private final String suffix;

	private static final String DEFAULT_SUFFIX = "__";

	@Override
	public void apply(GqlConfiguration.GqlConfigurationBuilder builder) throws MojoExecutionException {
		builder.jsonPropertyAnnotation(annotation);
		if (annotation != null && !annotation.isEmpty() && (prefix == null || prefix.isEmpty()) && (suffix == null || suffix.isEmpty())) {
			builder.jsonPropertySuffix(DEFAULT_SUFFIX);
		}
		else {
			builder.jsonPropertyPrefix(prefix).jsonPropertySuffix(suffix);
		}
	}

}
