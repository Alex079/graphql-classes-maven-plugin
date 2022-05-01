package com.github.alme.graphql.generator.translator;

import static java.util.stream.Collectors.toSet;

import static com.github.alme.graphql.generator.translator.Util.createFullSelection;
import static com.github.alme.graphql.generator.translator.Util.fromVariableDef;
import static com.github.alme.graphql.generator.translator.Util.translateSelection;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.dto.GqlOperation;
import com.github.alme.graphql.generator.dto.GqlSelection;

import org.apache.maven.plugin.logging.Log;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import graphql.language.Document;
import graphql.language.FragmentDefinition;
import graphql.language.OperationDefinition;

public class OperationTranslator implements Translator {

	private static final String FRAGMENTS_KEY = "fragments";
	private static final String OPERATIONS_KEY = "operations";
	private static final String OPERATION_DOCUMENT = "OPERATION_DOCUMENT";
	private static final Configuration CFG = new Configuration(Configuration.VERSION_2_3_31);

	static {
		CFG.setClassLoaderForTemplateLoading(OperationTranslator.class.getClassLoader(), "/templates/text");
		CFG.setDefaultEncoding("UTF-8");
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
		populateFromSchema(ctx);
	}

	private void populate(GqlContext ctx, Collection<OperationDefinition> definitions, Collection<FragmentDefinition> allFragments) {
		definitions.forEach(definition -> {
			String operation = definition.getOperation().name().toLowerCase();
			String typeName = ctx.getSchema().get(operation);
			if (typeName != null) {
				Collection<FragmentDefinition> requiredFragments = new HashSet<>();
				Collection<GqlSelection> selections = translateSelection(definition.getSelectionSet(), allFragments, requiredFragments, ctx, typeName);
				String documentString = getDocumentString(definition, requiredFragments, ctx.getLog());
				ctx.getDefinedOperations()
					.computeIfAbsent(definition.getName(), name -> new GqlOperation(name, operation, typeName, documentString))
					.addSelections(selections)
					.addVariables(definition.getVariableDefinitions().stream().map(fromVariableDef(ctx)).collect(toSet()));
			}
		});
	}

	private String getDocumentString(OperationDefinition operation, Collection<FragmentDefinition> fragments, Log log) {
		Map<String, Collection<?>> input = new HashMap<>();
		input.put(OPERATIONS_KEY, Collections.singletonList(operation));
		input.put(FRAGMENTS_KEY, fragments);
		try (StringWriter writer = new StringWriter()) {
			CFG.getTemplate(OPERATION_DOCUMENT).process(input, writer);
			return writer.toString();
		} catch (TemplateException | IOException e) {
			log.warn(String.format("Operation document [%s] is not created.", operation.getName()), e);
			return null;
		}
	}

	private void populateFromSchema(GqlContext ctx) {
		ctx.getSchema().forEach((operation, typeName) ->
			ctx.getDynamicOperations().add(
				new GqlOperation(null, operation, typeName, null)
					.addSelections(createFullSelection(ctx, typeName)))
		);
	}

}
