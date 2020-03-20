package com.github.alme.graphql.generator.dto;

import java.util.Collection;
import java.util.HashSet;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class GqlStructure {

	@Getter private final String name;
	private final Collection<String> members = new HashSet<>();
	private final Collection<GqlField> fields = new HashSet<>();
	
	public GqlStructure addMembers(Collection<String> members) {
		this.members.addAll(members);
		return this;
	}

	public GqlStructure addMember(String member) {
		this.members.add(member);
		return this;
	}
	
	public Collection<String> getMembers() {
		return new HashSet<>(members);
	}
	
	public GqlStructure addFields(Collection<GqlField> fields) {
		this.fields.addAll(fields);
		return this;
	}
	
	public Collection<GqlField> getFields() {
		return new HashSet<>(fields);
	}

}
