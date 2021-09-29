package com.github.alme.graphql.generator.parameters;

import java.util.Set;

import com.github.alme.graphql.generator.dto.GqlConfiguration.GqlConfigurationBuilder;
import com.github.alme.graphql.generator.dto.GqlConfiguration.OperationWrapperType;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OperationWrapperTypeParameterApplier implements ParameterApplier {

	private final Set<OperationWrapperType> operationWrapperTypes;

	@Override
	public void apply(GqlConfigurationBuilder builder) {
		if (operationWrapperTypes != null) {
			builder
				.generateDefinedOperations(operationWrapperTypes.contains(OperationWrapperType.DEFINED))
				.generateDynamicOperations(operationWrapperTypes.contains(OperationWrapperType.DYNAMIC));
		}
	}

}
