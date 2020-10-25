package com.github.alme.graphql.generator.translator;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import com.github.alme.graphql.generator.dto.Context;
import com.github.alme.graphql.generator.dto.GqlField;
import com.github.alme.graphql.generator.dto.GqlSelection;
import com.github.alme.graphql.generator.dto.GqlStructure;
import com.github.alme.graphql.generator.dto.GqlType;

import graphql.language.Field;
import graphql.language.FieldDefinition;
import graphql.language.FragmentDefinition;
import graphql.language.FragmentSpread;
import graphql.language.InlineFragment;
import graphql.language.InputValueDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.SelectionSet;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.language.VariableDefinition;

public final class Util {

	private Util() {}

	public static GqlType translateType(Type<?> type, Context ctx) {
		if (type instanceof NonNullType) {
			return GqlType.mandatory(translateType(((NonNullType) type).getType(), ctx));
		}
		else if (type instanceof ListType) {
			return GqlType.list(translateType(((ListType) type).getType(), ctx));
		}
		else if (type instanceof TypeName) {
			String name = ((TypeName) type).getName();
			String scalar = ctx.getScalarMap().get(name);
			return GqlType.named(scalar == null ? name : formatTypeName(name, scalar));
		}
		return null;
	}

	private static String formatTypeName(String original, String current) {
		if (Objects.equals(original, current)) {
			return current;
		}
		return String.format("/*%s=>*/%s", original, current);
	}

	public static Collection<GqlSelection> translateSelection(
		SelectionSet selectionSet,
		Collection<FragmentDefinition> fragments,
		Context ctx,
		String typeName
	) {
		Collection<GqlSelection> result = new ArrayList<>();
		result.addAll(
			selectionSet.getSelectionsOfType(Field.class).stream()
				.map((field) -> {
					GqlType type = guessTypeOfField(field, ctx, typeName);
					GqlSelection selection = new GqlSelection(field.getAlias() == null ? field.getName() : field.getAlias(), type);
					if (field.getSelectionSet() != null) {
						selection.addSelections(translateSelection(field.getSelectionSet(), fragments, ctx, type.getInner()));
					}
					return selection;
				})
				.collect(toList()));
		result.addAll(
			selectionSet.getSelectionsOfType(InlineFragment.class).stream()
				.map((fragment) -> translateSelection(fragment.getSelectionSet(), fragments, ctx, fragment.getTypeCondition().getName()))
				.flatMap(Collection::stream)
				.collect(toList()));
		result.addAll(
			selectionSet.getSelectionsOfType(FragmentSpread.class).stream()
				.map(FragmentSpread::getName)
				.map((fragmentName) -> fragments.stream().filter((candidate) -> Objects.equals(fragmentName, candidate.getName())).findAny())
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map((fragment) -> translateSelection(fragment.getSelectionSet(), fragments, ctx, fragment.getTypeCondition().getName()))
				.flatMap(Collection::stream)
				.collect(toList()));
		return result;
	}

	private static GqlType guessTypeOfField(Field field, Context ctx, String containerType) {
		return Stream.concat(
			Optional.ofNullable(ctx.getObjectTypes().get(containerType))
				.map(GqlStructure::getFields)
				.map(Collection::stream)
				.orElseGet(Stream::empty),
			Optional.ofNullable(ctx.getInterfaceTypes().get(containerType))
				.map(GqlStructure::getFields)
				.map(Collection::stream)
				.orElseGet(Stream::empty))
			.filter((candidate) -> Objects.equals(field.getName(), candidate.getName()))
			.map(GqlField::getType)
			.findAny()
			.orElse(GqlType.named("String"));
	}

	public static Function<FieldDefinition, GqlField> fromFieldDef(Context ctx) {
		return (v) -> new GqlField(v.getName(), translateType(v.getType(), ctx));
	}

	public static Function<InputValueDefinition, GqlField> fromInputValueDef(Context ctx) {
		return (v) -> new GqlField(v.getName(), translateType(v.getType(), ctx));
	}

	public static Function<VariableDefinition, GqlField> fromVariableDef(Context ctx) {
		return (v) -> new GqlField(v.getName(), translateType(v.getType(), ctx));
	}

}
