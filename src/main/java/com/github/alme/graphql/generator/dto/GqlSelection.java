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

	GqlField field;
	String alias;
	String fragmentTypeName;

	Collection<GqlSelection> selections = new HashSet<>();

	public GqlSelection addSelections(Collection<GqlSelection> selections) {
		if (selections != null) {
			this.selections.addAll(selections);
		}
		return this;
	}

	public String getName() {
		return field.getName();
	}

	public GqlType getType() {
		return field.getType();
	}

	public String getTitle() {
		return (alias == null || alias.isEmpty()) ? field.getName() : alias;
	}

	public Collection<GqlArgument> getArguments() {
		return field.getArguments();
	}

}
