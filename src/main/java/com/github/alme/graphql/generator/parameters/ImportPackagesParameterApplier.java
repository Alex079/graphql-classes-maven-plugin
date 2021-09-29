package com.github.alme.graphql.generator.parameters;

import java.util.Set;

import com.github.alme.graphql.generator.dto.GqlConfiguration.GqlConfigurationBuilder;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ImportPackagesParameterApplier implements ParameterApplier {

	private final Set<String> importPackages;

	@Override
	public void apply(GqlConfigurationBuilder builder) {
		builder
			.importPackage("java.util")
			.importPackages(importPackages);
	}

}
