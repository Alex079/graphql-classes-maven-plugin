package com.github.alme.graphql.generator.io.translator;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.dto.GqlValue;

import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import graphql.language.Document;
import graphql.language.EnumTypeDefinition;
import graphql.language.EnumTypeExtensionDefinition;
import graphql.language.EnumValueDefinition;

@ExtendWith(MockitoExtension.class)
class EnumTypeTranslatorTest {

	@Mock
	private Document doc;

	@Mock
	private Log log;

	private final EnumTypeTranslator translator = new EnumTypeTranslator();

	@Test
	void translateNoEnums() {
		when(doc.getDefinitionsOfType(EnumTypeDefinition.class)).thenReturn(emptyList());
		GqlContext ctx = new GqlContext(log, emptyMap(), emptyMap());

		translator.translate(doc, ctx);

		assertTrue(ctx.getEnumTypes().isEmpty());
	}

	@Test
	void translateOneEnumWithTwoMembers() {
		when(doc.getDefinitionsOfType(EnumTypeDefinition.class)).thenReturn(singletonList(
			EnumTypeDefinition.newEnumTypeDefinition()
				.name("Enum1")
				.enumValueDefinition(EnumValueDefinition.newEnumValueDefinition().name("V1").build())
				.enumValueDefinition(EnumValueDefinition.newEnumValueDefinition().name("V2").build())
				.build()));
		GqlContext ctx = new GqlContext(log, emptyMap(), emptyMap());

		translator.translate(doc, ctx);

		assertEquals(1, ctx.getEnumTypes().size());
		assertTrue(ctx.getEnumTypes().containsKey("Enum1"));
		assertEquals(2, ctx.getEnumTypes().get("Enum1").getValues().size());
		assertThat(ctx.getEnumTypes().get("Enum1").getValues()).extracting(GqlValue::getName).containsExactlyInAnyOrder("V1", "V2");
	}

	@Test
	void translateOneEnumWithTwoMembersWithExtension() {
		when(doc.getDefinitionsOfType(EnumTypeDefinition.class)).thenReturn(asList(
			EnumTypeDefinition.newEnumTypeDefinition()
				.name("Enum1")
				.enumValueDefinition(EnumValueDefinition.newEnumValueDefinition().name("V2").build())
				.build(),
			EnumTypeExtensionDefinition.newEnumTypeExtensionDefinition()
				.name("Enum1")
				.enumValueDefinitions(singletonList(EnumValueDefinition.newEnumValueDefinition().name("V1").build()))
				.build()));
		GqlContext ctx = new GqlContext(log, emptyMap(), emptyMap());

		translator.translate(doc, ctx);

		assertEquals(1, ctx.getEnumTypes().size());
		assertTrue(ctx.getEnumTypes().containsKey("Enum1"));
		assertEquals(2, ctx.getEnumTypes().get("Enum1").getValues().size());
		assertThat(ctx.getEnumTypes().get("Enum1").getValues()).extracting(GqlValue::getName).containsExactlyInAnyOrder("V1", "V2");
	}
}