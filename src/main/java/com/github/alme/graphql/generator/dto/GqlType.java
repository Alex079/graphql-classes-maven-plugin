package com.github.alme.graphql.generator.dto;

import lombok.Value;

@Value
public class GqlType {

	public enum Flag {
		MANDATORY, LIST, NAMED
	}

	Flag flag;
	String name;
	GqlType nested;

	public static GqlType mandatory(GqlType nested) {
		return new GqlType(Flag.MANDATORY, null, nested);
	}

	public static GqlType list(GqlType nested) {
		return new GqlType(Flag.LIST, null, nested);
	}

	public static GqlType named(String name) {
		return new GqlType(Flag.NAMED, name, null);
	}

	public String getInner() {
		GqlType res = this;
		while (res.getNested() != null) {
			res = res.getNested();
		}
		return res.getName();
	}

	public String getFull() {
		switch (flag) {
			case MANDATORY:
				return nested.getFull();
			case LIST:
				return String.format("List<%s>", nested.getFull());
			case NAMED:
			default:
				return name;
		}
	}

	public String getCustom(String customType) {
		switch (flag) {
			case MANDATORY:
				return nested.getCustom(customType);
			case LIST:
				return String.format("List<%s>", nested.getCustom(customType));
			case NAMED:
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
			case NAMED:
			default:
				return name;
		}
	}
}
