package com.github.alme.graphql.generator.translator;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
			return GqlType.named(ctx.getScalarMap().getOrDefault(name, name));
		}
		return null;
	}

	public static Collection<GqlSelection> translateSelection(
		SelectionSet selectionSet,
		Collection<FragmentDefinition> allFragments,
		Collection<FragmentDefinition> requiredFragments,
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
						selection.addSelections(translateSelection(field.getSelectionSet(),
							allFragments, requiredFragments, ctx, type.getInner()));
					}
					return selection;
				})
				.collect(toList()));
		result.addAll(
			selectionSet.getSelectionsOfType(InlineFragment.class).stream()
				.map((fragment) -> translateSelection(fragment.getSelectionSet(),
					allFragments, requiredFragments, ctx, fragment.getTypeCondition().getName()))
				.flatMap(Collection::stream)
				.collect(toList()));
		result.addAll(
			selectionSet.getSelectionsOfType(FragmentSpread.class).stream()
				.map(FragmentSpread::getName)
				.map((fragmentName) -> allFragments.stream()
					.filter((candidate) -> matchesByNameAndType(candidate, fragmentName, typeName, ctx)).findAny())
				.filter(Optional::isPresent)
				.map(Optional::get)
				.peek(requiredFragments::add)
				.map((fragment) -> translateSelection(fragment.getSelectionSet(),
					allFragments, requiredFragments, ctx, fragment.getTypeCondition().getName()))
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

	private static boolean matchesByNameAndType(FragmentDefinition candidate, String fragmentName, String selectionType, Context ctx) {
		if (!Objects.equals(fragmentName, candidate.getName())) {
			return false;
		}
		String candidateType = candidate.getTypeCondition().getName();
		if (Objects.equals(selectionType, candidateType)) {
			return true;
		}
		Set<String> candidateTypes = new HashSet<>();
		candidateTypes.add(candidateType);
		Set<String> selectionTypes = new HashSet<>();
		selectionTypes.add(selectionType);
		ctx.getObjectTypes().values().forEach((typeStructure) -> {
			if (typeStructure.getMembers().contains(candidateType)) {
				candidateTypes.add(typeStructure.getName());
			}
			if (typeStructure.getMembers().contains(selectionType)) {
				selectionTypes.add(typeStructure.getName());
			}
		});
		/*
		 Given matching fragment name and not matching selection type and fragment condition type,
		 'selectionType' is likely to be an interface or a union,
		 'candidateType' is likely to be an object.
		 In this case 'selectionTypes' is likely to contain more elements than 'candidateTypes'.
		 'disjoint' method will iterate over the second collection if the first collection is a 'Set',
		 therefore it is slightly better to invoke 'disjoint(selectionTypes, candidateTypes)'.
		*/
		return !Collections.disjoint(selectionTypes, candidateTypes);
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
