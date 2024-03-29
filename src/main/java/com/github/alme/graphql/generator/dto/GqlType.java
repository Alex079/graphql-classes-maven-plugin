package com.github.alme.graphql.generator.dto;

import java.util.function.UnaryOperator;

import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.Type;
import graphql.language.TypeName;
import lombok.Value;

@Value
public class GqlType {

	public enum Flag {
		MANDATORY,
		LIST,
		NAMED,
	}

	Flag flag;
	String name;
	GqlType nested;

	public static GqlType of(Type<?> type, UnaryOperator<String> naming) {
		if (type instanceof NonNullType) {
			return mandatory(of(((NonNullType) type).getType(), naming));
		}
		else if (type instanceof ListType) {
			return list(of(((ListType) type).getType(), naming));
		}
		else if (type instanceof TypeName) {
			return named(naming.apply(((TypeName) type).getName()));
		}
		return null;
	}

	public static GqlType mandatory(GqlType nested) {
		return new GqlType(Flag.MANDATORY, null, nested);
	}

	public static GqlType list(GqlType nested) {
		return new GqlType(Flag.LIST, null, nested);
	}

	public static GqlType named(String name) {
		return new GqlType(Flag.NAMED, name, null);
	}

	public String getName() {
		GqlType res = this;
		while (res.nested != null) {
			res = res.nested;
		}
		return res.name;
	}

	/**
	 * Used in templates
	 */
	public String getFull() {
		switch (flag) {
			case MANDATORY:
				return nested.getFull();
			case LIST:
				return String.format("java.util.List<%s>", nested.getFull());
			default:
				return name;
		}
	}

	/**
	 * Used in templates
	 */
	public String getCustom(String customType) {
		switch (flag) {
			case MANDATORY:
				return nested.getCustom(customType);
			case LIST:
				return String.format("java.util.List<%s>", nested.getCustom(customType));
			default:
				return customType;
		}
	}

	@Override
	public String toString() {
		switch (flag) {
			case MANDATORY:
				return String.format("%s!", nested);
			case LIST:
				return String.format("[%s]", nested);
			default:
				return name;
		}
	}
}
