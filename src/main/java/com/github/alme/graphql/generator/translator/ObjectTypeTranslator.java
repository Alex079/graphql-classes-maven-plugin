package com.github.alme.graphql.generator.translator;

import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.github.alme.graphql.generator.dto.Context;
import com.github.alme.graphql.generator.dto.GqlStructure;
import graphql.language.Document;
import graphql.language.ObjectTypeDefinition;
import graphql.language.ObjectTypeExtensionDefinition;
import graphql.language.TypeName;

public class ObjectTypeTranslator implements Translator {

	private static final Collection<String> IMPLICIT_SCHEMA = new HashSet<>();
	static {
		IMPLICIT_SCHEMA.add("Query");
		IMPLICIT_SCHEMA.add("Mutation");
		IMPLICIT_SCHEMA.add("Subscription");
	}

	@Override
	public void translate(Document doc, Context ctx) {
		Collection<ObjectTypeDefinition> main = new ArrayList<>();
		Collection<ObjectTypeExtensionDefinition> ext = new ArrayList<>();
		doc.getDefinitionsOfType(ObjectTypeDefinition.class).forEach((i) -> {
			if (i.getClass() == ObjectTypeDefinition.class) {
				main.add(i);
			}
			else if (i.getClass() == ObjectTypeExtensionDefinition.class) {
				ext.add((ObjectTypeExtensionDefinition) i);
			}
		});
		populate(doc, ctx, main);
		populate(doc, ctx, ext);
	}

	private void populate(Document doc, Context ctx, Collection<? extends ObjectTypeDefinition> definitions) {
		definitions.forEach((def) -> {
			ctx.getObjectTypes()
			.computeIfAbsent(def.getName(), GqlStructure::new)
			.addMembers(
				def.getImplements().stream()
				.map(TypeName.class::cast).map(TypeName::getName)
				.collect(toSet()))
			.addFields(
				def.getFieldDefinitions().stream()
				.map(Util.fromFieldDef(doc, ctx))
				.collect(toSet()));
			if (IMPLICIT_SCHEMA.contains(def.getName())) {
				ctx.getSchema().put(def.getName().toLowerCase(), def.getName());
			}
		});
	}

}
