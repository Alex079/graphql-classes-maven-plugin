package com.github.alme.graphql.generator.dto;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class GqlSelection {

	private final String name;
	private final GqlType type;
	private final Collection<GqlSelection> selection = new HashSet<>();
	
	public GqlSelection(String name, GqlType type) {
		this.name = name;
		this.type = type;
	}
	
	public GqlSelection addSelection(Collection<GqlSelection> selection) {
		this.selection.addAll(selection);
		return this;
	}

	public String getName() {
		return name;
	}

	public GqlType getType() {
		return type;
	}

	public Collection<GqlSelection> getSelection() {
		return new HashSet<>(selection);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, selection, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof GqlSelection)) {
			return false;
		}
		GqlSelection other = (GqlSelection) obj;
		return Objects.equals(name, other.name) && Objects.equals(selection, other.selection)
				&& Objects.equals(type, other.type);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{name=").append(name).append(", type=").append(type).append(", selection=").append(selection)
				.append("}");
		return builder.toString();
	}
}
