package com.github.alme.graphql.generator.translator;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import static graphql.language.Argument.newArgument;
import static graphql.language.BooleanValue.newBooleanValue;
import static graphql.language.Directive.newDirective;
import static graphql.language.Field.newField;
import static graphql.language.FragmentDefinition.newFragmentDefinition;
import static graphql.language.FragmentSpread.newFragmentSpread;
import static graphql.language.InlineFragment.newInlineFragment;
import static graphql.language.IntValue.newIntValue;
import static graphql.language.OperationDefinition.newOperationDefinition;
import static graphql.language.SelectionSet.newSelectionSet;
import static graphql.language.TypeName.newTypeName;
import static graphql.language.VariableDefinition.newVariableDefinition;
import static graphql.language.VariableReference.newVariableReference;

import java.math.BigInteger;

import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.dto.GqlField;
import com.github.alme.graphql.generator.dto.GqlSelection;
import com.github.alme.graphql.generator.dto.GqlStructure;
import com.github.alme.graphql.generator.dto.GqlType;

import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import graphql.language.Document;
import graphql.language.FragmentDefinition;
import graphql.language.OperationDefinition;

@ExtendWith(MockitoExtension.class)
class OperationTranslatorTest {

	@Mock
	private Document doc;

	@Mock
	private Log log;

	private final Translator translator = new OperationTranslator();

	@Test
	void translateNoOperations() {
		GqlContext ctx = new GqlContext(log, emptyMap(), emptyMap());
		when(doc.getDefinitionsOfType(OperationDefinition.class)).thenReturn(emptyList());

		translator.translate(doc, ctx);

		assertTrue(ctx.getDefinedOperations().isEmpty());
	}

	@Test
	void translateWithoutSchema() {
		GqlContext ctx = new GqlContext(log, emptyMap(), emptyMap());
		when(doc.getDefinitionsOfType(OperationDefinition.class)).thenReturn(singletonList(
			newOperationDefinition()
				.operation(OperationDefinition.Operation.MUTATION)
				.build()
		));

		translator.translate(doc, ctx);

		assertTrue(ctx.getDefinedOperations().isEmpty());
	}

	@Test
	void translateOneEmptyUnnamedOperationWithoutFragments() {
		GqlContext ctx = new GqlContext(log, emptyMap(), emptyMap());
		ctx.getSchema().put("query", "Query");
		when(doc.getDefinitionsOfType(OperationDefinition.class)).thenReturn(singletonList(
			newOperationDefinition()
				.operation(OperationDefinition.Operation.QUERY)
				.selectionSet(newSelectionSet().build())
				.build()
		));
		when(doc.getDefinitionsOfType(FragmentDefinition.class)).thenReturn(emptyList());

		translator.translate(doc, ctx);

		assertThat(ctx.getDefinedOperations())
			.hasSize(1)
			.extracting(m -> m.get(null))
			.isNotNull()
			.satisfies(operation -> {
				assertThat(operation.getName()).isNull();
				assertThat(operation.getOperation()).isEqualTo("query");
				assertThat(operation.getText()).contains("query");
				assertThat(operation.getTypeName()).isEqualTo("Query");
				assertThat(operation.getVariables()).isEmpty();
				assertThat(operation.getSelections()).isEmpty();
			});
	}

	@Test
	void translateOneOperationWithoutFragments() {
		GqlContext ctx = new GqlContext(log, emptyMap(), emptyMap());
		ctx.getSchema().put("query", "Query");
		when(doc.getDefinitionsOfType(OperationDefinition.class)).thenReturn(singletonList(
			newOperationDefinition()
				.name("getValues")
				.operation(OperationDefinition.Operation.QUERY)
				.selectionSet(newSelectionSet().selection(newField("f").build()).build())
				.build()
		));
		when(doc.getDefinitionsOfType(FragmentDefinition.class)).thenReturn(emptyList());

		translator.translate(doc, ctx);

		assertThat(ctx.getDefinedOperations())
			.hasSize(1)
			.extractingByKey("getValues")
			.isNotNull()
			.satisfies(operation -> {
				assertThat(operation.getText()).containsIgnoringWhitespaces("query getValues {f}");
				assertThat(operation.getName()).isEqualTo("getValues");
				assertThat(operation.getOperation()).isEqualTo("query");
				assertThat(operation.getTypeName()).isEqualTo("Query");
				assertThat(operation.getVariables()).isEmpty();
				assertThat(operation.getSelections())
					.hasSize(1)
					.first().isEqualTo(new GqlSelection(new GqlField("f", GqlType.named("String")), "", ""));
			});
	}

