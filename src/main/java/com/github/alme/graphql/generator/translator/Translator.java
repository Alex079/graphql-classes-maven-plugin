package com.github.alme.graphql.generator.translator;

import com.github.alme.graphql.generator.dto.GqlContext;

import graphql.language.Document;

public interface Translator {

	void translate(Document doc, GqlContext ctx);

}
