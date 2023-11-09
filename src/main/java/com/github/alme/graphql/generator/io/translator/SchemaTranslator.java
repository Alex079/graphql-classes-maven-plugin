package com.github.alme.graphql.generator.io.translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.io.Util;

import graphql.language.Document;
import graphql.language.SchemaDefinition;
import graphql.language.SchemaExtensionDefinition;

public class SchemaTranslator implements Translator {

	private static final Collection<String> IMPLICIT_SCHEMA = new HashSet<>();

	static {
		IMPLICIT_SCHEMA.add("Query");
		IMPLICIT_SCHEMA.add("Mutation");
		IMPLICIT_SCHEMA.add("Subscription");
	}

	@Override
	public void translate(Document doc, GqlContext ctx) {
		Collection<SchemaDefinition> main = new ArrayList<>();
		Collection<SchemaExtensionDefinition> ext = new ArrayList<>();
		doc.getDefinitionsOfType(SchemaDefinition.class).forEach(i -> {
			if (i.getClass() == SchemaDefinition.class) {
				main.add(i);
			}
			else if (i.getClass() == SchemaExtensionDefinition.class) {
				ext.add((SchemaExtensionDefinition) i);
			}
		});
		setImplicit(ctx);
		populate(ctx, main);
		populate(ctx, ext);
	}

	private void setImplicit(GqlContext ctx) {
		ctx.getObjectTypes().keySet().stream()
			.filter(IMPLICIT_SCHEMA::contains)
			.forEach(name -> ctx.getOperations().put(name.toLowerCase(), name));
	}

	private void populate(GqlContext ctx, Collection<? extends SchemaDefinition> definitions) {
		definitions.forEach(definition -> {
			ctx.getSchemaJavadoc().addAll(Util.extractJavadoc(definition));
			definition.getOperationTypeDefinitions().forEach(operation ->
				ctx.getOperations().put(operation.getName(), operation.getTypeName().getName()));
		});
	}

}
