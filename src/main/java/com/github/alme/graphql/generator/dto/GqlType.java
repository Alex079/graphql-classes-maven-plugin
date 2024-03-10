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
		if (type instanceof NonNullType nonnull) {
			return mandatory(of(nonnull.getType(), naming));
		}
		else if (type instanceof ListType list) {
			return list(of(list.getType(), naming));
		}
		else if (type instanceof TypeName named) {
			return named(naming.apply(named.getName()));
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
		return switch (flag) {
			case MANDATORY -> nested.getFull();
			case LIST -> "java.util.List<%s>".formatted(nested.getFull());
			case NAMED -> name;
		};
	}

	/**
	 * Used in templates
	 */
	public String getCustom(String customType) {
		return switch (flag) {
			case MANDATORY -> nested.getCustom(customType);
			case LIST -> "java.util.List<%s>".formatted(nested.getCustom(customType));
			case NAMED -> customType;
		};
	}

	@Override
	public String toString() {
		return switch (flag) {
			case MANDATORY -> "%s!".formatted(nested);
			case LIST -> "[%s]".formatted(nested);
			case NAMED -> name;
		};
	}
}
