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

import graphql.language.Document;
import graphql.language.Field;
import graphql.language.FieldDefinition;
import graphql.language.FragmentDefinition;
import graphql.language.FragmentSpread;
import graphql.language.InlineFragment;
import graphql.language.InputValueDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.ScalarTypeDefinition;
import graphql.language.SelectionSet;
import graphql.language.SelectionSetContainer;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.language.VariableDefinition;

public final class Util {
	
	private Util() {}
	
	public static final GqlType translateType(Type<?> type, Document doc, Context ctx) {
		if (type instanceof NonNullType) {
			return GqlType.mandatory(translateType(((NonNullType) type).getType(), doc, ctx));
		}
		else if (type instanceof ListType) {
			return GqlType.list(translateType(((ListType) type).getType(), doc, ctx));
		}
		else if (type instanceof TypeName) {
			String name = ((TypeName) type).getName();
			if (isScalar(name, doc, ctx)) {
//				return GqlType.scalar(ctx.getScalarMap().getOrDefault(name, name));
				return GqlType.named(formatTypeName(name, ctx.getScalarMap().getOrDefault(name, name)));
			}
			return GqlType.named(name);
		}
		return null;
	}
	
	private static String formatTypeName(String original, String current) {
		if (Objects.equals(original, current)) {
			return current;
		}
		return String.format("/*%s=>*/%s", original, current);
	}
//	
//	private static final GqlValue translateValue(Value<?> value, Document doc) {
//		if (value.getClass() == ArrayValue.class) {
//			return new GqlValue(((ArrayValue) value).getValues().stream()
//				.map(v -> translateValue(v, doc))
//				.collect(toList()));
//		}
//		if (value.getClass() == EnumValue.class) {
//			return new GqlValue(((EnumValue) value).getName()); // TODO type is needed
//		}
//		if (value.getClass() == NullValue.class) {
//			return new GqlValue("null");
//		}
//		if (value.getClass() == ObjectValue.class) {
//			
//		}
//		if (value.getClass() == VariableReference.class) {
//			return new GqlValue(((VariableReference) value).getName());
//		}
//		if (value.getClass() == BooleanValue.class) {
//			return new GqlValue(String.valueOf(((BooleanValue) value).isValue()));
//		}
//		if (value.getClass() == FloatValue.class) {
//			
//		}
//		if (value.getClass() == IntValue.class) {
//			
//		}
//		if (value.getClass() == StringValue.class) {
//			
//		}
//		return null;
//	}
	
	public static final Collection<GqlSelection> translateSelection(SelectionSetContainer<?> container, Document doc, Context ctx, String typeName) {
		Collection<GqlSelection> result = new ArrayList<>();
		SelectionSet selectionSet = container.getSelectionSet();
		result.addAll(
			selectionSet.getSelectionsOfType(Field.class).stream()
			.map((field) -> {
				GqlType type = guessTypeOfField(field, ctx, typeName);
				GqlSelection sel = new GqlSelection(
					field.getAlias() == null ? field.getName() : field.getAlias(),
					type);
				if (field.getSelectionSet() != null) {
					sel.addSelection(translateSelection(field, doc, ctx, type.getInner()));
				}
				return sel;
			})
			.collect(toList()));
		result.addAll(
			selectionSet.getSelectionsOfType(InlineFragment.class).stream()
			.map((fragment) -> translateSelection(fragment, doc, ctx, fragment.getTypeCondition().getName()))
			.flatMap(Collection::stream)
			.collect(toList()));
		result.addAll(
			selectionSet.getSelectionsOfType(FragmentSpread.class).stream()
			.map(FragmentSpread::getName)
			.map((fragmentName) ->
				doc.getDefinitionsOfType(FragmentDefinition.class).stream()
				.filter((candidate) -> Objects.equals(fragmentName, candidate.getName()))
				.findAny().orElse(null))
			.filter(Objects::nonNull)
			.map((fragment) -> translateSelection(fragment, doc, ctx, fragment.getTypeCondition().getName()))
			.flatMap(Collection::stream)
			.collect(toList()));
		return result;
	}

	private static GqlType guessTypeOfField(Field field, Context ctx, String containerType) {
		return Stream.concat(
			Stream.concat(
				Optional.ofNullable(ctx.getObjectTypes().get(containerType))
				.map(GqlStructure::getFields)
				.map(Collection::stream)
				.orElseGet(Stream::empty),
				Optional.ofNullable(ctx.getInterfaceTypes().get(containerType))
				.map(GqlStructure::getFields)
				.map(Collection::stream)
				.orElseGet(Stream::empty)
			),
			Optional.ofNullable(ctx.getUnionTypes().get(containerType))
			.map(GqlStructure::getMembers)
			.map(Collection::stream)
			.orElseGet(Stream::empty)
			.map(ctx.getObjectTypes()::get)
			.map(GqlStructure::getFields)
			.flatMap(Collection::stream)
		)
		.filter((candidate) -> Objects.equals(field.getName(), candidate.getName()))
		.map(GqlField::getType)
		.findAny()
		.orElse(GqlType.named("String"));
	}
	
	public static final Function<FieldDefinition, GqlField> fromFieldDef(Document doc, Context ctx) {
		return (v) -> new GqlField(
			v.getName(),
			translateType(v.getType(), doc, ctx));
	}
	
	public static final Function<InputValueDefinition, GqlField> fromInputValueDef(Document doc, Context ctx) {
		return (v) -> new GqlField(
			v.getName(),
			translateType(v.getType(), doc, ctx));
//			translateValue(v.getDefaultValue(), doc));
	}
	
	public static final Function<VariableDefinition, GqlField> fromVariableDef(Document doc, Context ctx) {
		return (v) -> new GqlField(
			v.getName(),
			translateType(v.getType(), doc, ctx));
//			translateValue(v.getDefaultValue(), doc));
	}
	
	private static final boolean isScalar(String name, Document doc, Context ctx) {
		return ctx.getScalarMap().keySet().contains(name) ||
			doc.getDefinitionsOfType(ScalarTypeDefinition.class).stream()
			.map(ScalarTypeDefinition::getName)
			.anyMatch((s) -> s.equals(name));
	}

}
