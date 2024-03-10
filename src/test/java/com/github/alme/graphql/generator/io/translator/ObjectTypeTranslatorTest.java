package com.github.alme.graphql.generator.io.translator;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import static graphql.language.EnumValue.newEnumValue;
import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.InputValueDefinition.newInputValueDefinition;
import static graphql.language.ListType.newListType;
import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static graphql.language.ObjectTypeExtensionDefinition.newObjectTypeExtensionDefinition;
import static graphql.language.TypeName.newTypeName;

import java.util.Collection;

import com.github.alme.graphql.generator.dto.GqlArgument;
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

@ExtendWith(MockitoExtension.class)
class ObjectTypeTranslatorTest {

	@Mock
	private Document doc;

	@Mock
	private Log log;

	private final ObjectTypeTranslator translator = new ObjectTypeTranslator();

	@Test
	void translateNoObjectTypes() {
		when(doc.getDefinitionsOfType(ObjectTypeDefinition.class)).thenReturn(emptyList());
		GqlContext ctx = new GqlContext(log, emptyMap(), emptyMap());

		translator.translate(doc, ctx);

		assertTrue(ctx.getObjectTypes().isEmpty());
	}

	@Test
	void translateOneObjectTypeWithTwoFields() {
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
						.fieldDefinition(newFieldDefinition()
								.name("field2")
								.type(newListType(newTypeName("Type3").build()).build())
								.build())
						.build()));
		GqlContext ctx = new GqlContext(log, emptyMap(), emptyMap());

		translator.translate(doc, ctx);

		assertEquals(1, ctx.getObjectTypes().size());
		assertTrue(ctx.getObjectTypes().containsKey("Object1"));
		Collection<GqlField> fields = ctx.getObjectTypes().get("Object1").getFields();
		assertEquals(2, fields.size());
		fields.forEach(field -> {
			GqlType type = field.getType();
			switch (field.getName()) {
				case "field1":
					assertEquals(GqlType.Flag.NAMED, type.getFlag());
					assertEquals("Type1", type.getName());
					break;
				case "field2":
					assertEquals(GqlType.Flag.NAMED, type.getNested().getFlag());
					assertEquals("Type3", type.getNested().getName());
					break;
			}
		});
	}

	@Test
	void translateOneObjectTypeWithNoFields() {
		when(doc.getDefinitionsOfType(ObjectTypeDefinition.class)).thenReturn(singletonList(
				newObjectTypeDefinition()
						.name("Object1")
						.build()));
		GqlContext ctx = new GqlContext(log, emptyMap(), emptyMap());

		translator.translate(doc, ctx);

		assertEquals(1, ctx.getObjectTypes().size());
		assertTrue(ctx.getObjectTypes().containsKey("Object1"));
		Collection<GqlField> fields = ctx.getObjectTypes().get("Object1").getFields();
		assertEquals(0, fields.size());
	}

	@Test
	void translateOneObjectTypeWithTwoFieldsWithExtension() {
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
								.type(newTypeName("Type1").build())
								.inputValueDefinition(newInputValueDefinition()
										.name("p1")
										.type(newTypeName("Type2").build())
										.build())
								.build())
						.build()));
		GqlContext ctx = new GqlContext(log, emptyMap(), emptyMap());

		translator.translate(doc, ctx);

		assertEquals(1, ctx.getObjectTypes().size());
		assertTrue(ctx.getObjectTypes().containsKey("Object1"));
		Collection<GqlField> fields = ctx.getObjectTypes().get("Object1").getFields();
		assertEquals(2, fields.size());
		fields.forEach(field -> {
			GqlType type = field.getType();
			switch (field.getName()) {
				case "field1":
					assertEquals(GqlType.Flag.NAMED, type.getFlag());
					assertEquals("Type1", type.getName());
					break;
				case "field2":
					assertEquals(GqlType.Flag.NAMED, type.getNested().getFlag());
					assertEquals("Type3", type.getNested().getName());
					break;
			}
		});
	}

	@Test
	void translateArguments() {
		when(doc.getDefinitionsOfType(ObjectTypeDefinition.class)).thenReturn(singletonList(
			newObjectTypeDefinition()
				.name("Object1")
				.fieldDefinition(newFieldDefinition()
					.name("field1")
					.type(newListType(newTypeName("Type1").build()).build())
					.inputValueDefinition(newInputValueDefinition()
						.name("arg1")
						.type(newTypeName("Enum1").build())
						.defaultValue(newEnumValue("V1").build())
						.build())
					.build())
				.build()));
		GqlContext ctx = new GqlContext(log, emptyMap(), emptyMap());

		translator.translate(doc, ctx);

		assertEquals(1, ctx.getObjectTypes().size());
		assertTrue(ctx.getObjectTypes().containsKey("Object1"));
		Collection<GqlField> fields = ctx.getObjectTypes().get("Object1").getFields();
		assertEquals(1, fields.size());
		GqlField field = fields.iterator().next();
		GqlType fieldType = field.getType();
		assertEquals("field1", field.getName());
		assertEquals(GqlType.Flag.LIST, fieldType.getFlag());
		assertEquals("Type1", fieldType.getNested().getName());
		assertEquals(1, field.getArguments().size());
		GqlArgument argument = field.getArguments().iterator().next();
		GqlType argumentType = argument.getType();
		assertEquals("arg1", argument.getName());
		assertEquals(GqlType.Flag.NAMED, argumentType.getFlag());
		assertEquals("Enum1", argumentType.getName());
	}
}