package com.github.alme.graphql.generator.dto;

import java.util.Collection;
import java.util.HashSet;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude = {"selections", "variables"})
@ToString(exclude = {"selections", "variables"})
public class GqlOperation {

	String name;
	String operation;
	String typeName;
	String text;
	Collection<GqlField> variables = new HashSet<>();
	Collection<GqlSelection> selections = new HashSet<>();

	public GqlOperation addVariables(Collection<GqlField> variables) {
		if (variables != null) {
			this.variables.addAll(variables);
		}
		return this;
	}

	public GqlOperation addSelections(Collection<GqlSelection> selections) {
		if (selections != null) {
			this.selections.addAll(selections);
		}
		return this;
	}

}
