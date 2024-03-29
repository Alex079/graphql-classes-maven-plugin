package com.github.alme.graphql.generator.dto;

import java.util.List;
import java.util.function.UnaryOperator;

import com.github.alme.graphql.generator.io.Util;

import graphql.language.InputValueDefinition;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude = { "javadoc" })
@ToString(exclude = { "javadoc" })
public class GqlArgument {

	String name;
	GqlType type;
	List<String> javadoc;

	public static GqlArgument of(InputValueDefinition definition, UnaryOperator<String> naming) {
		return new GqlArgument(
			definition.getName(),
			GqlType.of(definition.getType(), naming),
			Util.extractJavadoc(definition)
		);
	}
}
