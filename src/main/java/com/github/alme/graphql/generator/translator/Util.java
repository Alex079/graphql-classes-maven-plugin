package com.github.alme.graphql.generator.translator;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import com.github.alme.graphql.generator.dto.GqlArgument;
import com.github.alme.graphql.generator.dto.GqlContext;
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
import lombok.experimental.UtilityClass;

@UtilityClass
public class Util {

	public static GqlType translateType(Type<?> type, GqlContext ctx) {
		if (type instanceof NonNullType) {
			return GqlType.mandatory(translateType(((NonNullType) type).getType(), ctx));
		}
		else if (type instanceof ListType) {
			return GqlType.list(translateType(((ListType) type).getType(), ctx));
		}
		else if (type instanceof TypeName) {
			String name = ((TypeName) type).getName();
			return GqlType.named(ctx.getScalars().getOrDefault(name, name));
		}
		return null;
	}

	public static Collection<GqlSelection> translateSelection(
		SelectionSet selectionSet,
		Collection<FragmentDefinition> allFragments,
		Collection<FragmentDefinition> requiredFragments,
		GqlContext ctx,
		String typeName
	) {
		return deepMerge(streamSelection(selectionSet, allFragments, requiredFragments, ctx, typeName));
	}

	private static Stream<GqlSelection> streamSelection(
		SelectionSet selectionSet,
		Collection<FragmentDefinition> allFragments,
		Collection<FragmentDefinition> requiredFragments,
		GqlContext ctx,
		String typeName
	) {
		return Stream.of(
			selectionSet.getSelectionsOfType(Field.class).stream()
				.map(field -> {
					GqlType type = guessTypeOfField(field, ctx, typeName);
					String alias = field.getAlias();
					if (alias == null) {
						alias = "";
					}
					GqlSelection selection = new GqlSelection(new GqlField(field.getName(), type), alias, "");
					if (field.getSelectionSet() != null) {
						selection.addSelections(translateSelection(field.getSelectionSet(), allFragments, requiredFragments, ctx, type.getInner()));
					}
					return selection;
				}),
			selectionSet.getSelectionsOfType(InlineFragment.class).stream()
				.map(fragment -> translateSelection(fragment.getSelectionSet(),
					allFragments, requiredFragments, ctx, fragment.getTypeCondition().getName()))
				.flatMap(Collection::stream),
			selectionSet.getSelectionsOfType(FragmentSpread.class).stream()
				.map(FragmentSpread::getName)
				.map(fragmentName -> allFragments.stream()
					.filter(candidate -> Objects.equals(candidate.getName(), fragmentName))
					.filter(candidate -> matchesByType(candidate.getTypeCondition().getName(), typeName, ctx))
					.findAny())
				.filter(Optional::isPresent)
				.map(Optional::get)
				.peek(requiredFragments::add)
				.map(fragment -> translateSelection(fragment.getSelectionSet(),
					allFragments, requiredFragments, ctx, fragment.getTypeCondition().getName()))
				.flatMap(Collection::stream)
			)
			.flatMap(Function.identity());
	}

	private static Collection<GqlSelection> deepMerge(Stream<GqlSelection> selection) {
		return selection
			.reduce(
				new HashMap<GqlSelection, GqlSelection>(),
				(result, currItem) -> {
					GqlSelection prevItem = result.remove(currItem);
					if (prevItem == null) {
						result.put(currItem, currItem);
					}
					else {
						GqlSelection nextItem =
							new GqlSelection(currItem.getField(), currItem.getAlias(), currItem.getFragmentTypeName())
								.addSelections(deepMerge(Stream.concat(prevItem.getSelections().stream(), currItem.getSelections().stream())));
						result.put(nextItem, nextItem);
					}
					return result;
				},
				(mapA, mapB) -> {
					mapA.putAll(mapB);
					return mapA;
				}
			)
			.keySet();
	}

	private static GqlType guessTypeOfField(Field field, GqlContext ctx, String containerTypeName) {
		return Stream.concat(
			Optional.ofNullable(ctx.getObjectTypes().get(containerTypeName))
				.map(GqlStructure::getFields)
				.map(Collection::stream)
				.orElseGet(Stream::empty),
			Optional.ofNullable(ctx.getInterfaceTypes().get(containerTypeName))
				.map(GqlStructure::getFields)
				.map(Collection::stream)
				.orElseGet(Stream::empty))
			.filter(candidate -> Objects.equals(field.getName(), candidate.getName()))
			.map(GqlField::getType)
			.findAny()
			.orElse(GqlType.named("String"));
	}

	private static boolean matchesByType(String candidateType, String selectionType, GqlContext ctx) {
		if (Objects.equals(selectionType, candidateType)) {
			return true;
		}
		Set<String> candidateTypes = new HashSet<>();
		candidateTypes.add(candidateType);
		Set<String> selectionTypes = new HashSet<>();
		selectionTypes.add(selectionType);
		ctx.getObjectTypes().values().forEach(typeStructure -> {
			if (typeStructure.getMembers().contains(candidateType)) {
				candidateTypes.add(typeStructure.getName());
			}
			if (typeStructure.getMembers().contains(selectionType)) {
				selectionTypes.add(typeStructure.getName());
			}
		});
		return !Collections.disjoint(selectionTypes, candidateTypes);
	}

	public static Function<FieldDefinition, GqlField> fromFieldDef(GqlContext ctx) {
		return v -> new GqlField(v.getName(), translateType(v.getType(), ctx))
			.addArguments(v.getInputValueDefinitions().stream()
				.map(definition -> new GqlArgument(definition.getName(), translateType(definition.getType(), ctx)))
				.collect(toList()));
	}

	public static Function<InputValueDefinition, GqlField> fromInputValueDef(GqlContext ctx) {
		return v -> new GqlField(v.getName(), translateType(v.getType(), ctx));
	}

	public static Function<VariableDefinition, GqlField> fromVariableDef(GqlContext ctx) {
		return v -> new GqlField(v.getName(), translateType(v.getType(), ctx));
	}

}
