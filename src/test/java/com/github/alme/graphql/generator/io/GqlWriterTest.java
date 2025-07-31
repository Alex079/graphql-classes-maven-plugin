package com.github.alme.graphql.generator.io;

import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.InputValueDefinition.newInputValueDefinition;
import static graphql.language.ListType.newListType;
import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static graphql.language.TypeName.newTypeName;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.alme.graphql.generator.dto.GqlConfiguration;
import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.io.translator.ObjectTypeTranslator;
import com.github.alme.graphql.generator.io.translator.OperationTranslator;

import graphql.language.Document;
import graphql.language.Field;
import graphql.language.ObjectTypeDefinition;
import graphql.language.OperationDefinition;
import graphql.language.SelectionSet;

@ExtendWith(MockitoExtension.class)
class GqlWriterTest {

	private final ObjectTypeTranslator objectTypeTranslator = new ObjectTypeTranslator();
	private final OperationTranslator operationTranslator = new OperationTranslator();
	@Mock
	private Document doc;
	@Mock
	private Log log;
	@Mock
	private WriterFactory writerFactory;
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
		then_the_generated_class_overrides_the_toString_method();
		then_the_generated_class_overrides_the_equals_method();
		then_the_generated_class_overrides_the_hashCode_method();
	}

	@Test
	void translateOneQueryOperationTypeWithOneField() throws MojoExecutionException, IOException {
		given_a_context_containing_query_operation_definition_with_one_field();
		given_generate_defined_operations_config();
		when_the_write_is_invoked();
		assertThat(testWriter.toString()).containsIgnoringWhitespaces("\t@Override\n"
				+ "\tpublic boolean equals(Object o) {\n"
				+ "\t\tif (this == o) return true;\n"
				+ "\t\tif (!(o instanceof GetInstrumentsQuery)) return false;\n"
				+ "\t\tGetInstrumentsQuery other = (GetInstrumentsQuery) o;");
	}

	@Test
	void translateOneObjectTypeWithTwoFields() throws MojoExecutionException, IOException {
		given_a_context_containing_an_object_with_two_fields();
		given_the_minimum_set_of_configurations();
		when_the_write_is_invoked();
		then_the_generated_class_overrides_the_toString_method();
		then_the_generated_class_overrides_the_equals_method();
		then_the_generated_class_overrides_the_hashCode_method();
	}

	@Test
	void translateOneObjectTypeWithTwoFieldsAndBuilderPattern() throws MojoExecutionException, IOException {
		given_a_context_containing_an_object_with_two_fields();
		given_the_builder_enhancement_configurations();
		when_the_write_is_invoked();
		then_the_generated_class_overrides_the_toString_method();
		then_the_generated_class_overrides_the_equals_method();
		then_the_generated_class_overrides_the_hashCode_method();
		then_the_generated_class_has_builder_methods();
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
		this.ctx = new GqlContext(log, emptyMap(), emptyMap());
		objectTypeTranslator.translate(doc, ctx);
	}

	private void given_a_context_containing_an_object_without_fields() {
		when(doc.getDefinitionsOfType(ObjectTypeDefinition.class)).thenReturn(singletonList(
				newObjectTypeDefinition()
						.name("Object1")
						.build()));
		this.ctx = new GqlContext(log, emptyMap(), emptyMap());
		objectTypeTranslator.translate(doc, ctx);
	}

	private void given_a_context_containing_query_operation_definition_with_one_field() {
		when(doc.getDefinitionsOfType(OperationDefinition.class)).thenReturn(singletonList(
				OperationDefinition.newOperationDefinition()
						.name("GetInstruments")
						.operation(OperationDefinition.Operation.QUERY)
						.selectionSet(SelectionSet.newSelectionSet()
								.selection(Field.newField("isin").build())
								.build())
						.build()));
		this.ctx = new GqlContext(log, emptyMap(), emptyMap());
		ctx.getOperations().put("query", "Query");
		operationTranslator.translate(doc, ctx);
	}

	private void given_the_minimum_set_of_configurations() {
		gqlConfiguration = GqlConfiguration.builder()
				.schemaTypesPackageName("com.company.test")
				.generateSchemaOtherTypes(true)
				.build();
	}

	private void given_generate_defined_operations_config() {
		gqlConfiguration = GqlConfiguration.builder()
				.schemaTypesPackageName("com.company.test")
				.generateSchemaOtherTypes(true)
				.generateDefinedOperations(true)
				.operationsPackageName("com.company.test")
				.build();
	}

	private void given_the_builder_enhancement_configurations() {
		gqlConfiguration = GqlConfiguration.builder()
				.schemaTypesPackageName("com.company.test")
				.generateSchemaOtherTypes(true)
				.generateDtoBuilder(true)
				.build();
	}

	private void when_the_write_is_invoked() throws IOException, MojoExecutionException {
		doReturn(testWriter).when(writerFactory).getWriter(any(), any());
		GqlWriter gqlWriter = new GqlWriter(writerFactory);
		gqlWriter.write(ctx, gqlConfiguration);
	}

	private void then_the_generated_class_overrides_the_toString_method() {
		assertTrue(testWriter.toString().contains("public String toString()"));
	}

	private void then_the_generated_class_overrides_the_equals_method() {
		assertTrue(testWriter.toString().contains("public boolean equals(Object o)"));
	}

	private void then_the_generated_class_overrides_the_hashCode_method() {
		assertTrue(testWriter.toString().contains("public int hashCode()"));
	}

	private void then_the_generated_class_has_builder_methods() {
		assertTrue(testWriter.toString().contains("public static Builder builder()"));
		assertTrue(testWriter.toString().contains("public Builder toBuilder()"));
		assertTrue(testWriter.toString().contains("public Object1 build()"));
	}
}