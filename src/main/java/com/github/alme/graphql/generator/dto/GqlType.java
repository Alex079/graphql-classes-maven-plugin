package com.github.alme.graphql.generator.dto;

import java.util.Objects;

public class GqlType {
	
	public enum Flag { MANDATORY, LIST, NAMED/*, SCALAR*/ }

	private final Flag flag;
	private final String name;
	private final GqlType nested;

	private GqlType(Flag flags, String name, GqlType nested) {
		this.flag = flags;
		this.name = name;
		this.nested = nested;
	}
	
	public static GqlType mandatory(GqlType nested) {
		return new GqlType(Flag.MANDATORY, null, nested);
	}
	
	public static GqlType list(GqlType nested) {
		return new GqlType(Flag.LIST, null, nested);
	}
//	
//	public static GqlType scalar(String name) {
//		return new GqlType(Flag.SCALAR, name, null);
//	}
	
	public static GqlType named(String name) {
		return new GqlType(Flag.NAMED, name, null);
	}

	public Flag getFlag() {
		return flag;
	}

	public String getName() {
		return name;
	}

	public GqlType getNested() {
		return nested;
	}

	@Override
	public int hashCode() {
		return Objects.hash(flag, name, nested);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof GqlType)) {
			return false;
		}
		GqlType other = (GqlType) obj;
		return flag == other.flag && Objects.equals(name, other.name) && Objects.equals(nested, other.nested);
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
//		case SCALAR:
		default:
			return name;
		}
	}
	
	public String getCustom(String innermost) {
		switch (flag) {
		case MANDATORY:
			return nested.getCustom(innermost);
		case LIST:
			return String.format("List<%s>", nested.getCustom(innermost));
		case NAMED:
//		case SCALAR:
		default:
			return innermost;
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
