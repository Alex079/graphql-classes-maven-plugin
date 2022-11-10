package com.github.alme.graphql.generator.parameters;

import com.github.alme.graphql.generator.dto.GqlConfiguration;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DataObjectEnhancementTypeParameterApplier implements ParameterApplier {

	private final GqlConfiguration.DataObjectEnhancementType dataObjectEnhancementType;

	@Override
	public void apply(GqlConfiguration.GqlConfigurationBuilder builder) {
		boolean needPlain = dataObjectEnhancementType == null;
		boolean needBuilder = dataObjectEnhancementType == GqlConfiguration.DataObjectEnhancementType.BUILDER;
		boolean needChaining = dataObjectEnhancementType == GqlConfiguration.DataObjectEnhancementType.METHOD_CHAINING;
		boolean needValue = dataObjectEnhancementType == GqlConfiguration.DataObjectEnhancementType.VALUE;
		builder
			.generateDtoMethodChaining(needChaining || needBuilder)
			.generateDtoBuilder(needBuilder)
			.generateDtoSetters(needPlain || needChaining)
			.generateDtoConstructor(needValue || needBuilder);
	}

}
