package com.github.alme.graphql.generator.translator;

import static com.github.alme.graphql.generator.translator.Util.fromFieldDef;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collection;

import com.github.alme.graphql.generator.dto.Context;
import com.github.alme.graphql.generator.dto.GqlStructure;

import graphql.language.Document;
import graphql.language.InterfaceTypeDefinition;
import graphql.language.InterfaceTypeExtensionDefinition;

public class InterfaceTypeTranslator implements Translator {

	@Override
	public void translate(Document doc, Context ctx) {
		Collection<InterfaceTypeDefinition> main = new ArrayList<>();
		Collection<InterfaceTypeExtensionDefinition> ext = new ArrayList<>();
		doc.getDefinitionsOfType(InterfaceTypeDefinition.class).forEach((i) -> {
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

	private void populate(Context ctx, Collection<? extends InterfaceTypeDefinition> definitions) {
		definitions.forEach((definition) ->
			ctx.getInterfaceTypes()
				.computeIfAbsent(definition.getName(), GqlStructure::new)
				.addFields(definition.getFieldDefinitions().stream().map(fromFieldDef(ctx)).collect(toSet())));
	}

}
