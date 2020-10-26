package com.github.alme.graphql.generator.dto;

import java.util.Collection;
import java.util.HashSet;

import lombok.Value;

@Value
public class GqlStructure {

	String name;
	Collection<String> members = new HashSet<>();
	Collection<GqlField> fields = new HashSet<>();

	public GqlStructure addMembers(Collection<String> members) {
		this.members.addAll(members);
		return this;
	}

	public GqlStructure addMember(String member) {
		this.members.add(member);
		return this;
	}

	public GqlStructure addFields(Collection<GqlField> fields) {
		this.fields.addAll(fields);
		return this;
	}

}
