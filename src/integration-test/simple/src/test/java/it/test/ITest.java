package it.test;

import static org.assertj.core.api.Assertions.assertThat;

import static it.test.support.Utils.gsonConvert;
import static it.test.support.Utils.jacksonConvert;

import java.util.Collections;
import java.util.Objects;

import org.junit.jupiter.api.Test;

class ITest {

	@Test
	void testEmptyType() {
		it.builder.g.types.EmptyType emptyType1WithGson = it.builder.g.types.EmptyType.builder().build();
		it.builder.g.types.EmptyType emptyType2WithGson = it.builder.g.types.EmptyType.builder().build();
		assertThat(emptyType1WithGson).isEqualTo(emptyType2WithGson)
			.extracting(Objects::toString).isEqualTo("{ }");

		it.chaining.types.EmptyType emptyType1WithJackson = new it.chaining.types.EmptyType();
		it.chaining.types.EmptyType emptyType2WithJackson = new it.chaining.types.EmptyType();
		assertThat(emptyType1WithJackson).isEqualTo(emptyType2WithJackson)
			.extracting(Objects::toString).isEqualTo("{ }");
	}

	@Test
	void testDocumentGeneration() {
		String[] expected = {"mutation updateField2($id: [ID!]! = [\"\"\"0\"\"\"]) {\n" +
			"\tfield2(arg1: $id) {\n" +
			"\t\tid\n" +
			"\t\tname\n" +
			"\t\t...on Type2MutationField2 {\n" +
			"\t\t\t...ValueFragment\n" +
			"\t\t}\n" +
			"\t\t...on Type1MutationField2 {\n" +
			"\t\t\t...ValueFragment\n" +
			"\t\t}\n" +
			"\t}\n" +
			"}",
			"fragment ValueFragment on Type1MutationField2 {\n" +
				"\tt1Value: value\n" +
				"}",
			"fragment ValueFragment on Type2MutationField2 {\n" +
				"\tt2Value: value {\n" +
				"\t\t...ValueFragment\n" +
				"\t}\n" +
				"}",
			"fragment ValueFragment on Type3MutationField2 {\n" +
				"\tt3Value: value\n" +
				"}"};

		it.builder.g.updateField2Mutation.UpdateField2Mutation gMutation =
			new it.builder.g.updateField2Mutation.UpdateField2Mutation(var -> var.setId(Collections.singletonList("2")));

		assertThat(gMutation.getDocument()).contains(expected);

		it.chaining.updateField2Mutation.UpdateField2Mutation cMutation =
			new it.chaining.updateField2Mutation.UpdateField2Mutation(var -> var.setId(Collections.singletonList("2")));

		assertThat(cMutation.getDocument()).contains(expected);

		it.plain.updateField2Mutation.UpdateField2Mutation pMutation =
			new it.plain.updateField2Mutation.UpdateField2Mutation(var -> var.setId(Collections.singletonList("2")));

		assertThat(pMutation.getDocument()).contains(expected);

		it.value.updateField2Mutation.UpdateField2Mutation vMutation =
			new it.value.updateField2Mutation.UpdateField2Mutation(var -> var.setId(Collections.singletonList("2")));

		assertThat(vMutation.getDocument()).contains(expected);
	}

	@Test
	void testStringToDefinedResultConversion() {
		String resultExample = "{\n" +
			"  \"field2\": [{\n" +
			"    \"id\": \"ID\",\n" +
			"    \"name\": \"Name\",\n" +
			"    \"t1Value\": 11,\n" +
			"    \"t2Value\": {\n" +
			"      \"t3Value\": 33\n" +
			"    }\n" +
			"  }]\n" +
			"}";

		it.builder.g.updateField2Mutation.results.Mutation1Result gResult =
			gsonConvert(resultExample, it.builder.g.updateField2Mutation.results.Mutation1Result.class);
		it.builder.j.updateField2Mutation.results.Mutation1Result jResult =
			jacksonConvert(resultExample, it.builder.j.updateField2Mutation.results.Mutation1Result.class);
		it.chaining.updateField2Mutation.results.Mutation1Result cResult =
			jacksonConvert(resultExample, it.chaining.updateField2Mutation.results.Mutation1Result.class);
		it.plain.updateField2Mutation.results.Mutation1Result pResult =
			jacksonConvert(resultExample, it.plain.updateField2Mutation.results.Mutation1Result.class);
		it.value.updateField2Mutation.results.Mutation1Result vResult =
			jacksonConvert(resultExample, it.value.updateField2Mutation.results.Mutation1Result.class);

		assertThat(gResult.toString())
			.isEqualTo(jResult.toString())
			.isEqualTo(cResult.toString())
			.isEqualTo(pResult.toString())
			.isEqualTo(vResult.toString());
		assertThat(gResult.getField2().get(0).getT2Value().getT3Value())
			.isEqualTo(jResult.getField2().get(0).getT2Value().getT3Value())
			.isEqualTo(cResult.getField2().get(0).getT2Value().getT3Value())
			.isEqualTo(pResult.getField2().get(0).getT2Value().getT3Value())
			.isEqualTo(vResult.getField2().get(0).getT2Value().getT3Value());
	}

