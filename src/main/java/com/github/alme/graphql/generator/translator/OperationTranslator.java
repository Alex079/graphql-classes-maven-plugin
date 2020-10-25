package com.github.alme.graphql.generator.translator;

import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.alme.graphql.generator.dto.Context;
import com.github.alme.graphql.generator.dto.GqlOperation;
import com.github.alme.graphql.generator.dto.GqlType;

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
	private static final Configuration CFG = new Configuration(Configuration.VERSION_2_3_30);

	static {
		CFG.setClassLoaderForTemplateLoading(OperationTranslator.class.getClassLoader(), "/templates/text");
		CFG.setDefaultEncoding("UTF-8");
		CFG.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		CFG.setLogTemplateExceptions(false);
		CFG.setWrapUncheckedExceptions(true);
		CFG.setFallbackOnNullLoopVariable(false);
	}

	@Override
	public void translate(Document doc, Context ctx) {
		Collection<OperationDefinition> operations = doc.getDefinitionsOfType(OperationDefinition.class);
		populate(doc, ctx, operations);
	}

	private void populate(Document doc, Context ctx, Collection<OperationDefinition> definitions) {
		List<FragmentDefinition> fragmentDefs = doc.getDefinitionsOfType(FragmentDefinition.class);
		definitions.forEach((opDef) -> {
			String operation = opDef.getOperation().name().toLowerCase();
			String typeName = ctx.getSchema().get(operation);
			if (typeName != null) {
				ctx.getOperations()
					.computeIfAbsent(opDef.getName(), (name) -> new GqlOperation(name, operation, GqlType.named(typeName)))
					.setText(getDocumentString(opDef, fragmentDefs, ctx.getLog()))
					.addSelection(Util.translateSelection(opDef, doc, ctx, typeName))
					.addVariables(
						opDef.getVariableDefinitions().stream()
							.map(Util.fromVariableDef(doc, ctx))
							.collect(toSet()));
			}
		});
	}

	private String getDocumentString(OperationDefinition opDef, List<FragmentDefinition> fragmentDefs, Log log) {
		try {
			Map<String, List<?>> input = new HashMap<>();
			input.put(OPERATIONS_KEY, Collections.singletonList(opDef));
			input.put(FRAGMENTS_KEY, fragmentDefs);
			StringWriter out = new StringWriter();
			CFG.getTemplate(OPERATION_DOCUMENT).process(input, new PrintWriter(out));
			return out.toString();
		} catch (TemplateException | IOException e) {
			log.warn(String.format("Operation document [%s] is not created.", opDef.getName()), e);
			return null;
		}
	}

}
