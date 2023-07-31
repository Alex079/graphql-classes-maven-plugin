package com.github.alme.graphql.generator.io.translator;

import static java.util.stream.Collectors.toSet;

import static com.github.alme.graphql.generator.io.Util.fromFieldDef;

import java.util.ArrayList;
import java.util.Collection;

import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.dto.GqlStructure;

import graphql.language.Document;
import graphql.language.ObjectTypeDefinition;
import graphql.language.ObjectTypeExtensionDefinition;
import graphql.language.TypeName;

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
			ctx.getObjectTypes()
				.computeIfAbsent(definition.getName(), GqlStructure::new)
				.addMembers(definition.getImplements().stream().map(TypeName.class::cast).map(TypeName::getName).collect(toSet()))
				.addFields(definition.getFieldDefinitions().stream().map(fromFieldDef(ctx)).collect(toSet())));
	}

}
