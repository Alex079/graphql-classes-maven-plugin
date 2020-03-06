package com.github.alme.graphql.generator.dto;

import java.util.Objects;

public class GqlField {

	private final String name;
	private final GqlType type;
	
	public GqlField(String name, GqlType type) {
		this.name = name;
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	
	public GqlType getType() {
		return type;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(name, type);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof GqlField)) {
			return false;
		}
		GqlField other = (GqlField) obj;
		return Objects.equals(name, other.name) && Objects.equals(type, other.type);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{name=").append(name).append(", type=").append(type).append("}");
		return builder.toString();
	}
}
