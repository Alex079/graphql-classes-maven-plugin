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
public class GqlOperation {

	@Getter public final String name;
	@Getter public final String operation;
	@Getter public final GqlType type;
	@Getter public String text;
	public final Collection<GqlField> variables = new HashSet<>();
	public final Collection<GqlSelection> selection = new HashSet<>();

	public GqlOperation addVariables(Collection<GqlField> variables) {
		this.variables.addAll(variables);
		return this;
	}

	public Collection<GqlField> getVariables() {
		return new HashSet<>(variables);
	}
	
	public GqlOperation addSelection(Collection<GqlSelection> selection) {
		this.selection.addAll(selection);
		return this;
	}

	public Collection<GqlSelection> getSelection() {
		return new HashSet<>(selection);
	}

	public GqlOperation setText(String text) {
		this.text = text;
		return this;
	}

}
