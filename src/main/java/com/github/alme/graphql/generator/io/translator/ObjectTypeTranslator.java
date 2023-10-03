package com.github.alme.graphql.generator.io.translator;

import java.util.ArrayList;
import java.util.Collection;

import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.dto.GqlStructure;

import graphql.language.Document;
import graphql.language.ObjectTypeDefinition;
import graphql.language.ObjectTypeExtensionDefinition;

public class ObjectTypeTranslator implements Translator {

	@Override
	public void translate(Document doc, GqlContext ctx) {
		Collection<ObjectTypeDefinition> main = new ArrayList<>();
		Collection<ObjectTypeExtensionDefinition> ext = new ArrayList<>();
		doc.getDefinitionsOfType(ObjectTypeDefinition.class).forEach(i -> {
			if (i.getClass() == ObjectTypeDefinition.class) {
				main.add(i);
			}
			else if (i.getClass() == ObjectTypeExtensionDefinition.class) {
				ext.add((ObjectTypeExtensionDefinition) i);
			}
		});
		populate(ctx, main);
		populate(ctx, ext);
	}

	private void populate(GqlContext ctx, Collection<? extends ObjectTypeDefinition> definitions) {
		definitions.forEach(definition ->
			ctx.getObjectTypes().merge(
				definition.getName(),
				GqlStructure.of(definition, ctx::applyNaming),
				GqlStructure::merge));
	}

}
