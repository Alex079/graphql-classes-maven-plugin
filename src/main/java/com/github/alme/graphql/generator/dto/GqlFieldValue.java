package com.github.alme.graphql.generator.dto;

import java.util.Objects;

public class GqlFieldValue {

	private final String name;
	private final GqlType type;
	private final GqlValue defaultValue;
	
	public GqlFieldValue(String name, GqlType type, GqlValue defaultValue) {
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
	}
	
	public String getName() {
		return name;
	}
	
	public GqlType getType() {
		return type;
	}
	
	public GqlValue getDefaultValue() {
		return defaultValue;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(defaultValue, name, type);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof GqlFieldValue)) {
			return false;
		}
		GqlFieldValue other = (GqlFieldValue) obj;
		return Objects.equals(defaultValue, other.defaultValue) && Objects.equals(name, other.name)
				&& Objects.equals(type, other.type);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{name=").append(name).append(", type=").append(type).append(", defaultValue=")
				.append(defaultValue).append("}");
		return builder.toString();
	}
}
