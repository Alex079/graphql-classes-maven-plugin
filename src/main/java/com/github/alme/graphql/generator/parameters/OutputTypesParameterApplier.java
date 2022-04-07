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
		if (generatedOutputTypes != null) {
			boolean definedOperations = generatedOutputTypes.contains(GeneratedOutputType.DEFINED_OPERATIONS);
			boolean dynamicOperations = generatedOutputTypes.contains(GeneratedOutputType.DYNAMIC_OPERATIONS);
			boolean schemaTypes = generatedOutputTypes.contains(GeneratedOutputType.SCHEMA_TYPES);
			builder
				.generateDefinedOperations(definedOperations)
				.generateDynamicOperations(dynamicOperations)
				.generateSchemaInputTypes(schemaTypes || definedOperations || dynamicOperations)
				.generateSchemaOtherTypes(schemaTypes);
		}
	}

}
