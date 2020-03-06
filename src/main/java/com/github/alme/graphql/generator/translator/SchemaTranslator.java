package com.github.alme.graphql.generator.translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import com.github.alme.graphql.generator.dto.Context;
import com.github.alme.graphql.generator.dto.GqlField;

import graphql.language.Document;
import graphql.language.SchemaDefinition;
import graphql.language.SchemaExtensionDefinition;

public class SchemaTranslator implements Translator {

	@Override
	public void translate(Document doc, Context ctx) {
		Collection<SchemaDefinition> main = new ArrayList<>();
		Collection<SchemaExtensionDefinition> ext = new ArrayList<>();
		doc.getDefinitionsOfType(SchemaDefinition.class).forEach((i) -> {
			if (i.getClass() == SchemaDefinition.class) {
				main.add(i);
			}
			else if (i.getClass() == SchemaExtensionDefinition.class) {
				ext.add((SchemaExtensionDefinition) i);
			}
		});
		populate(doc, ctx, main);
		populate(doc, ctx, ext);
	}

	private void populate(Document doc, Context ctx, Collection<? extends SchemaDefinition> definitions) {
		ctx.getSchema().addAll(
			definitions.stream()
			.map(SchemaDefinition::getOperationTypeDefinitions)
			.flatMap(Collection::stream)
			.map((def) -> new GqlField(def.getName(), Util.translateType(def.getTypeName(), doc, ctx)))
			.collect(Collectors.toSet()));
	}

}
