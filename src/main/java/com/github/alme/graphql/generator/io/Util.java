package com.github.alme.graphql.generator.io;

import static java.util.stream.Collectors.toList;

import java.util.function.Function;

import com.github.alme.graphql.generator.dto.GqlArgument;
import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.dto.GqlField;
import com.github.alme.graphql.generator.dto.GqlType;

import graphql.language.FieldDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
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

	public static String firstUpper(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	public static String firstLower(String s) {
		return s.substring(0, 1).toLowerCase() + s.substring(1);
	}

}
