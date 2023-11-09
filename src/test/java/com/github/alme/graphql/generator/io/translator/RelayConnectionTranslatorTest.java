package com.github.alme.graphql.generator.io.translator;

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
import static graphql.language.InterfaceTypeDefinition.newInterfaceTypeDefinition;
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
import graphql.language.SourceLocation;

@ExtendWith(MockitoExtension.class)
class RelayConnectionTranslatorTest {

	@Mock
	private Document doc;

	@Mock
	private Log log;

	private final RelayConnectionTranslator translator = new RelayConnectionTranslator();

	@Test
	void skipWhenNoObjectTypes() {
		when(doc.getDefinitions()).thenReturn(emptyList());
		GqlContext ctx = new GqlContext(log, emptyMap(), emptyMap());

		translator.translate(doc, ctx);

		assertThat(ctx.getObjectTypes()).isEmpty();
	}

	@Test
	void skipWhenDirectiveIsOnInterface() {
		when(doc.getDefinitions()).thenReturn(singletonList(
			newInterfaceTypeDefinition()
				.name("Interface1")
				.definition(newFieldDefinition()
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
		GqlContext ctx = new GqlContext(log, emptyMap(), emptyMap());

		translator.translate(doc, ctx);

		assertThat(ctx.getObjectTypes()).isEmpty();
	}

	@Test
	void skipWhenOneObjectTypeWithoutDirective() {
		when(doc.getDefinitions()).thenReturn(singletonList(
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
		GqlContext ctx = new GqlContext(log, emptyMap(), emptyMap());

		translator.translate(doc, ctx);

		assertThat(ctx.getObjectTypes()).isEmpty();
	}

	@Test
	void translateOneObjectTypeWithDirective() {
		when(doc.getDefinitions()).thenReturn(singletonList(
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
		GqlContext ctx = new GqlContext(log, emptyMap(), emptyMap());

		translator.translate(doc, ctx);

		assertThat(ctx.getObjectTypes()).containsOnlyKeys("Type8Connection", "Type8ConnectionEdge", "PageInfo");
		assertThat(ctx.getObjectTypes().get("Type8Connection").getFields()).containsOnly(
			GqlField.of("edges", GqlType.list(GqlType.named("Type8ConnectionEdge"))),
			GqlField.of("pageInfo", GqlType.named("PageInfo"))
		);
		assertThat(ctx.getObjectTypes().get("Type8ConnectionEdge").getFields()).containsOnly(
			GqlField.of("node", GqlType.named("Type8")),
			GqlField.of("cursor", GqlType.named("String"))
		);
		assertThat(ctx.getObjectTypes().get("PageInfo").getFields()).containsOnly(
			GqlField.of("startCursor", GqlType.named("String")),
			GqlField.of("endCursor", GqlType.named("String")),
			GqlField.of("hasNextPage", GqlType.mandatory(GqlType.named("Boolean"))),
			GqlField.of("hasPreviousPage", GqlType.mandatory(GqlType.named("Boolean")))
		);
	}

	@Test
	void translateOneObjectTypeWithExtensionWithDirective() {
		when(doc.getDefinitions()).thenReturn(asList(
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
		GqlContext ctx = new GqlContext(log, emptyMap(), emptyMap());

		translator.translate(doc, ctx);

		assertThat(ctx.getObjectTypes()).containsOnlyKeys("Type8Connection", "Type8ConnectionEdge", "PageInfo");
		assertThat(ctx.getObjectTypes().get("Type8Connection").getFields()).containsOnly(
			GqlField.of("edges", GqlType.list(GqlType.named("Type8ConnectionEdge"))),
			GqlField.of("pageInfo", GqlType.named("PageInfo"))
		);
		assertThat(ctx.getObjectTypes().get("Type8ConnectionEdge").getFields()).containsOnly(
			GqlField.of("node", GqlType.named("Type8")),
			GqlField.of("cursor", GqlType.named("String"))
		);
		assertThat(ctx.getObjectTypes().get("PageInfo").getFields()).containsOnly(
			GqlField.of("startCursor", GqlType.named("String")),
			GqlField.of("endCursor", GqlType.named("String")),
			GqlField.of("hasNextPage", GqlType.mandatory(GqlType.named("Boolean"))),
			GqlField.of("hasPreviousPage", GqlType.mandatory(GqlType.named("Boolean")))
		);
	}
}