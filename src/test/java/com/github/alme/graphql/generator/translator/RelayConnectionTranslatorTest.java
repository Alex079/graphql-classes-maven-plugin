package com.github.alme.graphql.generator.translator;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import static graphql.language.Argument.newArgument;
import static graphql.language.Directive.newDirective;
import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.InputValueDefinition.newInputValueDefinition;
import static graphql.language.ListType.newListType;
import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static graphql.language.ObjectTypeExtensionDefinition.newObjectTypeExtensionDefinition;
import static graphql.language.StringValue.newStringValue;
import static graphql.language.TypeName.newTypeName;

import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.dto.GqlField;
import com.github.alme.graphql.generator.dto.GqlType;

import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import graphql.language.Document;
import graphql.language.ObjectTypeDefinition;
import graphql.language.SourceLocation;

@ExtendWith(MockitoExtension.class)
class RelayConnectionTranslatorTest {

	@Mock
	private Document doc;

	@Mock
	private Log log;

	private final RelayConnectionTranslator translator = new RelayConnectionTranslator();

	@Test
	void translateNoObjectTypes() {
		when(doc.getDefinitionsOfType(ObjectTypeDefinition.class)).thenReturn(emptyList());
		GqlContext ctx = new GqlContext(log, emptyMap());

		translator.translate(doc, ctx);

		assertThat(ctx.getObjectTypes()).isEmpty();
	}

	@Test
	void translateOneObjectTypeWithoutDirective() {
		when(doc.getDefinitionsOfType(ObjectTypeDefinition.class)).thenReturn(singletonList(
			newObjectTypeDefinition()
				.name("Object1")
				.fieldDefinition(newFieldDefinition()
					.name("field1")
					.type(newTypeName("Type1").build())
					.inputValueDefinition(newInputValueDefinition()
						.name("p1")
						.type(newTypeName("Type2").build())
						.build())
					.build())
				.build()));
		GqlContext ctx = new GqlContext(log, emptyMap());

		translator.translate(doc, ctx);

		assertThat(ctx.getObjectTypes()).isEmpty();
	}

	@Test
	void translateOneObjectTypeWithDirective() {
		when(doc.getDefinitionsOfType(ObjectTypeDefinition.class)).thenReturn(singletonList(
			newObjectTypeDefinition()
				.name("Object1")
				.fieldDefinition(newFieldDefinition()
					.name("field1")
					.directive(newDirective()
						.name("connection")
						.argument(newArgument("for", newStringValue("Type8").build()).build())
						.sourceLocation(new SourceLocation(0, 0))
						.build())
					.type(newTypeName("Type8Connection").build())
					.inputValueDefinition(newInputValueDefinition()
						.name("p1")
						.type(newTypeName("Type2").build())
						.build())
					.build())
				.build()));
		GqlContext ctx = new GqlContext(log, emptyMap());

		translator.translate(doc, ctx);

		assertThat(ctx.getObjectTypes()).containsOnlyKeys("Type8Connection", "Type8ConnectionEdge", "PageInfo");
		assertThat(ctx.getObjectTypes().get("Type8Connection").getFields()).containsOnly(
			new GqlField("edges", GqlType.list(GqlType.named("Type8ConnectionEdge"))),
			new GqlField("pageInfo", GqlType.named("PageInfo"))
		);
		assertThat(ctx.getObjectTypes().get("Type8ConnectionEdge").getFields()).containsOnly(
			new GqlField("node", GqlType.named("Type8")),
			new GqlField("cursor", GqlType.named("String"))
		);
		assertThat(ctx.getObjectTypes().get("PageInfo").getFields()).containsOnly(
			new GqlField("startCursor", GqlType.named("String")),
			new GqlField("endCursor", GqlType.named("String")),
			new GqlField("hasNextPage", GqlType.mandatory(GqlType.named("Boolean"))),
			new GqlField("hasPreviousPage", GqlType.mandatory(GqlType.named("Boolean")))
		);
	}

	@Test
	void translateOneObjectTypeWithExtensionWithDirective() {
		when(doc.getDefinitionsOfType(ObjectTypeDefinition.class)).thenReturn(asList(
			newObjectTypeDefinition()
				.name("Object1")
				.fieldDefinition(newFieldDefinition()
					.name("field2")
					.type(newListType(newTypeName("Type3").build()).build())
					.build())
				.build(),
			newObjectTypeExtensionDefinition()
				.name("Object1")
				.fieldDefinition(newFieldDefinition()
					.name("field1")
					.type(newTypeName("Type8Connection").build())
					.directive(newDirective()
						.name("connection")
						.argument(newArgument("for", newStringValue("Type8").build()).build())
						.sourceLocation(new SourceLocation(0, 0))
						.build())
					.inputValueDefinition(newInputValueDefinition()
						.name("p1")
						.type(newTypeName("Type2").build())
						.build())
					.build())
				.build()));
		GqlContext ctx = new GqlContext(log, emptyMap());

		translator.translate(doc, ctx);

		assertThat(ctx.getObjectTypes()).containsOnlyKeys("Type8Connection", "Type8ConnectionEdge", "PageInfo");
		assertThat(ctx.getObjectTypes().get("Type8Connection").getFields()).containsOnly(
			new GqlField("edges", GqlType.list(GqlType.named("Type8ConnectionEdge"))),
			new GqlField("pageInfo", GqlType.named("PageInfo"))
		);
		assertThat(ctx.getObjectTypes().get("Type8ConnectionEdge").getFields()).containsOnly(
			new GqlField("node", GqlType.named("Type8")),
			new GqlField("cursor", GqlType.named("String"))
		);
		assertThat(ctx.getObjectTypes().get("PageInfo").getFields()).containsOnly(
			new GqlField("startCursor", GqlType.named("String")),
			new GqlField("endCursor", GqlType.named("String")),
			new GqlField("hasNextPage", GqlType.mandatory(GqlType.named("Boolean"))),
			new GqlField("hasPreviousPage", GqlType.mandatory(GqlType.named("Boolean")))
		);
	}
}