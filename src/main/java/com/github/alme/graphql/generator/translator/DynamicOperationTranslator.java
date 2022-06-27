package com.github.alme.graphql.generator.translator;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.dto.GqlOperation;
import com.github.alme.graphql.generator.dto.GqlSelection;

import graphql.language.Document;

public class DynamicOperationTranslator implements Translator {

	@Override
	public void translate(Document doc, GqlContext ctx) {
		ctx.getDynamicSelections().putAll(createCompleteSelections(ctx));
		ctx.getSchema().forEach((operation, typeName) ->
			ctx.getDynamicOperations().add(new GqlOperation(null, operation, typeName, null))
		);
	}

	private static Map<String, Collection<GqlSelection>> createCompleteSelections(GqlContext ctx) {
		Map<String, Collection<GqlSelection>> result = new HashMap<>();
		// build selections for object types
		ctx.getObjectTypes().forEach((objectTypeName, objectType) ->
			result.put(objectTypeName, objectType.getFields().stream().map(field ->
				new GqlSelection(field.getName(), field.getType(), "")).collect(toList()))
		);
		// build selections for interfaces using object types
		ctx.getInterfaceTypes().forEach((interfaceTypeName, interfaceType) -> {
			Set<String> uniqueNames = new HashSet<>();
			List<GqlSelection> selections = Stream.concat(
				interfaceType.getFields().stream().map(field -> {
					uniqueNames.add(field.getName());
					return new GqlSelection(field.getName(), field.getType(), "");
				}),
				ctx.getObjectTypes().values().stream()
					.filter(objectType -> objectType.getMembers().contains(interfaceTypeName))
					.flatMap(objectType -> objectType.getFields().stream()
						.filter(field -> !uniqueNames.contains(field.getName()))
						.map(field -> new GqlSelection(field.getName(), field.getType(), objectType.getName()))
					)
			).collect(toList());
			result.put(interfaceTypeName, selections);
		});
		// build selections for unions using object types
		ctx.getUnionTypes().keySet().forEach(unionTypeName -> {
			List<GqlSelection> selections = ctx.getObjectTypes().values().stream()
				.filter(objectType -> objectType.getMembers().contains(unionTypeName))
				.flatMap(objectType -> objectType.getFields().stream()
					.map(field -> new GqlSelection(field.getName(), field.getType(), objectType.getName()))
				)
				.collect(toList());
			result.put(unionTypeName, selections);
		});
		// link selections by inner type
		result.values().stream()
			.flatMap(Collection::stream)
			.forEach(selection -> selection.addSelections(result.get(selection.getType().getInner())));
		return result;
	}

}
