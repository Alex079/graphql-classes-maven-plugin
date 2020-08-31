package com.github.alme.graphql.generator.translator;

import static graphql.language.OperationTypeDefinition.newOperationTypeDefinition;
import static graphql.language.SchemaDefinition.newSchemaDefinition;
import static graphql.language.SchemaExtensionDefinition.newSchemaExtensionDefinition;
import static graphql.language.TypeName.newTypeName;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.github.alme.graphql.generator.dto.Context;
import graphql.language.Document;
import graphql.language.SchemaDefinition;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchemaTranslatorTest {

    @Mock
    private Document doc;

    @Mock
    private Log log;

    private final SchemaTranslator translator = new SchemaTranslator();

    @Test
    void translateEmptySchema() {
        when(doc.getDefinitionsOfType(SchemaDefinition.class)).thenReturn(emptyList());
        Context ctx = new Context(log);

        translator.translate(doc, ctx);

        assertTrue(ctx.getSchema().isEmpty());
    }

    @Test
    void translateSchemaWithTwoOperations() {
        when(doc.getDefinitionsOfType(SchemaDefinition.class)).thenReturn(singletonList(
                newSchemaDefinition()
                        .operationTypeDefinition(newOperationTypeDefinition()
                                .name("query")
                                .typeName(newTypeName("Type1").build())
                                .build())
                        .operationTypeDefinition(newOperationTypeDefinition()
                                .name("mutation")
                                .typeName(newTypeName("Type2").build())
                                .build())
                        .build()));
        Context ctx = new Context(log);

        translator.translate(doc, ctx);

        assertEquals(2, ctx.getSchema().size());
        assertEquals("Type1", ctx.getSchema().get("query"));
        assertEquals("Type2", ctx.getSchema().get("mutation"));
    }

    @Test
    void translateSchemaWithTwoOperationsWithExtension() {
        when(doc.getDefinitionsOfType(SchemaDefinition.class)).thenReturn(asList(
                newSchemaDefinition()
                        .operationTypeDefinition(newOperationTypeDefinition()
                                .name("query")
                                .typeName(newTypeName("Type1").build())
                                .build())
                        .build(),
                newSchemaExtensionDefinition()
                        .operationTypeDefinition(newOperationTypeDefinition()
                                .name("mutation")
                                .typeName(newTypeName("Type2").build())
                                .build())
                        .build()));
        Context ctx = new Context(log);

        translator.translate(doc, ctx);

        assertEquals(2, ctx.getSchema().size());
        assertEquals("Type1", ctx.getSchema().get("query"));
        assertEquals("Type2", ctx.getSchema().get("mutation"));
    }
}