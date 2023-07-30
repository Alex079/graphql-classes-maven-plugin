package com.github.alme.graphql.generator.io.translator;

import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.dto.GqlOperation;
import com.github.alme.graphql.generator.dto.GqlSelection;
import com.github.alme.graphql.generator.io.Util;

import graphql.language.Document;

public class DynamicOperationTranslator implements Translator {

	private static final String SUFFIX = "Result";

	@Override
	public void translate(Document doc, GqlContext ctx) {
		ctx.getDynamicSelections().putAll(createCompleteSelections(ctx));
		ctx.getSchema().forEach((operation, typeName) ->
			ctx.getDynamicOperations().put("Dynamic" + Util.firstUpper(operation), new GqlOperation(null, operation, typeName, null))
		);
	}

	private static Map<String, Collection<GqlSelection>> createCompleteSelections(GqlContext ctx) {
		Map<String, Collection<GqlSelection>> result = new HashMap<>();
		// build selections for object types
		Map<String, String> aliasMap = ctx.getAliases();
		ctx.getObjectTypes().forEach((objectTypeName, objectType) ->
			result.put(objectTypeName + SUFFIX, objectType.getFields().stream()
				.map(field -> new GqlSelection(field, aliasMap.getOrDefault(field.getName(), ""), ""))
				.collect(toSet())));
		// build selections for interfaces using object types
		ctx.getInterfaceTypes().forEach((interfaceTypeName, interfaceType) ->
			result.put(interfaceTypeName + SUFFIX, Stream.concat(
				interfaceType.getFields().stream()
					.map(field -> new GqlSelection(field, aliasMap.getOrDefault(field.getName(), ""), "")),
				ctx.getObjectTypes().values().stream()
					.filter(objectType -> objectType.getMembers().contains(interfaceTypeName))
					.flatMap(objectType -> objectType.getFields().stream()
						.map(field -> new GqlSelection(field, field.getName() + '_' + objectType.getName(), objectType.getName())))
				)
				.collect(toSet())));
		// build selections for unions using object types
		ctx.getUnionTypes().keySet().forEach(unionTypeName ->
			result.put(unionTypeName + SUFFIX, ctx.getObjectTypes().values().stream()
				.filter(objectType -> objectType.getMembers().contains(unionTypeName))
				.flatMap(objectType -> objectType.getFields().stream()
					.map(field -> new GqlSelection(field, field.getName() + '_' + objectType.getName(), objectType.getName())))
				.collect(toSet())));
		// link selections by inner type
		result.values().stream()
			.flatMap(Collection::stream)
			.filter(selection -> result.containsKey(selection.getType().getInner() + SUFFIX))
			.forEach(selection -> selection.setTargetTypeName(selection.getType().getInner() + SUFFIX));
		return result;
	}

}
