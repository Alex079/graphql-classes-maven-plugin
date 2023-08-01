package com.github.alme.graphql.generator.dto;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import graphql.language.SelectionSet;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.Delegate;

@Value
@EqualsAndHashCode(exclude = {"targetTypeName", "subsets"})
@ToString(exclude = {"subsets"})
public class GqlSelection {

	@Delegate GqlField field;
	String alias;
	String fragmentTypeName;
	AtomicReference<String> targetTypeName = new AtomicReference<>("");
	Set<SelectionSet> subsets = new LinkedHashSet<>();

	public String getTargetTypeName() {
		return targetTypeName.get();
	}

	public GqlSelection setTargetTypeName(String typeName) {
		targetTypeName.set(typeName);
		return this;
	}

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

	public String getTitle() {
		return (alias == null || alias.isEmpty()) ? field.getName() : alias;
	}

}
