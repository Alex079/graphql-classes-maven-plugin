package com.github.alme.graphql.generator.dto;

import java.util.Collection;
import java.util.HashSet;

import lombok.Value;

@Value
public class GqlOperation {

	String name;
	String operation;
	String typeName;
	String text;
	Collection<GqlField> variables = new HashSet<>();
	Collection<GqlSelection> selections = new HashSet<>();

	public GqlOperation addVariables(Collection<GqlField> variables) {
		this.variables.addAll(variables);
		return this;
	}

	public GqlOperation addSelections(Collection<GqlSelection> selections) {
		this.selections.addAll(selections);
		return this;
	}

}
