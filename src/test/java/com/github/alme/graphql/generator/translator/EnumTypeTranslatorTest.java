package com.github.alme.graphql.generator.translator;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.github.alme.graphql.generator.dto.Context;
import graphql.language.Document;
import graphql.language.EnumTypeDefinition;
import graphql.language.EnumTypeExtensionDefinition;
import graphql.language.EnumValueDefinition;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        Context ctx = new Context(log);

        translator.translate(doc, ctx);

        assertTrue(ctx.getUnionTypes().isEmpty());
    }

    @Test
    void translateOneEnumWithTwoMembers() {
        when(doc.getDefinitionsOfType(EnumTypeDefinition.class)).thenReturn(singletonList(
                EnumTypeDefinition.newEnumTypeDefinition()
                        .name("Enum1")
                        .enumValueDefinition(EnumValueDefinition.newEnumValueDefinition().name("V1").build())
                        .enumValueDefinition(EnumValueDefinition.newEnumValueDefinition().name("V2").build())
                        .build()));
        Context ctx = new Context(log);

        translator.translate(doc, ctx);

        assertEquals(1, ctx.getEnumTypes().size());
        assertTrue(ctx.getEnumTypes().containsKey("Enum1"));
        assertEquals(2, ctx.getEnumTypes().get("Enum1").getMembers().size());
        assertTrue(ctx.getEnumTypes().get("Enum1").getMembers().containsAll(asList("V2", "V1")));
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
        Context ctx = new Context(log);

        translator.translate(doc, ctx);

        assertEquals(1, ctx.getEnumTypes().size());
        assertTrue(ctx.getEnumTypes().containsKey("Enum1"));
        assertEquals(2, ctx.getEnumTypes().get("Enum1").getMembers().size());
        assertTrue(ctx.getEnumTypes().get("Enum1").getMembers().containsAll(asList("V2", "V1")));
    }
}