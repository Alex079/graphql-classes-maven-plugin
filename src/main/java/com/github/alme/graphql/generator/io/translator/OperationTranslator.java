package com.github.alme.graphql.generator.io.translator;

import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.dto.GqlField;
import com.github.alme.graphql.generator.dto.GqlOperation;
import com.github.alme.graphql.generator.dto.GqlSelection;
import com.github.alme.graphql.generator.dto.GqlStructure;
import com.github.alme.graphql.generator.dto.GqlType;
import com.github.alme.graphql.generator.io.Util;

import org.apache.maven.plugin.logging.Log;
import org.jetbrains.annotations.NotNull;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import graphql.language.Document;
import graphql.language.Field;
import graphql.language.FragmentDefinition;
import graphql.language.FragmentSpread;
import graphql.language.InlineFragment;
import graphql.language.OperationDefinition;
import graphql.language.SelectionSet;

public class OperationTranslator implements Translator {

	private static final String SUFFIX = "Result";
	private static final String UNNAMED = "Unnamed";
	private static final String FRAGMENTS_KEY = "fragments";
	private static final String OPERATION_KEY = "o";
	private static final String OPERATION_DOCUMENT = "OPERATION_DOCUMENT";

	private final Configuration freemarker;

	public OperationTranslator() {
		freemarker = new Configuration(Configuration.VERSION_2_3_34);
		freemarker.setClassLoaderForTemplateLoading(OperationTranslator.class.getClassLoader(), "/templates/text");
		freemarker.setDefaultEncoding(StandardCharsets.UTF_8.displayName());
		freemarker.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		freemarker.setLogTemplateExceptions(false);
		freemarker.setWrapUncheckedExceptions(true);
		freemarker.setFallbackOnNullLoopVariable(false);
	}

	@Override
	public void translate(Document doc, GqlContext ctx) {
		Collection<OperationDefinition> operations = doc.getDefinitionsOfType(OperationDefinition.class);
		Collection<FragmentDefinition> allFragments = doc.getDefinitionsOfType(FragmentDefinition.class);
		populate(ctx, operations, allFragments);
	}

	private void populate(GqlContext ctx, Collection<OperationDefinition> definitions, Collection<FragmentDefinition> allFragments) {
		definitions.forEach(definition -> {
			String operationName = definition.getOperation().name().toLowerCase();
			String typeName = ctx.getOperations().get(operationName);
			if (typeName != null) {
				String definitionName = definition.getName();
				String baseName = getOperationBaseName(definitionName, operationName);
				Collection<FragmentDefinition> requiredFragments = new HashSet<>();
				ctx.getDefinedSelections()
					.computeIfAbsent(baseName, x -> traverseSelections(
						typeName, definition.getSelectionSet(), allFragments, requiredFragments, ctx));
				ctx.getDefinedOperations()
					.computeIfAbsent(baseName, x -> GqlOperation.of(
						definitionName,
						operationName,
						typeName + SUFFIX,
						getDocumentString(definition, requiredFragments, ctx.getLog()),
						definition.getVariableDefinitions().stream().map(v -> GqlField.of(v, ctx::applyNaming)).collect(toSet())));
			}
		});
	}

	private String getOperationBaseName(String definitionName, String operationName) {
		return (definitionName == null ? UNNAMED : Util.firstUpper(definitionName)) + Util.firstUpper(operationName);
	}

	private Map<String, Collection<GqlSelection>> traverseSelections(
		String typeName,
		SelectionSet subset,
		Collection<FragmentDefinition> allFragments,
		Collection<FragmentDefinition> requiredFragments,
		GqlContext ctx
	) {
		Map<String, Map<Integer, Set<GqlSelection>>> typeMap = new HashMap<>();
		Map<String, AtomicInteger> counters = new HashMap<>();
		Queue<GqlSelection> selectionsToResolve = new LinkedList<>();
		selectionsToResolve.offer(GqlSelection.of(typeName, subset));
		while (!selectionsToResolve.isEmpty()) {
			var currentSelection = selectionsToResolve.poll();
			String currentTypeName = currentSelection.getType().getName();
			// get a set of selections by type name and their unresolved subset
			var subSelections = currentSelection.getSubsets().stream()
				.flatMap(unresolved -> resolveOneLevel(unresolved, allFragments, requiredFragments, new HashSet<>(), ctx, currentTypeName).stream())
				.collect(toSet());
			// find exactly the same selection set already linked to this type
			var variants = typeMap.computeIfAbsent(currentTypeName, x -> new HashMap<>());
			int variantNumber = variants.entrySet().stream()
				.filter(entry -> entry.getValue().equals(subSelections))
				.map(Map.Entry::getKey)
				.findAny()
				.orElseGet(() -> {
					// this selection set has not been linked to this type before
					int key = counters.computeIfAbsent(currentTypeName, x -> new AtomicInteger()).incrementAndGet();
					variants.put(key, subSelections);
					subSelections.stream().filter(s -> !s.getSubsets().isEmpty()).forEach(selectionsToResolve::offer);
					return key;
				});
			// link previous selection to the type name
			currentSelection.setTargetTypeName(currentTypeName + getTypeSuffix(variantNumber));
		}
		Map<String, Collection<GqlSelection>> result = new HashMap<>();
		typeMap.forEach((type, variants) -> variants.forEach((variantNumber, selections) ->
			result.put(type + getTypeSuffix(variantNumber), selections)
		));
		return result;
	}

