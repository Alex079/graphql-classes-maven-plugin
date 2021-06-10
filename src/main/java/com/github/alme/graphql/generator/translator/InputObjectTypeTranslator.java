package com.github.alme.graphql.generator.translator;

import static java.util.stream.Collectors.toSet;

import static com.github.alme.graphql.generator.translator.Util.fromInputValueDef;

import java.util.ArrayList;
import java.util.Collection;

import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.dto.GqlStructure;

import graphql.language.Document;
import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputObjectTypeExtensionDefinition;

public class InputObjectTypeTranslator implements Translator {

	@Override
	public void translate(Document doc, GqlContext ctx) {
		Collection<InputObjectTypeDefinition> main = new ArrayList<>();
		Collection<InputObjectTypeExtensionDefinition> ext = new ArrayList<>();
		doc.getDefinitionsOfType(InputObjectTypeDefinition.class).forEach((i) -> {
			if (i.getClass() == InputObjectTypeDefinition.class) {
				main.add(i);
			}
			else if (i.getClass() == InputObjectTypeExtensionDefinition.class) {
				ext.add((InputObjectTypeExtensionDefinition) i);
			}
		});
		populate(ctx, main);
		populate(ctx, ext);
	}

	private void populate(GqlContext ctx, Collection<? extends InputObjectTypeDefinition> definitions) {
		definitions.forEach((definition) ->
			ctx.getObjectTypes()
				.computeIfAbsent(definition.getName(), GqlStructure::new)
				.addFields(definition.getInputValueDefinitions().stream().map(fromInputValueDef(ctx)).collect(toSet())));
	}

}
