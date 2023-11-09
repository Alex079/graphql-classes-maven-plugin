package com.github.alme.graphql.generator.io.translator;

import java.util.ArrayList;
import java.util.Collection;

import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.dto.GqlStructure;

import graphql.language.Document;
import graphql.language.EnumTypeDefinition;
import graphql.language.EnumTypeExtensionDefinition;

public class EnumTypeTranslator implements Translator {

	@Override
	public void translate(Document doc, GqlContext ctx) {

		Collection<EnumTypeDefinition> main = new ArrayList<>();
		Collection<EnumTypeExtensionDefinition> ext = new ArrayList<>();
		doc.getDefinitionsOfType(EnumTypeDefinition.class).forEach(i -> {
			if (i.getClass() == EnumTypeDefinition.class) {
				main.add(i);
			}
			else if (i.getClass() == EnumTypeExtensionDefinition.class) {
				ext.add((EnumTypeExtensionDefinition) i);
			}
		});
		populate(ctx, main);
		populate(ctx, ext);
	}

	private void populate(GqlContext ctx, Collection<? extends EnumTypeDefinition> definitions) {
		definitions.forEach(definition ->
			ctx.getEnumTypes().merge(
				definition.getName(),
				GqlStructure.of(definition),
				GqlStructure::merge));
	}

}