	@NotNull
	private static String getTypeSuffix(int ordinalNumber) {
		return SUFFIX + (ordinalNumber < 2 ? "" : ordinalNumber);
	}

	private static Collection<GqlSelection> resolveOneLevel(
		SelectionSet selectionSet,
		Collection<FragmentDefinition> allFragments,
		Collection<FragmentDefinition> requiredFragments,
		Collection<FragmentDefinition> visitedFragments,
		GqlContext ctx,
		String typeName
	) {
		Map<String, GqlSelection> result = new HashMap<>();
		// fields declared explicitly
		selectionSet.getSelectionsOfType(Field.class).stream()
			.map(field -> GqlSelection.of(findField(field.getName(), ctx, typeName), field.getAlias(), field.getSelectionSet()))
			.forEach(selection -> result.merge(selection.getKey(), selection, GqlSelection::merge));
		// fields reachable via inline fragments
		selectionSet.getSelectionsOfType(InlineFragment.class).stream()
			.map(fragment -> resolveOneLevel(fragment.getSelectionSet(),
				allFragments, requiredFragments, visitedFragments, ctx, fragment.getTypeCondition().getName()))
			.flatMap(Collection::stream)
			.forEach(selection -> result.merge(selection.getKey(), selection, GqlSelection::merge));
		// fields reachable via named fragments
		selectionSet.getSelectionsOfType(FragmentSpread.class).stream()
			.map(FragmentSpread::getName)
			.map(fragmentName -> allFragments.stream()
				.filter(candidate -> Objects.equals(candidate.getName(), fragmentName))
				.filter(candidate -> fragmentMatchesByType(candidate.getTypeCondition().getName(), typeName, ctx))
				.findAny())
			.filter(Optional::isPresent)
			.map(Optional::get)
			.filter(visitedFragments::add)
			.map(fragment -> {
				requiredFragments.add(fragment);
				return resolveOneLevel(fragment.getSelectionSet(),
					allFragments, requiredFragments, visitedFragments, ctx, fragment.getTypeCondition().getName());
			})
			.flatMap(Collection::stream)
			.forEach(selection -> result.merge(selection.getKey(), selection, GqlSelection::merge));
		return result.values();
	}

	private static GqlField findField(String fieldName, GqlContext ctx, String containerTypeName) {
		return Stream.of(ctx.getObjectTypes().get(containerTypeName), ctx.getInterfaceTypes().get(containerTypeName))
			.filter(Objects::nonNull)
			.map(GqlStructure::getFields)
			.flatMap(Collection::stream)
			.filter(candidate -> Objects.equals(fieldName, candidate.getName()))
			.findAny()
			.orElseGet(() -> GqlField.of(fieldName, GqlType.named("String")));
	}

	private static boolean fragmentMatchesByType(String candidateType, String selectionType, GqlContext ctx) {
		if (Objects.equals(selectionType, candidateType)) {
			return true;
		}
		Set<String> candidateTypes = new HashSet<>();
		candidateTypes.add(candidateType);
		Set<String> selectionTypes = new HashSet<>();
		selectionTypes.add(selectionType);
		ctx.getObjectTypes().values().forEach(typeStructure -> {
			if (typeStructure.getParents().contains(candidateType)) {
				candidateTypes.add(typeStructure.getName());
			}
			if (typeStructure.getParents().contains(selectionType)) {
				selectionTypes.add(typeStructure.getName());
			}
		});
		return !Collections.disjoint(selectionTypes, candidateTypes);
	}

	private String getDocumentString(OperationDefinition operation, Collection<FragmentDefinition> fragments, Log log) {
		try (StringWriter writer = new StringWriter()) {
			freemarker.setSharedVariable(OPERATION_KEY, operation);
			freemarker.setSharedVariable(FRAGMENTS_KEY, fragments);
			freemarker.getTemplate(OPERATION_DOCUMENT).process(null, writer);
			return writer.toString();
		} catch (TemplateException | IOException e) {
			log.warn("Operation document [%s] is not created.".formatted(operation.getName()), e);
			return null;
		} finally {
			freemarker.clearSharedVariables();
		}
	}

}
