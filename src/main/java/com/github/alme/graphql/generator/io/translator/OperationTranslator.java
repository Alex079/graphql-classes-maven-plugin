package com.github.alme.graphql.generator.io.translator;

import static java.util.stream.Collectors.toSet;

import static com.github.alme.graphql.generator.io.Util.fromVariableDef;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
import lombok.val;

public class OperationTranslator implements Translator {

	private static final String FRAGMENTS_KEY = "fragments";
	private static final String OPERATION_KEY = "o";
	private static final String OPERATION_DOCUMENT = "OPERATION_DOCUMENT";
	private static final Configuration CFG = new Configuration(Configuration.VERSION_2_3_32);

	static {
		CFG.setClassLoaderForTemplateLoading(OperationTranslator.class.getClassLoader(), "/templates/text");
		CFG.setDefaultEncoding(StandardCharsets.UTF_8.displayName());
		CFG.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		CFG.setLogTemplateExceptions(false);
		CFG.setWrapUncheckedExceptions(true);
		CFG.setFallbackOnNullLoopVariable(false);
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
			String typeName = ctx.getSchema().get(operationName);
			if (typeName != null) {
				String definitionName = definition.getName();
				String baseName = getOperationBaseName(definitionName, operationName);
				Collection<FragmentDefinition> requiredFragments = new HashSet<>();
				ctx.getDefinedSelections()
					.computeIfAbsent(baseName, x -> traverseSelections(definition.getSelectionSet(), allFragments, requiredFragments, ctx, typeName));
				ctx.getDefinedOperations()
					.computeIfAbsent(baseName, x -> new GqlOperation(definitionName, operationName, typeName + "Result1",
						getDocumentString(definition, requiredFragments, ctx.getLog())))
					.addVariables(definition.getVariableDefinitions().stream().map(fromVariableDef(ctx)).collect(toSet()));
			}
		});
	}

	private String getOperationBaseName(String definitionName, String operationName) {
		return (definitionName == null ? "Unnamed" : Util.firstUpper(definitionName)) + Util.firstUpper(operationName);
	}

	private Map<String, Collection<GqlSelection>> traverseSelections(
		SelectionSet selectionSet,
		Collection<FragmentDefinition> allFragments,
		Collection<FragmentDefinition> requiredFragments,
		GqlContext ctx,
		String typeName
	) {
		Map<String, Map<Integer, Set<GqlSelection>>> typeMap = new HashMap<>();
		Map<String, AtomicInteger> counters = new HashMap<>();
		Queue<GqlSelection> selectionsToResolve = new LinkedList<>();
		selectionsToResolve.offer(new GqlSelection(new GqlField(null, GqlType.named(typeName)), "", "").addSubset(selectionSet));
		while (!selectionsToResolve.isEmpty()) {
			val currentSelectionSet = selectionsToResolve.poll();
			// get a set of selections by type name and their unresolved subset
			String currentTypeName = currentSelectionSet.getType().getInner();
			Set<GqlSelection> subSelections = new HashSet<>();
			Set<GqlSelection> subSelectionsToResolve = new LinkedHashSet<>();
			currentSelectionSet.getSubsets().forEach(unresolvedSet ->
				subSelections.addAll(resolveOneLevel(unresolvedSet,
					allFragments, requiredFragments, new HashSet<>(), ctx, currentTypeName, subSelectionsToResolve)));
			// find exactly the same selection set already linked to this type
			val selectionsByTypeName = typeMap.computeIfAbsent(currentTypeName, x -> new HashMap<>());
			int ordinalNumber = selectionsByTypeName.entrySet().stream()
				.filter(entry -> entry.getValue().size() == subSelections.size()
					&& entry.getValue().stream().allMatch(selection -> subSelections.stream().anyMatch(selection::equalsWithSubsets)))
				.map(Map.Entry::getKey)
				.findAny()
				.orElseGet(() -> {
					// this selection set has not been linked to this type before
					int key = counters.computeIfAbsent(currentTypeName, x -> new AtomicInteger()).incrementAndGet();
					selectionsByTypeName.put(key, subSelections);
					subSelectionsToResolve.forEach(selectionsToResolve::offer);
					return key;
				});
			// link previous selection to the type name
			currentSelectionSet.setTargetTypeName(currentTypeName + "Result" + ordinalNumber);
		}
		Map<String, Collection<GqlSelection>> result = new HashMap<>();
		typeMap.forEach((type, variants) -> variants.forEach((variantNumber, selections) -> result.put(type + "Result" + variantNumber, selections)));
		return result;
	}

	private Set<GqlSelection> resolveOneLevel(
		SelectionSet selectionSet,
		Collection<FragmentDefinition> allFragments,
		Collection<FragmentDefinition> requiredFragments,
		Collection<FragmentDefinition> visitedFragments,
		GqlContext ctx,
		String typeName,
		Set<GqlSelection> remaining
	) {
		Map<GqlSelection, GqlSelection> result = new HashMap<>();
		// fields declared explicitly
		selectionSet.getSelectionsOfType(Field.class).stream()
			.map(field -> {
				GqlField gqlField = new GqlField(field.getName(), guessTypeOfField(field, ctx, typeName));
				String alias = Optional.ofNullable(field.getAlias()).orElse("");
				return new GqlSelection(gqlField, alias, "").addSubset(field.getSelectionSet());
			})
			.forEach(newSelection -> {
				GqlSelection selection = mergeSubsets(newSelection, result);
				if (!selection.getSubsets().isEmpty()) {
					remaining.add(selection);
				}
			});
		// fields reachable via inline fragments
		selectionSet.getSelectionsOfType(InlineFragment.class).stream()
			.map(fragment -> resolveOneLevel(fragment.getSelectionSet(),
				allFragments, requiredFragments, visitedFragments, ctx, fragment.getTypeCondition().getName(), remaining))
			.flatMap(Collection::stream)
			.forEach(selection -> mergeSubsets(selection, result));
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
			.peek(requiredFragments::add)
			.map(fragment -> resolveOneLevel(fragment.getSelectionSet(),
				allFragments, requiredFragments, visitedFragments, ctx, fragment.getTypeCondition().getName(), remaining))
			.flatMap(Collection::stream)
			.forEach(selection -> mergeSubsets(selection, result));
		return result.keySet();
	}

	private static GqlSelection mergeSubsets(GqlSelection newSelection, Map<GqlSelection, GqlSelection> result) {
		GqlSelection oldSelection = result.get(newSelection);
		if (oldSelection == null) {
			result.put(newSelection, newSelection);
			return newSelection;
		}
		return oldSelection.addSubsets(newSelection.getSubsets());
	}

	private static GqlType guessTypeOfField(Field field, GqlContext ctx, String containerTypeName) {
		return Stream.concat(
				Optional.ofNullable(ctx.getObjectTypes().get(containerTypeName))
					.map(GqlStructure::getFields)
					.map(Collection::stream)
					.orElseGet(Stream::empty),
				Optional.ofNullable(ctx.getInterfaceTypes().get(containerTypeName))
					.map(GqlStructure::getFields)
					.map(Collection::stream)
					.orElseGet(Stream::empty))
			.filter(candidate -> Objects.equals(field.getName(), candidate.getName()))
			.map(GqlField::getType)
			.findAny()
			.orElseGet(() -> GqlType.named("String"));
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
			if (typeStructure.getMembers().contains(candidateType)) {
				candidateTypes.add(typeStructure.getName());
			}
			if (typeStructure.getMembers().contains(selectionType)) {
				selectionTypes.add(typeStructure.getName());
			}
		});
		return !Collections.disjoint(selectionTypes, candidateTypes);
	}

	private String getDocumentString(OperationDefinition operation, Collection<FragmentDefinition> fragments, Log log) {
		try (StringWriter writer = new StringWriter()) {
			CFG.setSharedVariable(OPERATION_KEY, operation);
			CFG.setSharedVariable(FRAGMENTS_KEY, fragments);
			CFG.getTemplate(OPERATION_DOCUMENT).process(null, writer);
			return writer.toString();
		} catch (TemplateException | IOException e) {
			log.warn(String.format("Operation document [%s] is not created.", operation.getName()), e);
			return null;
		} finally {
			CFG.clearSharedVariables();
		}
	}

}
