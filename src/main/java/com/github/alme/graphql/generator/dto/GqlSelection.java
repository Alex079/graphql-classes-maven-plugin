package com.github.alme.graphql.generator.dto;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import graphql.language.SelectionSet;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Delegate;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(exclude = { "targetTypeName", "subsets" })
@ToString(exclude = { "subsets" })
public class GqlSelection {

	private final @Delegate GqlField field;
	private final String alias;
	private final String fragmentTypeName;
	private final Set<SelectionSet> subsets;
	@Setter private String targetTypeName;

	public String getKey() {
		return String.format("%s:%s:%s:%s", alias, field.getName(), field.getType(), fragmentTypeName);
	}

	public static GqlSelection of(GqlField field, String alias, String fragmentTypeName) {
		return new GqlSelection(field, alias, fragmentTypeName, emptySet());
	}

	public static GqlSelection of(GqlField field, String alias, SelectionSet subset) {
		return new GqlSelection(field, alias, "", getSubsets(subset));
	}

	public static GqlSelection of(GqlField field, String alias) {
		return new GqlSelection(field, alias, "", emptySet());
	}

	public static GqlSelection of(String typeName, SelectionSet subset) {
		return new GqlSelection(GqlField.of(null, GqlType.named(typeName)), null, "", getSubsets(subset));
	}

	private static Set<SelectionSet> getSubsets(SelectionSet subset) {
		return Optional.ofNullable(subset).map(Collections::singleton).orElseGet(Collections::emptySet);
	}

	public GqlSelection merge(GqlSelection that) {
		return new GqlSelection(
			this.field,
			this.alias,
			this.fragmentTypeName,
			Stream.concat(this.subsets.stream(), that.subsets.stream()).collect(toSet())
		);
	}

	/**
	 * Used in templates
	 */
	public String getTitle() {
		return (alias == null || alias.isEmpty()) ? field.getName() : alias;
	}

}
