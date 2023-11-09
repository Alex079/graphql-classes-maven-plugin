package com.github.alme.graphql.generator.io.translator;

import java.util.ArrayList;
import java.util.Collection;

import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.dto.GqlStructure;

import graphql.language.Document;
import graphql.language.InterfaceTypeDefinition;
import graphql.language.InterfaceTypeExtensionDefinition;

public class InterfaceTypeTranslator implements Translator {

	@Override
	public void translate(Document doc, GqlContext ctx) {
		Collection<InterfaceTypeDefinition> main = new ArrayList<>();
		Collection<InterfaceTypeExtensionDefinition> ext = new ArrayList<>();
		doc.getDefinitionsOfType(InterfaceTypeDefinition.class).forEach(i -> {
			if (i.getClass() == InterfaceTypeDefinition.class) {
				main.add(i);
			}
			else if (i.getClass() == InterfaceTypeExtensionDefinition.class) {
				ext.add((InterfaceTypeExtensionDefinition) i);
			}
		});
		populate(ctx, main);
		populate(ctx, ext);
	}

	private void populate(GqlContext ctx, Collection<? extends InterfaceTypeDefinition> definitions) {
		definitions.forEach(definition ->
			ctx.getInterfaceTypes().merge(
				definition.getName(),
				GqlStructure.of(definition, ctx::applyNaming),
				GqlStructure::merge));
	}

}
