package com.github.alme.graphql.generator.parameters;

import java.time.Instant;

import com.github.alme.graphql.generator.GeneratorMojo;
import com.github.alme.graphql.generator.dto.GqlConfiguration;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GeneratedAnnotationParameterApplier implements ParameterApplier {

	private final String generatedAnnotationVersion;

	private static final String TEMPLATE = "@javax.annotation%s.Generated(value = \"%s\", date = \"%s\")";
	private static final String PROCESSING = ".processing";
	private static final String JAVA8 = "1.8";

	@Override
	public void apply(GqlConfiguration.GqlConfigurationBuilder builder) {
		if (generatedAnnotationVersion != null) {
			builder.generatedAnnotation(TEMPLATE.formatted(
				JAVA8.equals(generatedAnnotationVersion) ? "" : PROCESSING,
				GeneratorMojo.class.getName(),
				Instant.now()
			));
		}
	}

}
