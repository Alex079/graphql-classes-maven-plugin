package com.github.alme.graphql.generator.dto;

import java.util.Collection;
import java.util.HashSet;

import lombok.Value;

@Value
public class GqlSelection {

	String name;
	GqlType type;
	String containerTypeName;
	Collection<GqlSelection> selections = new HashSet<>();

	public GqlSelection addSelections(Collection<GqlSelection> selections) {
		this.selections.addAll(selections);
		return this;
	}

}