	@Test
	void translateOneOperationWithFragmentsAndAnnotations() {
		GqlContext ctx = new GqlContext(log, emptyMap(), emptyMap());
		ctx.getSchema().put("query", "Query");
		ctx.getObjectTypes().put("Type1",
			new GqlStructure("Type1").addFields(singletonList(
				new GqlField("f1", GqlType.mandatory(GqlType.named("CustomType1")))
			)));
		ctx.getObjectTypes().put("Type2",
			new GqlStructure("Type2").addFields(singletonList(
				new GqlField("f2", GqlType.mandatory(GqlType.named("CustomType2")))
			)));
		ctx.getObjectTypes().put("Query",
			new GqlStructure("Query").addFields(asList(
				new GqlField("a", GqlType.named("Type1")),
				new GqlField("b", GqlType.named("Type2"))
			)));
		when(doc.getDefinitionsOfType(OperationDefinition.class)).thenReturn(singletonList(
			newOperationDefinition()
				.name("getValues")
				.operation(OperationDefinition.Operation.QUERY)
				.selectionSet(newSelectionSet()
					.selection(newField("a", newSelectionSet()
						.selection(newFragmentSpread("part").build())
						.build()).build())
					.selection(newField("b", newSelectionSet()
						.selection(newInlineFragment()
							.typeCondition(newTypeName("Type2").build())
							.selectionSet(newSelectionSet().selection(newField("f2").build()).build())
							.build())
						.build()).build())
					.build())
				.build()
		));
		when(doc.getDefinitionsOfType(FragmentDefinition.class)).thenReturn(asList(
			newFragmentDefinition()
				.name("part")
				.typeCondition(newTypeName("Type1").build())
				.directive(newDirective().name("CustomDirective").argument(newArgument("arg", newBooleanValue(true).build()).build()).build())
				.selectionSet(newSelectionSet().selection(newField("f1").alias("field1").build()).build())
				.build(),
			newFragmentDefinition()
				.name("part")
				.typeCondition(newTypeName("Type2").build())
				.selectionSet(newSelectionSet().selection(newField("f2").alias("field2").build()).build())
				.build()
		));

		translator.translate(doc, ctx);

		assertThat(ctx.getDefinedOperations())
			.hasSize(1)
			.extractingByKey("getValues")
			.isNotNull()
			.satisfies(operation -> {
				assertThat(operation.getText())
					.containsIgnoringWhitespaces(
						"query getValues {a {...part} b {...on Type2 {f2}}}",
						"fragment part on Type1 @CustomDirective(arg: true) {field1: f1}");
				assertThat(operation.getName()).isEqualTo("getValues");
				assertThat(operation.getOperation()).isEqualTo("query");
				assertThat(operation.getTypeName()).isEqualTo("Query");
				assertThat(operation.getVariables()).isEmpty();
				assertThat(operation.getSelections())
					.hasSize(2)
					.containsExactlyInAnyOrder(
						new GqlSelection(new GqlField("a", GqlType.named("Type1")), "", "")
							.addSelections(singletonList(
								new GqlSelection(new GqlField("field1", GqlType.mandatory(GqlType.named("CustomType1"))), "", ""))),
						new GqlSelection(new GqlField("b", GqlType.named("Type2")), "", "")
							.addSelections(singletonList(
								new GqlSelection(new GqlField("f2", GqlType.mandatory(GqlType.named("CustomType2"))), "", "")))
					);
			});
	}

	@Test
	void translateOneOperationWithVariables() {
		GqlContext ctx = new GqlContext(log, emptyMap(), emptyMap());
		ctx.getSchema().put("mutation", "Mutation");
		ctx.getObjectTypes().put("Mutation",
			new GqlStructure("Mutation").addFields(asList(
				new GqlField("a", GqlType.named("Type1")),
				new GqlField("b", GqlType.named("Type2"))
			)));
		when(doc.getDefinitionsOfType(OperationDefinition.class)).thenReturn(singletonList(
			newOperationDefinition()
				.name("setValues")
				.operation(OperationDefinition.Operation.MUTATION)
				.variableDefinition(newVariableDefinition("v", newTypeName("InputType").build(), newIntValue(BigInteger.ONE).build()).build())
				.selectionSet(newSelectionSet()
					.selection(newField("a").arguments(singletonList(newArgument("v", newVariableReference().name("v").build()).build())).build())
					.build())
				.build()
		));
		when(doc.getDefinitionsOfType(FragmentDefinition.class)).thenReturn(emptyList());

		translator.translate(doc, ctx);

		assertThat(ctx.getDefinedOperations())
			.hasSize(1)
			.extractingByKey("setValues")
			.isNotNull()
			.satisfies(operation -> {
				assertThat(operation.getText()).containsIgnoringWhitespaces("mutation setValues($v: InputType = 1) {a(v: $v)}");
				assertThat(operation.getName()).isEqualTo("setValues");
				assertThat(operation.getOperation()).isEqualTo("mutation");
				assertThat(operation.getTypeName()).isEqualTo("Mutation");
				assertThat(operation.getVariables())
					.hasSize(1)
					.first().isEqualTo(new GqlField("v", GqlType.named("InputType")));
				assertThat(operation.getSelections())
					.hasSize(1)
					.first().isEqualTo(new GqlSelection(new GqlField("a", GqlType.named("Type1")), "", ""));
			});
	}

}