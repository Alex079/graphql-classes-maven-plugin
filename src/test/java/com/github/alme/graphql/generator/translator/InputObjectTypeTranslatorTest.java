package com.github.alme.graphql.generator.translator;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import static graphql.language.ArrayValue.newArrayValue;
import static graphql.language.EnumValue.newEnumValue;
import static graphql.language.InputObjectTypeDefinition.newInputObjectDefinition;
import static graphql.language.InputObjectTypeExtensionDefinition.newInputObjectTypeExtensionDefinition;
import static graphql.language.InputValueDefinition.newInputValueDefinition;
import static graphql.language.ListType.newListType;
import static graphql.language.NonNullType.newNonNullType;
import static graphql.language.ObjectField.newObjectField;
import static graphql.language.ObjectValue.newObjectValue;
import static graphql.language.StringValue.newStringValue;
import static graphql.language.TypeName.newTypeName;

import java.util.Collection;

import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.dto.GqlField;
import com.github.alme.graphql.generator.dto.GqlType;

import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import graphql.language.Document;
import graphql.language.InputObjectTypeDefinition;

@ExtendWith(MockitoExtension.class)
class InputObjectTypeTranslatorTest {

	@Mock
	private Document doc;

	@Mock
	private Log log;

	private final InputObjectTypeTranslator translator = new InputObjectTypeTranslator();

	@Test
	void translateNoInputObjectTypes() {
		when(doc.getDefinitionsOfType(InputObjectTypeDefinition.class)).thenReturn(emptyList());
		GqlContext ctx = new GqlContext(log, emptyMap());

		translator.translate(doc, ctx);

		assertTrue(ctx.getInterfaceTypes().isEmpty());
	}

	@Test
	void translateOneInputObjectTypeWithTwoFields() {
		when(doc.getDefinitionsOfType(InputObjectTypeDefinition.class)).thenReturn(singletonList(
			newInputObjectDefinition()
				.name("Input1")
				.inputValueDefinition(newInputValueDefinition()
					.name("field1")
					.type(newTypeName("Enum1").build())
					.defaultValue(newEnumValue("V1").build())
					.build())
				.inputValueDefinition(newInputValueDefinition()
					.name("field2")
					.type(newNonNullType(newListType(newNonNullType(newTypeName("Type2").build()).build()).build()).build())
					.defaultValue(newArrayValue().value(newObjectValue().objectField(
						newObjectField().name("n").value(newStringValue("v").build()).build()).build()).build())
					.build())
				.build()));
		GqlContext ctx = new GqlContext(log, emptyMap());

		translator.translate(doc, ctx);

		assertEquals(1, ctx.getObjectTypes().size());
		assertTrue(ctx.getObjectTypes().containsKey("Input1"));
		Collection<GqlField> input1 = ctx.getObjectTypes().get("Input1").getFields();
		assertEquals(2, input1.size());
		input1.forEach(gqlField -> {
			switch (gqlField.getName()) {
				case "field1":
					assertEquals(GqlType.Flag.NAMED, gqlField.getType().getFlag());
					assertEquals("Enum1", gqlField.getType().getName());
					break;
				case "field2":
					assertEquals(GqlType.Flag.MANDATORY, gqlField.getType().getFlag());
					assertEquals(GqlType.Flag.LIST, gqlField.getType().getNested().getFlag());
					assertEquals(GqlType.Flag.MANDATORY, gqlField.getType().getNested().getNested().getFlag());
					assertEquals(GqlType.Flag.NAMED, gqlField.getType().getNested().getNested().getNested().getFlag());
					assertEquals("Type2", gqlField.getType().getNested().getNested().getNested().getName());
					break;
			}
		});
	}

	@Test
	void translateOneInputObjectTypeWithTwoFieldsWithExtension() {
		when(doc.getDefinitionsOfType(InputObjectTypeDefinition.class)).thenReturn(asList(
			newInputObjectDefinition()
				.name("Input1")
				.inputValueDefinition(newInputValueDefinition()
					.name("field1")
					.type(newTypeName("Enum1").build())
					.defaultValue(newEnumValue("V1").build())
					.build())
				.build(),
			newInputObjectTypeExtensionDefinition()
				.name("Input1")
				.inputValueDefinitions(singletonList(newInputValueDefinition()
					.name("field2")
					.type(newNonNullType(newListType(newNonNullType(newTypeName("Type2").build()).build()).build()).build())
					.defaultValue(newArrayValue().value(newObjectValue().objectField(
						newObjectField().name("n").value(newStringValue("v").build()).build()).build()).build())
					.build()))
				.build()));
		GqlContext ctx = new GqlContext(log, emptyMap());

		translator.translate(doc, ctx);

		assertEquals(1, ctx.getObjectTypes().size());
		assertTrue(ctx.getObjectTypes().containsKey("Input1"));
		Collection<GqlField> input1 = ctx.getObjectTypes().get("Input1").getFields();
		assertEquals(2, input1.size());
		input1.forEach(gqlField -> {
			switch (gqlField.getName()) {
				case "field1":
					assertEquals(GqlType.Flag.NAMED, gqlField.getType().getFlag());
					assertEquals("Enum1", gqlField.getType().getName());
					break;
				case "field2":
					assertEquals(GqlType.Flag.MANDATORY, gqlField.getType().getFlag());
					assertEquals(GqlType.Flag.LIST, gqlField.getType().getNested().getFlag());
					assertEquals(GqlType.Flag.MANDATORY, gqlField.getType().getNested().getNested().getFlag());
					assertEquals(GqlType.Flag.NAMED, gqlField.getType().getNested().getNested().getNested().getFlag());
					assertEquals("Type2", gqlField.getType().getNested().getNested().getNested().getName());
					break;
			}
		});
	}
}