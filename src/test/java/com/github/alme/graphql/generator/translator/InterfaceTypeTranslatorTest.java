package com.github.alme.graphql.generator.translator;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.InterfaceTypeDefinition.newInterfaceTypeDefinition;
import static graphql.language.InterfaceTypeExtensionDefinition.newInterfaceTypeExtensionDefinition;
import static graphql.language.ListType.newListType;
import static graphql.language.NonNullType.newNonNullType;
import static graphql.language.TypeName.newTypeName;

import java.util.Collection;

import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.dto.GqlField;
import com.github.alme.graphql.generator.dto.GqlStructure;
import com.github.alme.graphql.generator.dto.GqlType;

import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import graphql.language.Document;
import graphql.language.InterfaceTypeDefinition;

@ExtendWith(MockitoExtension.class)
class InterfaceTypeTranslatorTest {

	@Mock
	private Document doc;

	@Mock
	private Log log;

	private final InterfaceTypeTranslator translator = new InterfaceTypeTranslator();

	@Test
	void translateNoInterfaces() {
		when(doc.getDefinitionsOfType(InterfaceTypeDefinition.class)).thenReturn(emptyList());
		GqlContext ctx = new GqlContext(log, emptyMap());

		translator.translate(doc, ctx);

		assertTrue(ctx.getInterfaceTypes().isEmpty());
	}

	@Test
	void translateOneInterfaceWithTwoFields() {
		when(doc.getDefinitionsOfType(InterfaceTypeDefinition.class)).thenReturn(singletonList(
			newInterfaceTypeDefinition()
				.name("Interface1")
				.definition(newFieldDefinition()
					.name("field1")
					.type(newTypeName("Type1").build())
					.build())
				.definition(newFieldDefinition()
					.name("field2")
					.type(newNonNullType(newListType(newNonNullType(newTypeName("Type2").build()).build()).build()).build())
					.build())
				.build()));
		GqlContext ctx = new GqlContext(log, emptyMap());

		translator.translate(doc, ctx);

		assertEquals(1, ctx.getInterfaceTypes().size());
		assertTrue(ctx.getInterfaceTypes().containsKey("Interface1"));
		Collection<GqlField> interface1 = ctx.getInterfaceTypes().get("Interface1").getFields();
		assertEquals(2, interface1.size());
		interface1.forEach(field -> {
			GqlType type = field.getType();
			switch (field.getName()) {
				case "field1":
					assertEquals(GqlType.Flag.NAMED, type.getFlag());
					assertEquals("Type1", type.getName());
					break;
				case "field2":
					assertEquals(GqlType.Flag.MANDATORY, type.getFlag());
					assertEquals(GqlType.Flag.LIST, type.getNested().getFlag());
					assertEquals(GqlType.Flag.MANDATORY, type.getNested().getNested().getFlag());
					assertEquals(GqlType.Flag.NAMED, type.getNested().getNested().getNested().getFlag());
					assertEquals("Type2", type.getNested().getNested().getNested().getName());
					break;
				default:
					fail();
			}
		});
	}

	@Test
	void translateOneInterfaceWithTwoFieldsWithExtension() {
		when(doc.getDefinitionsOfType(InterfaceTypeDefinition.class)).thenReturn(asList(
			newInterfaceTypeDefinition()
				.name("Interface1")
				.definition(newFieldDefinition()
					.name("field1")
					.type(newTypeName("Type1").build())
					.build())
				.build(),
			newInterfaceTypeExtensionDefinition()
				.name("Interface1")
				.definition(newFieldDefinition()
					.name("field2")
					.type(newNonNullType(newListType(newNonNullType(newTypeName("Type2").build()).build()).build()).build())
					.build())
				.build()));
		GqlContext ctx = new GqlContext(log, emptyMap());

		translator.translate(doc, ctx);

		assertEquals(1, ctx.getInterfaceTypes().size());
		assertTrue(ctx.getInterfaceTypes().containsKey("Interface1"));
		Collection<GqlField> interface1 = ctx.getInterfaceTypes().get("Interface1").getFields();
		assertEquals(2, interface1.size());
		interface1.forEach(field -> {
			GqlType type = field.getType();
			switch (field.getName()) {
				case "field1":
					assertEquals(GqlType.Flag.NAMED, type.getFlag());
					assertEquals("Type1", type.getName());
					break;
				case "field2":
					assertEquals(GqlType.Flag.MANDATORY, type.getFlag());
					assertEquals(GqlType.Flag.LIST, type.getNested().getFlag());
					assertEquals(GqlType.Flag.MANDATORY, type.getNested().getNested().getFlag());
					assertEquals(GqlType.Flag.NAMED, type.getNested().getNested().getNested().getFlag());
					assertEquals("Type2", type.getNested().getNested().getNested().getName());
					break;
				default:
					fail();
			}
		});
	}

	@Test
	void translateInterfaceImplementation() {
		when(doc.getDefinitionsOfType(InterfaceTypeDefinition.class)).thenReturn(asList(
			newInterfaceTypeDefinition()
				.name("Interface1")
				.definition(newFieldDefinition()
					.name("field1")
					.type(newTypeName("Type1").build())
					.build())
				.build(),
			newInterfaceTypeDefinition()
				.name("Interface2")
				.implementz(newTypeName("Interface1").build())
				.definition(newFieldDefinition()
					.name("field1")
					.type(newTypeName("Type1").build())
					.build())
				.definition(newFieldDefinition()
					.name("field2")
					.type(newNonNullType(newListType(newNonNullType(newTypeName("Type2").build()).build()).build()).build())
					.build())
				.build()));
		GqlContext ctx = new GqlContext(log, emptyMap());

		translator.translate(doc, ctx);

		assertEquals(2, ctx.getInterfaceTypes().size());
		assertTrue(ctx.getInterfaceTypes().containsKey("Interface1"));
		assertTrue(ctx.getInterfaceTypes().containsKey("Interface2"));
		GqlStructure interface1 = ctx.getInterfaceTypes().get("Interface1");
		GqlStructure interface2 = ctx.getInterfaceTypes().get("Interface2");
		assertEquals(1, interface1.getFields().size());
		assertEquals(0, interface1.getMembers().size());
		assertEquals(2, interface2.getFields().size());
		assertEquals(1, interface2.getMembers().size());
		interface1.getFields().forEach(field -> {
			assertEquals("field1", field.getName());
			GqlType type = field.getType();
			assertEquals(GqlType.Flag.NAMED, type.getFlag());
			assertEquals("Type1", type.getName());
		});
		interface2.getFields().forEach(field -> {
			GqlType type = field.getType();
			switch (field.getName()) {
				case "field1":
					assertEquals(GqlType.Flag.NAMED, type.getFlag());
					assertEquals("Type1", type.getName());
					break;
				case "field2":
					assertEquals(GqlType.Flag.MANDATORY, type.getFlag());
					assertEquals(GqlType.Flag.LIST, type.getNested().getFlag());
					assertEquals(GqlType.Flag.MANDATORY, type.getNested().getNested().getFlag());
					assertEquals(GqlType.Flag.NAMED, type.getNested().getNested().getNested().getFlag());
					assertEquals("Type2", type.getNested().getNested().getNested().getName());
					break;
				default:
					fail();
			}
		});
	}
}