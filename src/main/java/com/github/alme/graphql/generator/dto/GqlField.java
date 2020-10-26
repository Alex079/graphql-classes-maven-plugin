package com.github.alme.graphql.generator.dto;

import lombok.Value;

@Value
public class GqlField {

	String name;
	GqlType type;

}
