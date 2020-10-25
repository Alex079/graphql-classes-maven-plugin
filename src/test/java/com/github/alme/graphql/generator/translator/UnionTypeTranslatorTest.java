package com.github.alme.graphql.generator.translator;

import static graphql.language.TypeName.newTypeName;
import static graphql.language.UnionTypeDefinition.newUnionTypeDefinition;
import static graphql.language.UnionTypeExtensionDefinition.newUnionTypeExtensionDefinition;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.github.alme.graphql.generator.dto.Context;

import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import graphql.language.Document;
import graphql.language.UnionTypeDefinition;

@ExtendWith(MockitoExtension.class)
class UnionTypeTranslatorTest {

	@Mock
	private Document doc;

	@Mock
	private Log log;

	private final UnionTypeTranslator translator = new UnionTypeTranslator();

	@Test
	void translateNoUnions() {
		when(doc.getDefinitionsOfType(UnionTypeDefinition.class)).thenReturn(emptyList());
		Context ctx = new Context(log);

		translator.translate(doc, ctx);

		assertTrue(ctx.getUnionTypes().isEmpty());
	}

	@Test
	void translateOneUnionOfTwoTypes() {
		when(doc.getDefinitionsOfType(UnionTypeDefinition.class)).thenReturn(singletonList(
			newUnionTypeDefinition()
				.name("Union1")
				.memberType(newTypeName("Type1").build())
				.memberType(newTypeName("Type2").build())
				.build()));
		Context ctx = new Context(log);

		translator.translate(doc, ctx);

		assertEquals(1, ctx.getUnionTypes().size());
		assertTrue(ctx.getUnionTypes().containsKey("Union1"));
		assertEquals(2, ctx.getObjectTypes().size());
		assertTrue(ctx.getObjectTypes().keySet().containsAll(asList("Type2", "Type1")));
	}

	@Test
	void translateOneUnionOfTwoTypesWithExtension() {
		when(doc.getDefinitionsOfType(UnionTypeDefinition.class)).thenReturn(asList(
			newUnionTypeDefinition()
				.name("Union1")
				.memberType(newTypeName("Type2").build())
				.build(),
			newUnionTypeExtensionDefinition()
				.name("Union1")
				.memberTypes(singletonList(newTypeName("Type1").build()))
				.build()));
		Context ctx = new Context(log);

		translator.translate(doc, ctx);

		assertEquals(1, ctx.getUnionTypes().size());
		assertTrue(ctx.getUnionTypes().containsKey("Union1"));
		assertEquals(2, ctx.getObjectTypes().size());
		assertTrue(ctx.getObjectTypes().keySet().containsAll(asList("Type2", "Type1")));
	}
}