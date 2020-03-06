package com.github.alme.graphql.generator.dto;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class GqlStructure {

	private final String name;
	private final Collection<String> members = new HashSet<>();
	private final Collection<GqlField> fields = new HashSet<>();
	
	public GqlStructure(String name) {
		this.name = name;
	}
	
	public GqlStructure addMembers(Collection<String> parents) {
		this.members.addAll(parents);
		return this;
	}
	
	public GqlStructure addFields(Collection<GqlField> fields) {
		this.fields.addAll(fields);
		return this;
	}
	
	public String getName() {
		return name;
	}
	
	public Collection<String> getMembers() {
		return new HashSet<>(members);
	}
	
	public Collection<GqlField> getFields() {
		return new HashSet<>(fields);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fields, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof GqlStructure)) {
			return false;
		}
		GqlStructure other = (GqlStructure) obj;
		return Objects.equals(fields, other.fields) && Objects.equals(name, other.name);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{name=").append(name).append(", fields=").append(fields).append("}");
		return builder.toString();
	}
}
