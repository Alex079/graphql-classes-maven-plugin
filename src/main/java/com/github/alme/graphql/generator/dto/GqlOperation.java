package com.github.alme.graphql.generator.dto;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class GqlOperation {

	public final String name;
	public final String operation;
	public final GqlType type;
	public String text;
	public final Collection<GqlField> variables = new HashSet<>();
	public final Collection<GqlSelection> selection = new HashSet<>();
	
	public GqlOperation(String name, String operation, GqlType type) {
		this.name = name;
		this.operation = operation;
		this.type = type;
	}
	
	public GqlOperation addVariables(Collection<GqlField> variables) {
		this.variables.addAll(variables);
		return this;
	}
	
	public GqlOperation addSelection(Collection<GqlSelection> selection) {
		this.selection.addAll(selection);
		return this;
	}

	public GqlOperation setText(String text) {
		this.text = text;
		return this;
	}

	public String getName() {
		return name;
	}

	public String getOperation() {
		return operation;
	}

	public String getText() {
		return text;
	}

	public GqlType getType() {
		return type;
	}

	public Collection<GqlField> getVariables() {
		return new HashSet<>(variables);
	}

	public Collection<GqlSelection> getSelection() {
		return new HashSet<>(selection);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, operation, selection, text, type, variables);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof GqlOperation)) {
			return false;
		}
		GqlOperation other = (GqlOperation) obj;
		return Objects.equals(name, other.name) && Objects.equals(operation, other.operation)
				&& Objects.equals(selection, other.selection) && Objects.equals(text, other.text)
				&& Objects.equals(type, other.type) && Objects.equals(variables, other.variables);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{name=").append(name).append(", operation=").append(operation).append(", type=").append(type)
				.append(", text=").append(text).append(", variables=").append(variables).append(", selection=")
				.append(selection).append("}");
		return builder.toString();
	}
}
