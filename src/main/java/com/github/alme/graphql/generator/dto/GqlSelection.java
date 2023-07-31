package com.github.alme.graphql.generator.dto;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import graphql.language.SelectionSet;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(exclude = {"targetTypeName", "subsets"})
@ToString(exclude = {"subsets"})
public class GqlSelection {

	private final GqlField field;
	private final String alias;
	private final String fragmentTypeName;
	private String targetTypeName = "";
	private final Set<SelectionSet> subsets = new LinkedHashSet<>();

	public GqlSelection addSubset(SelectionSet selectionSet) {
		if (selectionSet != null) {
			subsets.add(selectionSet);
		}
		return this;
	}

	public GqlSelection addSubsets(Collection<SelectionSet> selectionSets) {
		if (selectionSets != null) {
			subsets.addAll(selectionSets);
		}
		return this;
	}

	public boolean equalsWithSubsets(GqlSelection other) {
		return equals(other) && subsets.equals(other.subsets);
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
