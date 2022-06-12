package com.github.alme.graphql.generator.dto;

import java.util.Collection;
import java.util.HashSet;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude = "selections")
@ToString(exclude = "selections")
public class GqlSelection {

	String name;
	GqlType type;
	String fragmentTypeName;
	Collection<GqlSelection> selections = new HashSet<>();

	public GqlSelection addSelections(Collection<GqlSelection> selections) {
		if (selections != null) {
			this.selections.addAll(selections);
		}
		return this;
	}

}
