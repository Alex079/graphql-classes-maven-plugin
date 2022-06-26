package com.github.alme.graphql.generator.parameters;

import com.github.alme.graphql.generator.dto.GqlConfiguration;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DataObjectEnhancementTypeParameterApplier implements ParameterApplier {

	private final GqlConfiguration.DataObjectEnhancementType dataObjectEnhancementType;

	@Override
	public void apply(GqlConfiguration.GqlConfigurationBuilder builder) {
		boolean needBuilder = dataObjectEnhancementType == GqlConfiguration.DataObjectEnhancementType.BUILDER;
		boolean needChaining = dataObjectEnhancementType == GqlConfiguration.DataObjectEnhancementType.METHOD_CHAINING;
		builder
			.generateMethodChaining(needChaining || needBuilder)
			.generateDtoBuilder(needBuilder);
	}

}
