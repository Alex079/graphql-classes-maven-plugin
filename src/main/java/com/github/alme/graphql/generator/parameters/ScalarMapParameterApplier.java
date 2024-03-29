package com.github.alme.graphql.generator.parameters;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.github.alme.graphql.generator.dto.GqlConfiguration.GqlConfigurationBuilder;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ScalarMapParameterApplier implements ParameterApplier {

	private final Map<String, String> scalarMap;
	private final Collection<String> scalarMapAlternative;

	private static final String KEY_VALUE_SEPARATOR = "=";

	private static final Map<String, String> DEFAULT_SCALARS = Map.of(
		"Int", "Integer",
		"Float", "Double",
		"ID", "String"
	);

	@Override
	public void apply(GqlConfigurationBuilder builder) {
		builder.scalars(DEFAULT_SCALARS);
		if (scalarMap != null) {
			scalarMap.entrySet().stream()
				.filter(item ->
					item.getKey() != null && item.getValue() != null &&
					!item.getKey().isBlank() && !item.getValue().isBlank())
				.forEach(item -> builder.scalar(item.getKey().trim(), item.getValue().trim()));
		}
		else if (scalarMapAlternative != null) {
			scalarMapAlternative.stream()
				.filter(Objects::nonNull)
				.map(item -> item.split(KEY_VALUE_SEPARATOR, 2))
				.filter(item -> (item.length == 2) && !item[0].isBlank() && !item[1].isBlank())
				.forEach(item -> builder.scalar(item[0].trim(), item[1].trim()));
		}
	}

}
