package com.github.alme.graphql.generator.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GqlValue {
	
	private final String simple;
	private final List<GqlValue> list;
	private final Map<String, GqlValue> object;

	public GqlValue(String value) {
		simple = value;
		list = null;
		object = null;
	}

	public GqlValue(Map<String, GqlValue> value) {
		simple = null;
		list = null;
		object = value;
	}

	public GqlValue(List<GqlValue> value) {
		simple = null;
		list = value;
		object = null;
	}

	public String getSimple() {
		return simple;
	}
	
	public List<GqlValue> getList() {
		return new ArrayList<>(list);
	}
	
	public Map<String, GqlValue> getObject() {
		return new HashMap<>(object);
	}

	@Override
	public int hashCode() {
		return Objects.hash(list, object, simple);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof GqlValue)) {
			return false;
		}
		GqlValue other = (GqlValue) obj;
		return Objects.equals(list, other.list)
				&& Objects.equals(object, other.object)
				&& Objects.equals(simple, other.simple);
	}

	@Override
	public String toString() {
		if (list != null)
			return list.toString();
		if (object != null)
			return object.toString();
		return simple;
	}

}
