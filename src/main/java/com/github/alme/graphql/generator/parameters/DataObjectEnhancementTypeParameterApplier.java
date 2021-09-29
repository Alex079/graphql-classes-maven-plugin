package com.github.alme.graphql.generator.parameters;

import com.github.alme.graphql.generator.dto.GqlConfiguration;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DataObjectEnhancementTypeParameterApplier implements ParameterApplier {

	private final GqlConfiguration.DataObjectEnhancementType dataObjectEnhancementType;

	@Override
	public void apply(GqlConfiguration.GqlConfigurationBuilder builder) {
		if (dataObjectEnhancementType != null) {
			builder
				.generateMethodChaining(
					dataObjectEnhancementType == GqlConfiguration.DataObjectEnhancementType.METHOD_CHAINING ||
					dataObjectEnhancementType == GqlConfiguration.DataObjectEnhancementType.BUILDER)
				.generateDtoBuilder(dataObjectEnhancementType == GqlConfiguration.DataObjectEnhancementType.BUILDER);
		}
	}

}
