package com.github.alme.graphql.generator.parameters;

import java.util.Set;

import com.github.alme.graphql.generator.dto.GqlConfiguration.GqlConfigurationBuilder;
import com.github.alme.graphql.generator.dto.GqlConfiguration.GeneratedOutputType;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OutputTypesParameterApplier implements ParameterApplier {

	private final Set<GeneratedOutputType> generatedOutputTypes;

	@Override
	public void apply(GqlConfigurationBuilder builder) {
		boolean needSchemaTypes = false;
		boolean needDefinedOperations = true;
		boolean needDynamicOperations = false;
		if (generatedOutputTypes != null && !generatedOutputTypes.isEmpty()) {
			needSchemaTypes = generatedOutputTypes.contains(GeneratedOutputType.SCHEMA_TYPES);
			needDefinedOperations = generatedOutputTypes.contains(GeneratedOutputType.DEFINED_OPERATIONS);
			needDynamicOperations = generatedOutputTypes.contains(GeneratedOutputType.DYNAMIC_OPERATIONS);
		}
		builder
			.generateDefinedOperations(needDefinedOperations)
			.generateDynamicOperations(needDynamicOperations)
			.generateSchemaInputTypes(needSchemaTypes || needDefinedOperations || needDynamicOperations)
			.generateSchemaOtherTypes(needSchemaTypes);
	}

}
