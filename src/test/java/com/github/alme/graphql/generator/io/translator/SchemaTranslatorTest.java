package com.github.alme.graphql.generator.io.translator;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import static graphql.language.OperationTypeDefinition.newOperationTypeDefinition;
import static graphql.language.SchemaDefinition.newSchemaDefinition;
import static graphql.language.SchemaExtensionDefinition.newSchemaExtensionDefinition;
import static graphql.language.TypeName.newTypeName;

import com.github.alme.graphql.generator.dto.GqlContext;

import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import graphql.language.Description;
import graphql.language.Document;
import graphql.language.SchemaDefinition;

@ExtendWith(MockitoExtension.class)
class SchemaTranslatorTest {

	@Mock
	private Document doc;

	@Mock
	private Log log;

	private final SchemaTranslator translator = new SchemaTranslator();

	@Test
	void translateEmptySchema() {
		when(doc.getDefinitionsOfType(SchemaDefinition.class)).thenReturn(emptyList());
		GqlContext ctx = new GqlContext(log, emptyMap(), emptyMap());

		translator.translate(doc, ctx);

		assertThat(ctx.getOperations()).isEmpty();
		assertThat(ctx.getSchemaJavadoc()).isEmpty();
	}

	@Test
	void translateSchemaWithTwoOperations() {
		when(doc.getDefinitionsOfType(SchemaDefinition.class)).thenReturn(singletonList(
			newSchemaDefinition()
				.operationTypeDefinition(newOperationTypeDefinition()
					.name("query")
					.typeName(newTypeName("Type1").build())
					.build())
				.operationTypeDefinition(newOperationTypeDefinition()
					.name("mutation")
					.typeName(newTypeName("Type2").build())
					.build())
				.description(new Description("comment", null, true))
				.build()));
		GqlContext ctx = new GqlContext(log, emptyMap(), emptyMap());

		translator.translate(doc, ctx);

		assertThat(ctx)
			.satisfies(schema -> assertThat(schema.getOperations())
				.hasSize(2)
				.containsEntry("query", "Type1")
				.containsEntry("mutation", "Type2"))
			.satisfies(schema -> assertThat(schema.getSchemaJavadoc())
				.containsExactly("<p>comment</p>"));
	}

	@Test
	void translateSchemaWithTwoOperationsWithExtension() {
		when(doc.getDefinitionsOfType(SchemaDefinition.class)).thenReturn(asList(
			newSchemaDefinition()
				.operationTypeDefinition(newOperationTypeDefinition()
					.name("query")
					.typeName(newTypeName("Type1").build())
					.build())
				.build(),
			newSchemaExtensionDefinition()
				.operationTypeDefinition(newOperationTypeDefinition()
					.name("mutation")
					.typeName(newTypeName("Type2").build())
					.build())
				.build()));
		GqlContext ctx = new GqlContext(log, emptyMap(), emptyMap());

		translator.translate(doc, ctx);

		assertThat(ctx)
			.satisfies(schema -> assertThat(schema.getOperations())
				.hasSize(2)
				.containsEntry("query", "Type1")
				.containsEntry("mutation", "Type2"))
			.satisfies(schema -> assertThat(schema.getSchemaJavadoc())
				.isEmpty());
	}
}