package com.github.alme.graphql.generator.translator;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toSet;

import static com.github.alme.graphql.generator.translator.Util.fromFieldDef;

import java.util.Collection;
import java.util.List;

import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.dto.GqlStructure;

import graphql.kickstart.tools.relay.RelayConnectionFactory;
import graphql.language.Definition;
import graphql.language.Directive;
import graphql.language.Document;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeName;

public class RelayConnectionTranslator implements Translator {

	@Override
	public void translate(Document doc, GqlContext ctx) {
		List<ObjectTypeDefinition> definitions = doc.getDefinitionsOfType(ObjectTypeDefinition.class);
		boolean connectionDirectiveIsFound = definitions.stream()
			.map(ObjectTypeDefinition::getFieldDefinitions)
			.flatMap(Collection::stream)
			.map(FieldDefinition::getDirectives)
			.flatMap(Collection::stream)
			.map(Directive::getName)
			.anyMatch("connection"::equals);
		if (connectionDirectiveIsFound) {
			List<? extends Definition<?>> injected = new RelayConnectionFactory().create(unmodifiableList(definitions));
			populate(ctx, (Collection<ObjectTypeDefinition>) injected);
		}
	}

	private void populate(GqlContext ctx, Collection<ObjectTypeDefinition> definitions) {
		definitions.forEach(definition ->
			ctx.getObjectTypes()
				.computeIfAbsent(definition.getName(), GqlStructure::new)
				.addMembers(definition.getImplements().stream().map(TypeName.class::cast).map(TypeName::getName).collect(toSet()))
				.addFields(definition.getFieldDefinitions().stream().map(fromFieldDef(ctx)).collect(toSet()))
		);
	}

}