	@Test
	void testStringToDynamicResultConversion() {
		String resultExample = "{" +
			"  \"field3\": [" +
			"    {" +
			"      \"name_Type2MutationField2\": \"C\"," +
			"      \"value_Type2MutationField2\": {\"value\": 2}" +
			"    }," +
			"    {" +
			"      \"id_Type1MutationField2\": \"G\"," +
			"      \"value_Type1MutationField2\": 3" +
			"    }" +
			"  ]" +
			"}";

		it.builder.g.dynamic.results.Mutation1Result gResult = gsonConvert(resultExample, it.builder.g.dynamic.results.Mutation1Result.class);
		it.builder.j.dynamic.results.Mutation1Result jResult = jacksonConvert(resultExample, it.builder.j.dynamic.results.Mutation1Result.class);
		it.chaining.dynamic.results.Mutation1Result cResult = jacksonConvert(resultExample, it.chaining.dynamic.results.Mutation1Result.class);
		it.plain.dynamic.results.Mutation1Result pResult = jacksonConvert(resultExample, it.plain.dynamic.results.Mutation1Result.class);
		it.value.dynamic.results.Mutation1Result vResult = jacksonConvert(resultExample, it.value.dynamic.results.Mutation1Result.class);

		assertThat(gResult.toString())
			.isEqualTo(jResult.toString())
			.isEqualTo(cResult.toString())
			.isEqualTo(pResult.toString())
			.isEqualTo(vResult.toString());
		assertThat(gResult.getField3().get(0).getValue_Type2MutationField2().getValue())
			.isEqualTo(jResult.getField3().get(0).getValue_Type2MutationField2().getValue())
			.isEqualTo(cResult.getField3().get(0).getValue_Type2MutationField2().getValue())
			.isEqualTo(pResult.getField3().get(0).getValue_Type2MutationField2().getValue())
			.isEqualTo(vResult.getField3().get(0).getValue_Type2MutationField2().getValue());
	}

	@Test
	void testDynamicUnion() {
		it.builder.g.dynamic.DynamicMutation gMutation = new it.builder.g.dynamic.DynamicMutation(selection -> selection
			.getField3(
				field3Arguments -> field3Arguments.setArg1("1"),
				union1MutationField3Selector -> union1MutationField3Selector
					.onType1MutationField2(type1MutationField2Selector -> type1MutationField2Selector
						.getName()
						.getValue()
					)
					.onType2MutationField2(type2MutationField2Selector -> type2MutationField2Selector
						.getId()
						.getValue(it.builder.g.dynamic.selectors.Type3MutationField2ResultSelector::getValue)
					)
			)
		);
		it.builder.j.dynamic.DynamicMutation jMutation = new it.builder.j.dynamic.DynamicMutation(selection -> selection
			.getField3(
				field3Arguments -> field3Arguments.setArg1("1"),
				union1MutationField3Selector -> union1MutationField3Selector
					.onType1MutationField2(type1MutationField2Selector -> type1MutationField2Selector
						.getName()
						.getValue()
					)
					.onType2MutationField2(type2MutationField2Selector -> type2MutationField2Selector
						.getId()
						.getValue(it.builder.j.dynamic.selectors.Type3MutationField2ResultSelector::getValue)
					)
			)
		);
		it.chaining.dynamic.DynamicMutation cMutation = new it.chaining.dynamic.DynamicMutation(selection -> selection
			.getField3(
				field3Arguments -> field3Arguments.setArg1("1"),
				union1MutationField3Selector -> union1MutationField3Selector
					.onType1MutationField2(type1MutationField2Selector -> type1MutationField2Selector
						.getName()
						.getValue()
					)
					.onType2MutationField2(type2MutationField2Selector -> type2MutationField2Selector
						.getId()
						.getValue(it.chaining.dynamic.selectors.Type3MutationField2ResultSelector::getValue)
					)
			)
		);
		String expectedDocument = "mutation { field3 ( arg1: \"\"\"1\"\"\" ) {" +
			" ...on Type1MutationField2 { name_Type1MutationField2: name value_Type1MutationField2: value }" +
			" ...on Type2MutationField2 { id_Type2MutationField2: id value_Type2MutationField2: value { value } } } }";

		assertThat(gMutation.getDocument()).isEqualTo(jMutation.getDocument()).isEqualTo(cMutation.getDocument()).isEqualTo(expectedDocument);
	}

	@Test
	void testDynamicInterface() {
		it.builder.g.dynamic.DynamicMutation gMutation = new it.builder.g.dynamic.DynamicMutation(selection -> selection
			.getField2(null, interface1MutationField2Selector -> interface1MutationField2Selector
				.onType1MutationField2(it.builder.g.dynamic.selectors.Interface1MutationField2ResultSelector.Type1MutationField2Selector::getName)
				.getId()
				.getName()
			)
		);
		it.builder.j.dynamic.DynamicMutation jMutation = new it.builder.j.dynamic.DynamicMutation(selection -> selection
			.getField2(null, interface1MutationField2Selector -> interface1MutationField2Selector
				.onType1MutationField2(it.builder.j.dynamic.selectors.Interface1MutationField2ResultSelector.Type1MutationField2Selector::getName)
				.getId()
				.getName()
			)
		);
		it.chaining.dynamic.DynamicMutation cMutation = new it.chaining.dynamic.DynamicMutation(selection -> selection
			.getField2(null, interface1MutationField2Selector -> interface1MutationField2Selector
				.onType1MutationField2(it.chaining.dynamic.selectors.Interface1MutationField2ResultSelector.Type1MutationField2Selector::getName)
				.getId()
				.getName()
			)
		);
		String expectedDocument = "mutation { field2 { ...on Type1MutationField2 { name_Type1MutationField2: name } id name } }";

		assertThat(gMutation.getDocument()).isEqualTo(jMutation.getDocument()).isEqualTo(cMutation.getDocument()).isEqualTo(expectedDocument);
	}

}