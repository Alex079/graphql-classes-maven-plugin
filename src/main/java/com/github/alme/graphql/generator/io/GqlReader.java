package com.github.alme.graphql.generator.io;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Objects;

import com.github.alme.graphql.generator.dto.Context;
import com.github.alme.graphql.generator.translator.EnumTypeTranslator;
import com.github.alme.graphql.generator.translator.InputObjectTypeTranslator;
import com.github.alme.graphql.generator.translator.InterfaceTypeTranslator;
import com.github.alme.graphql.generator.translator.ObjectTypeTranslator;
import com.github.alme.graphql.generator.translator.OperationTranslator;
import com.github.alme.graphql.generator.translator.SchemaTranslator;
import com.github.alme.graphql.generator.translator.UnionTypeTranslator;

import org.antlr.v4.runtime.misc.Pair;
import org.apache.maven.plugin.logging.Log;

import graphql.language.Document;
import graphql.parser.MultiSourceReader;
import graphql.parser.Parser;

public class GqlReader {

	private static final String LOG_PARSER = "Parser has encountered %d definition(s).";
	private static final String LOG_TRANSLATOR = "Finished translating %d %s definition(s).";

	public void read(Context ctx, Collection<File> sources) {
		try (Reader reader = getReader(sources, ctx.getLog())) {
			Document doc = new Parser().parseDocument(reader);
			ctx.getLog().info(String.format(LOG_PARSER, doc.getDefinitions().size()));

			new EnumTypeTranslator().translate(doc, ctx);
			ctx.getLog().info(String.format(LOG_TRANSLATOR, ctx.getEnumTypes().size(), "Enum type"));

			new InterfaceTypeTranslator().translate(doc, ctx);
			ctx.getLog().info(String.format(LOG_TRANSLATOR, ctx.getInterfaceTypes().size(), "Interface type"));

			new InputObjectTypeTranslator().translate(doc, ctx);
			new ObjectTypeTranslator().translate(doc, ctx);
			ctx.getLog().info(String.format(LOG_TRANSLATOR, ctx.getObjectTypes().size(), "Object and Input Object type"));

			new UnionTypeTranslator().translate(doc, ctx);
			ctx.getLog().info(String.format(LOG_TRANSLATOR, ctx.getUnionTypes().size(), "Union type"));

			new SchemaTranslator().translate(doc, ctx);
			new OperationTranslator().translate(doc, ctx);
			ctx.getLog().info(String.format(LOG_TRANSLATOR, ctx.getOperations().size(), "Operation"));

		} catch (IOException e) {
			ctx.getLog().error(e);
		}

	}

	private Reader getReader(Collection<File> sources, Log log) {
		return sources.stream()
			.map(File::toPath)
			.map((path) -> {
				try {
					return new Pair<>(Files.newBufferedReader(path), path.toString());
				} catch (IOException e) {
					log.error(String.format("Skipping [%s] due to error.", path), e);
					return null;
				}
			})
			.filter(Objects::nonNull)
			.reduce(
				MultiSourceReader.newMultiSourceReader(),
				(multiReader, pair) -> multiReader.reader(pair.a, pair.b),
				(r1, r2) -> null /* unused */)
			.build();
	}

}
