package com.github.alme.graphql.generator.dto;

import java.util.Collection;
import java.util.HashSet;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class GqlSelection {

	@Getter private final String name;
	@Getter private final GqlType type;
	private final Collection<GqlSelection> selection = new HashSet<>();

	public GqlSelection addSelection(Collection<GqlSelection> selection) {
		this.selection.addAll(selection);
		return this;
	}

	public Collection<GqlSelection> getSelection() {
		return new HashSet<>(selection);
	}

}
