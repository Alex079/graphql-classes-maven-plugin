package com.github.alme.graphql.generator.io;

import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.InputValueDefinition.newInputValueDefinition;
import static graphql.language.ListType.newListType;
import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static graphql.language.TypeName.newTypeName;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Paths;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.alme.graphql.generator.dto.GqlConfiguration;
import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.io.utils.FileSystem;
import com.github.alme.graphql.generator.translator.ObjectTypeTranslator;

import graphql.language.Document;
import graphql.language.ObjectTypeDefinition;

@ExtendWith(MockitoExtension.class)
class TestGqlWriter {
	@Mock
	private Document doc;

	@Mock
	private Log log;

	@Mock
	private FileSystem fileSystem;

	private final ObjectTypeTranslator translator = new ObjectTypeTranslator();
	private GqlContext ctx;
	private GqlConfiguration gqlConfiguration;
	private StringWriter testWriter;

	@BeforeEach
	void init() {
		this.ctx = null;
		this.gqlConfiguration = null;
		this.testWriter = new StringWriter();
	}

	@Test
	void translateOneObjectTypeWithNoFields() throws MojoExecutionException, IOException {
		given_a_context_containing_an_object_without_fields();
		given_the_minimum_set_of_configurations();
		when_the_write_is_invoked();
		then_the_generated_class_does_not_override_the_toString_method();
		then_the_generated_class_does_not_override_the_equals_method();
		then_the_generated_class_does_not_override_the_hashCode_method();
	}

	@Test
	void translateOneObjectTypeWithTwoFields() throws MojoExecutionException, IOException {
		given_a_context_containing_an_object_with_two_fields();
		given_the_minimum_set_of_configurations();
		when_the_write_is_invoked();
		then_the_generated_class_does_overrides_the_toString_method();
		then_the_generated_class_does_overrides_the_equals_method();
		then_the_generated_class_does_overrides_the_hashCode_method();
	}

	private void given_a_context_containing_an_object_with_two_fields() {
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
		this.ctx = new GqlContext(log, emptyMap());
		translator.translate(doc, ctx);
	}

	private void given_a_context_containing_an_object_without_fields() {
		when(doc.getDefinitionsOfType(ObjectTypeDefinition.class)).thenReturn(singletonList(
				newObjectTypeDefinition()
						.name("Object1")
						.build()));
		this.ctx = new GqlContext(log, emptyMap());
		translator.translate(doc, ctx);
	}

	private void given_the_minimum_set_of_configurations() {
		gqlConfiguration = GqlConfiguration.builder()
				.typesPackageName("com.company.test")
				.typesPackagePath(Paths.get("target/generated"))
				.build();
	}

	private void when_the_write_is_invoked() throws IOException, MojoExecutionException {
		doReturn(testWriter).when(fileSystem).getWriter(any());
		GqlWriter gqlWriter = new GqlWriter(this.fileSystem);
		gqlWriter.write(ctx, gqlConfiguration);

	}

	private void then_the_generated_class_does_not_override_the_toString_method() {
		assertFalse(testWriter.toString().contains("public String toString()"));
	}

	private void then_the_generated_class_does_not_override_the_equals_method() {
		assertFalse(testWriter.toString().contains("public boolean equals(Object o)"));
	}

	private void then_the_generated_class_does_not_override_the_hashCode_method() {
		assertFalse(testWriter.toString().contains("public int hashCode()"));
	}

	private void then_the_generated_class_does_overrides_the_toString_method() {
		assertTrue(testWriter.toString().contains("public String toString()"));
	}

	private void then_the_generated_class_does_overrides_the_equals_method() {
		assertTrue(testWriter.toString().contains("public boolean equals(Object o)"));
	}

	private void then_the_generated_class_does_overrides_the_hashCode_method() {
		assertTrue(testWriter.toString().contains("public int hashCode()"));
	}
}