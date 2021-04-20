package com.github.alme.graphql.generator.io;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Objects;

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
import graphql.parser.MultiSourceReader;
import graphql.parser.Parser;
import lombok.Value;

public class GqlReader {

	private static final String LOG_PARSER = "Parser has encountered %d definition(s).";
	private static final String LOG_TRANSLATOR = "Finished translating %d %s definition(s).";

	@Value
	static class FileInfo {
		Reader reader;
		String path;
	}

	public void read(GqlContext context, Collection<File> sources) {
		try (Reader reader = getReader(sources, context.getLog())) {
			Document doc = new Parser().parseDocument(reader);
			context.getLog().info(format(LOG_PARSER, doc.getDefinitions().size()));

			new EnumTypeTranslator().translate(doc, context);
			context.getLog().info(format(LOG_TRANSLATOR, context.getEnumTypes().size(), "Enum type"));

			new InterfaceTypeTranslator().translate(doc, context);
			context.getLog().info(format(LOG_TRANSLATOR, context.getInterfaceTypes().size(), "Interface type"));

			new InputObjectTypeTranslator().translate(doc, context);
			new ObjectTypeTranslator().translate(doc, context);
			new RelayConnectionTranslator().translate(doc, context);
			context.getLog().info(format(LOG_TRANSLATOR, context.getObjectTypes().size(), "Object and Input Object type"));

			new UnionTypeTranslator().translate(doc, context);
			context.getLog().info(format(LOG_TRANSLATOR, context.getUnionTypes().size(), "Union type"));

			new SchemaTranslator().translate(doc, context);
			new OperationTranslator().translate(doc, context);
			context.getLog().info(format(LOG_TRANSLATOR, context.getOperations().size(), "Operation"));

		} catch (IOException e) {
			context.getLog().error(e);
		}

	}

	private Reader getReader(Collection<File> sources, Log log) {
		return sources.stream()
			.map(File::toPath)
			.map(path -> {
				try {
					return new FileInfo(Files.newBufferedReader(path), path.toString());
				} catch (IOException e) {
					log.error(format("Skipping [%s] due to error.", path), e);
					return null;
				}
			})
			.filter(Objects::nonNull)
			.reduce(
				MultiSourceReader.newMultiSourceReader(),
				(multiReader, pair) -> multiReader.reader(pair.reader, pair.path),
				(r1, r2) -> null /* unused */)
			.build();
	}

}
