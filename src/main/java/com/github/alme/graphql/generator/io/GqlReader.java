package com.github.alme.graphql.generator.io;

import static java.lang.String.format;

import java.io.IOException;
import java.io.Reader;

import com.github.alme.graphql.generator.dto.GqlConfiguration;
import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.io.translator.DynamicOperationTranslator;
import com.github.alme.graphql.generator.io.translator.EnumTypeTranslator;
import com.github.alme.graphql.generator.io.translator.InputObjectTypeTranslator;
import com.github.alme.graphql.generator.io.translator.InterfaceTypeTranslator;
import com.github.alme.graphql.generator.io.translator.ObjectTypeTranslator;
import com.github.alme.graphql.generator.io.translator.OperationTranslator;
import com.github.alme.graphql.generator.io.translator.RelayConnectionTranslator;
import com.github.alme.graphql.generator.io.translator.SchemaTranslator;
import com.github.alme.graphql.generator.io.translator.UnionTypeTranslator;

import org.apache.maven.plugin.logging.Log;

import graphql.language.Document;
import graphql.parser.Parser;
import graphql.parser.ParserEnvironment;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GqlReader {

	private static final String LOG_PARSER = "Parser has encountered %d definition(s).";
	private static final String LOG_TRANSLATOR = "Finished translating %d %s definition(s).";

	private final ReaderFactory readerFactory;

	public void read(GqlContext context, GqlConfiguration configuration) {
		Log log = context.getLog();
		try (Reader reader = readerFactory.getReader())
		{
			ParserEnvironment environment = ParserEnvironment.newParserEnvironment()
				.document(reader)
				.parserOptions(configuration.getParserOptions())
				.build();
			Document doc = Parser.parse(environment);
			log.info(format(LOG_PARSER, doc.getDefinitions().size()));

			new InputObjectTypeTranslator().translate(doc, context);
			log.info(format(LOG_TRANSLATOR, context.getInputObjectTypes().size(), "Input Object type"));

			new EnumTypeTranslator().translate(doc, context);
			log.info(format(LOG_TRANSLATOR, context.getEnumTypes().size(), "Enum type"));

			new InterfaceTypeTranslator().translate(doc, context);
			new UnionTypeTranslator().translate(doc, context);
			log.info(format(LOG_TRANSLATOR, context.getInterfaceTypes().size(), "Interface and union type"));

			new ObjectTypeTranslator().translate(doc, context);
			new RelayConnectionTranslator().translate(doc, context);
			log.info(format(LOG_TRANSLATOR, context.getObjectTypes().size(), "Object type"));

			boolean generateDefinedOperations = configuration.isGenerateDefinedOperations();
			boolean generateDynamicOperations = configuration.isGenerateDynamicOperations();

			if (generateDefinedOperations || generateDynamicOperations) {
				new SchemaTranslator().translate(doc, context);
				log.info(format(LOG_TRANSLATOR, context.getSchema().size(), "Schema"));
			}
			if (generateDefinedOperations) {
				new OperationTranslator().translate(doc, context);
				log.info(format(LOG_TRANSLATOR, context.getDefinedOperations().size(), "Defined operation"));
			}
			if (generateDynamicOperations) {
				new DynamicOperationTranslator().translate(doc, context);
				log.info(format(LOG_TRANSLATOR, context.getDynamicOperations().size(), "Dynamic operation"));
			}
		}
		catch (IOException e) {
			log.error(e);
		}
	}

}
