package com.github.alme.graphql.generator.io.translator;

import java.util.Collection;
import java.util.List;

import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.dto.GqlStructure;

import graphql.kickstart.tools.relay.RelayConnectionFactory;
import graphql.language.Document;
import graphql.language.ObjectTypeDefinition;

public class RelayConnectionTranslator implements Translator {

	@Override
	public void translate(Document doc, GqlContext ctx) {
		@SuppressWarnings("rawtypes")
		List definitions = doc.getDefinitions();
		@SuppressWarnings("unchecked")
		Collection<ObjectTypeDefinition> injected = new RelayConnectionFactory().create(definitions);
		populate(ctx, injected);
	}

	private void populate(GqlContext ctx, Collection<ObjectTypeDefinition> definitions) {
		definitions.forEach(definition ->
			ctx.getObjectTypes().merge(
				definition.getName(),
				GqlStructure.of(definition, ctx::applyNaming),
				GqlStructure::merge));
	}

}
