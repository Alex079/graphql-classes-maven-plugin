package com.github.alme.graphql.generator.dto;

import java.util.List;

import com.github.alme.graphql.generator.io.Util;

import graphql.language.EnumValueDefinition;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude = { "javadoc" })
@ToString(exclude = { "javadoc" })
public class GqlValue {

	String name;
	List<String> javadoc;

	public static GqlValue of(EnumValueDefinition definition) {
		return new GqlValue(
			definition.getName(),
			Util.extractJavadoc(definition)
		);
	}
}
