package com.github.alme.graphql.generator.io.translator;

import java.util.ArrayList;
import java.util.Collection;

import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.dto.GqlStructure;

import graphql.language.Document;
import graphql.language.TypeName;
import graphql.language.UnionTypeDefinition;
import graphql.language.UnionTypeExtensionDefinition;

public class UnionTypeTranslator implements Translator {

	@Override
	public void translate(Document doc, GqlContext ctx) {
		Collection<UnionTypeDefinition> main = new ArrayList<>();
		Collection<UnionTypeExtensionDefinition> ext = new ArrayList<>();
		doc.getDefinitionsOfType(UnionTypeDefinition.class).forEach(i -> {
			if (i.getClass() == UnionTypeDefinition.class) {
				main.add(i);
			}
			else if (i.getClass() == UnionTypeExtensionDefinition.class) {
				ext.add((UnionTypeExtensionDefinition) i);
			}
		});
		populate(ctx, main);
		populate(ctx, ext);
	}

	private void populate(GqlContext ctx, Collection<? extends UnionTypeDefinition> definitions) {
		definitions.forEach(definition -> {
			ctx.getInterfaceTypes().merge(
				definition.getName(),
				GqlStructure.of(definition),
				GqlStructure::merge);
			definition.getMemberTypes().stream()
				.map(TypeName.class::cast)
				.map(TypeName::getName)
				.forEach(memberTypeName -> ctx.getObjectTypes().merge(
					memberTypeName,
					GqlStructure.of(memberTypeName, definition.getName()),
					GqlStructure::merge));
		});
	}

}
