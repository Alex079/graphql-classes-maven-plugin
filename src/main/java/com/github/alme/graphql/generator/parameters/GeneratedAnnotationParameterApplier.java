package com.github.alme.graphql.generator.parameters;

import java.time.Instant;

import com.github.alme.graphql.generator.GeneratorMojo;
import com.github.alme.graphql.generator.dto.GqlConfiguration;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GeneratedAnnotationParameterApplier implements ParameterApplier {

	private static final String ANNOTATION = """
		@javax.annotation.processing.Generated(value = "%s", date = "%s")
		""".formatted(GeneratorMojo.class.getName(), Instant.now());

	@Override
	public void apply(GqlConfiguration.GqlConfigurationBuilder builder) {
		builder.generatedAnnotation(ANNOTATION);
	}

}
