package com.github.alme.graphql.generator.dto;

import java.util.Collection;
import java.util.HashSet;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude = "arguments")
@ToString(exclude = "arguments")
public class GqlField {

	String name;
	GqlType type;
	Collection<GqlArgument> arguments = new HashSet<>();

	public GqlField addArguments(Collection<GqlArgument> arguments) {
		if (arguments != null) {
			this.arguments.addAll(arguments);
		}
		return this;
	}

}
