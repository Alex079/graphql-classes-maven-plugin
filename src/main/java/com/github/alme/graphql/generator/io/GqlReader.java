package com.github.alme.graphql.generator.io;

import static java.lang.String.format;

import java.io.IOException;
import java.io.Reader;

import com.github.alme.graphql.generator.dto.GqlConfiguration;
import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.translator.EnumTypeTranslator;
import com.github.alme.graphql.generator.translator.InputObjectTypeTranslator;
import com.github.alme.graphql.generator.translator.InterfaceTypeTranslator;
import com.github.alme.graphql.generator.translator.ObjectTypeTranslator;
import com.github.alme.graphql.generator.translator.OperationTranslator;
import com.github.alme.graphql.generator.translator.RelayConnectionTranslator;
import com.github.alme.graphql.generator.translator.SchemaTranslator;
import com.github.alme.graphql.generator.translator.UnionTypeTranslator;

import org.apache.maven.plugin.logging.Log;

import graphql.language.Document;
import graphql.parser.Parser;
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
			Document doc = new Parser().parseDocument(reader, configuration.getParserOptions());
			log.info(format(LOG_PARSER, doc.getDefinitions().size()));

			new EnumTypeTranslator().translate(doc, context);
			log.info(format(LOG_TRANSLATOR, context.getEnumTypes().size(), "Enum type"));

			new InterfaceTypeTranslator().translate(doc, context);
			log.info(format(LOG_TRANSLATOR, context.getInterfaceTypes().size(), "Interface type"));

			new InputObjectTypeTranslator().translate(doc, context);
			log.info(format(LOG_TRANSLATOR, context.getInputObjectTypes().size(), "Input Object type"));

			new ObjectTypeTranslator().translate(doc, context);
			new RelayConnectionTranslator().translate(doc, context);
			log.info(format(LOG_TRANSLATOR, context.getObjectTypes().size(), "Object type"));

			new UnionTypeTranslator().translate(doc, context);
			log.info(format(LOG_TRANSLATOR, context.getUnionTypes().size(), "Union type"));

			new SchemaTranslator().translate(doc, context);
			new OperationTranslator().translate(doc, context);
			log.info(format(LOG_TRANSLATOR, context.getDefinedOperations().size(), "Operation"));
		}
		catch (IOException e) {
			log.error(e);
		}
	}

}
