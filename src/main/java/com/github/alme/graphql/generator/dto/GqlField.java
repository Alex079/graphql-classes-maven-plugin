package com.github.alme.graphql.generator.dto;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;

import com.github.alme.graphql.generator.io.Util;

import graphql.language.FieldDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.VariableDefinition;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude = { "arguments", "javadoc" })
@ToString(exclude = { "javadoc" })
public class GqlField {

	String name;
	GqlType type;
	Set<GqlArgument> arguments;
	List<String> javadoc;

	public static GqlField of(FieldDefinition definition, UnaryOperator<String> naming) {
		return new GqlField(
			definition.getName(),
			GqlType.of(definition.getType(), naming),
			definition.getInputValueDefinitions().stream().map(d -> GqlArgument.of(d, naming)).collect(toSet()),
			Util.extractJavadoc(definition)
		);
	}

	public static GqlField of(InputValueDefinition definition, UnaryOperator<String> naming) {
		return new GqlField(
			definition.getName(),
			GqlType.of(definition.getType(), naming),
			emptySet(),
			Util.extractJavadoc(definition)
		);
	}

	public static GqlField of(VariableDefinition definition, UnaryOperator<String> naming) {
		return new GqlField(
			definition.getName(),
			GqlType.of(definition.getType(), naming),
			emptySet(),
			emptyList()
		);
	}

	public static GqlField of(String name, GqlType type) {
		return new GqlField(
			name,
			type,
			emptySet(),
			emptyList()
		);
	}
}
