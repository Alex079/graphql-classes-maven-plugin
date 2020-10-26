package com.github.alme.graphql.generator.translator;

import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.InputValueDefinition.newInputValueDefinition;
import static graphql.language.ListType.newListType;
import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static graphql.language.ObjectTypeExtensionDefinition.newObjectTypeExtensionDefinition;
import static graphql.language.TypeName.newTypeName;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collection;

import com.github.alme.graphql.generator.dto.Context;
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
		Context ctx = new Context(log);

		translator.translate(doc, ctx);

		assertTrue(ctx.getInterfaceTypes().isEmpty());
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
		Context ctx = new Context(log);

		translator.translate(doc, ctx);

		assertEquals(1, ctx.getObjectTypes().size());
		assertTrue(ctx.getObjectTypes().containsKey("Object1"));
		Collection<GqlField> object1 = ctx.getObjectTypes().get("Object1").getFields();
		assertEquals(2, object1.size());
		object1.forEach(field -> {
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
		Context ctx = new Context(log);

		translator.translate(doc, ctx);

		assertEquals(1, ctx.getObjectTypes().size());
		assertTrue(ctx.getObjectTypes().containsKey("Object1"));
		Collection<GqlField> object1 = ctx.getObjectTypes().get("Object1").getFields();
		assertEquals(2, object1.size());
		object1.forEach(field -> {
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
}