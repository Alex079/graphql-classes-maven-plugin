package com.github.alme.graphql.generator.translator;

import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.InterfaceTypeDefinition.newInterfaceTypeDefinition;
import static graphql.language.InterfaceTypeExtensionDefinition.newInterfaceTypeExtensionDefinition;
import static graphql.language.ListType.newListType;
import static graphql.language.NonNullType.newNonNullType;
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
import graphql.language.Document;
import graphql.language.InterfaceTypeDefinition;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        Context ctx = new Context(log);

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
        Context ctx = new Context(log);

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
                        .definitions(singletonList(newFieldDefinition()
                                .name("field2")
                                .type(newNonNullType(newListType(newNonNullType(newTypeName("Type2").build()).build()).build()).build())
                                .build()))
                        .build()));
        Context ctx = new Context(log);

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
            }
        });
    }
}