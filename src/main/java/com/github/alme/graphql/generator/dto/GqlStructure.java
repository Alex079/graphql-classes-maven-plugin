package com.github.alme.graphql.generator.dto;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.github.alme.graphql.generator.io.Util;

import graphql.language.EnumTypeDefinition;
import graphql.language.InputObjectTypeDefinition;
import graphql.language.InterfaceTypeDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeName;
import graphql.language.UnionTypeDefinition;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude = { "javadoc" })
@ToString(exclude = { "javadoc" })
public class GqlStructure {

	String name;
	Set<String> parents;
	Set<GqlField> fields;
	Set<GqlValue> values;
	List<String> javadoc;

	public static GqlStructure of(InterfaceTypeDefinition definition, UnaryOperator<String> naming) {
		return new GqlStructure(
			definition.getName(),
			definition.getImplements().stream().map(TypeName.class::cast).map(TypeName::getName).collect(toSet()),
			definition.getFieldDefinitions().stream().map(v -> GqlField.of(v, naming)).collect(toSet()),
			emptySet(),
			Util.extractJavadoc(definition)
		);
	}

	public static GqlStructure of(ObjectTypeDefinition definition, UnaryOperator<String> naming) {
		return new GqlStructure(
			definition.getName(),
			definition.getImplements().stream().map(TypeName.class::cast).map(TypeName::getName).collect(toSet()),
			definition.getFieldDefinitions().stream().map(v -> GqlField.of(v, naming)).collect(toSet()),
			emptySet(),
			Util.extractJavadoc(definition)
		);
	}

	public static GqlStructure of(InputObjectTypeDefinition definition, UnaryOperator<String> naming) {
		return new GqlStructure(
			definition.getName(),
			emptySet(),
			definition.getInputValueDefinitions().stream().map(v -> GqlField.of(v, naming)).collect(toSet()),
			emptySet(),
			Util.extractJavadoc(definition)
		);
	}

	public static GqlStructure of(EnumTypeDefinition definition) {
		return new GqlStructure(
			definition.getName(),
			emptySet(),
			emptySet(),
			definition.getEnumValueDefinitions().stream().map(GqlValue::of).collect(toSet()),
			Util.extractJavadoc(definition)
		);
	}

	public static GqlStructure of(UnionTypeDefinition definition) {
		return new GqlStructure(
			definition.getName(),
			emptySet(),
			emptySet(),
			emptySet(),
			Util.extractJavadoc(definition)
		);
	}

	public static GqlStructure of(String name, String parentName) {
		return new GqlStructure(
			name,
			Optional.ofNullable(parentName).map(Collections::singleton).orElseGet(Collections::emptySet),
			emptySet(),
			emptySet(),
			emptyList()
		);
	}

	public GqlStructure merge(GqlStructure that) {
		return new GqlStructure(
			this.name,
			Stream.concat(this.parents.stream(), that.parents.stream()).collect(toSet()),
			Stream.concat(this.fields.stream(), that.fields.stream()).collect(toSet()),
			Stream.concat(this.values.stream(), that.values.stream()).collect(toSet()),
			Stream.concat(this.javadoc.stream(), that.javadoc.stream()).collect(toList())
		);
	}

}
