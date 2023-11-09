package com.github.alme.graphql.generator.dto;

import static java.util.Collections.emptySet;

import java.util.Set;

import lombok.Value;

@Value
public class GqlOperation {

	String name;
	String operation;
	String typeName;
	String text;
	Set<GqlField> variables;

	public static GqlOperation of(String name, String operation, String typeName, String text, Set<GqlField> variables) {
		return new GqlOperation(
			name,
			operation,
			typeName,
			text,
			variables
		);
	}

	public static GqlOperation of(String operation, String typeName) {
		return new GqlOperation(
			null,
			operation,
			typeName,
			null,
			emptySet()
		);
	}
}
