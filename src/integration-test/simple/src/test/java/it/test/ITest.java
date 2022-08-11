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
		it.gson.types.EmptyType emptyType1WithGson = it.gson.types.EmptyType.builder().build();
		it.gson.types.EmptyType emptyType2WithGson = it.gson.types.EmptyType.builder().build();
		assertThat(emptyType1WithGson).isEqualTo(emptyType2WithGson)
			.extracting(Objects::toString).isEqualTo(" { }");

		it.jackson.types.EmptyType emptyType1WithJackson = new it.jackson.types.EmptyType();
		it.jackson.types.EmptyType emptyType2WithJackson = new it.jackson.types.EmptyType();
		assertThat(emptyType1WithJackson).isEqualTo(emptyType2WithJackson)
			.extracting(Objects::toString).isEqualTo(" { }");
	}

	@Test
	void testDocumentGeneration() {
		CharSequence[] expected = {"mutation updateField2($id: [ID!]! = [\"\"\"0\"\"\"]) {\n" +
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

		it.gson.updateField2Mutation.UpdateField2Mutation gMutation =
			new it.gson.updateField2Mutation.UpdateField2Mutation(var -> var.setId(Collections.singletonList("2")));

		assertThat(gMutation.getDocument()).contains(expected);

		it.jackson.updateField2Mutation.UpdateField2Mutation jMutation =
			new it.jackson.updateField2Mutation.UpdateField2Mutation(var -> var.setId(Collections.singletonList("2")));

		assertThat(jMutation.getDocument()).contains(expected);

		it.plain.updateField2Mutation.UpdateField2Mutation pMutation =
			new it.plain.updateField2Mutation.UpdateField2Mutation(var -> var.setId(Collections.singletonList("2")));

		assertThat(pMutation.getDocument()).contains(expected);
	}

	@Test
	void testStringToResultConversion() {
		final String resultExample = "{\n" +
			"  \"field2\": [{\n" +
			"    \"id\": \"ID\",\n" +
			"    \"name\": \"Name\",\n" +
			"    \"t1Value\": 11,\n" +
			"    \"t2Value\": {\n" +
			"      \"t3Value\": 33\n" +
			"    }\n" +
			"  }]\n" +
			"}";

		it.gson.updateField2Mutation.UpdateField2MutationResult gResult =
			gsonConvert(resultExample, it.gson.updateField2Mutation.UpdateField2MutationResult.class);
		it.jackson.updateField2Mutation.UpdateField2MutationResult jResult =
			jacksonConvert(resultExample, it.jackson.updateField2Mutation.UpdateField2MutationResult.class);
		it.plain.updateField2Mutation.UpdateField2MutationResult pResult =
			jacksonConvert(resultExample, it.plain.updateField2Mutation.UpdateField2MutationResult.class);
		assertThat(gResult.toString())
			.isEqualTo(jResult.toString())
			.isEqualTo(pResult.toString());
		assertThat(gResult.getField2().get(0).getT2Value().getT3Value())
			.isEqualTo(jResult.getField2().get(0).getT2Value().getT3Value())
			.isEqualTo(pResult.getField2().get(0).getT2Value().getT3Value());
	}

	@Test
	void testDynamicUnion() {
		it.gson.mutation.DynamicMutation gMutation = new it.gson.mutation.DynamicMutation(selection -> selection
			.getField3(
				field3Arguments -> field3Arguments.setArg1("1"),
				union1MutationField3Selector -> union1MutationField3Selector
					.onType1MutationField2(type1MutationField2Selector -> type1MutationField2Selector
						.getName()
						.getValue()
					)
					.onType2MutationField2(type2MutationField2Selector -> type2MutationField2Selector
						.getId()
						.getValue(it.gson.selectors.Type3MutationField2Selector::getValue)
					)
			)
		);
		it.jackson.mutation.DynamicMutation jMutation = new it.jackson.mutation.DynamicMutation(selection -> selection
			.getField3(
				field3Arguments -> field3Arguments.setArg1("1"),
				union1MutationField3Selector -> union1MutationField3Selector
					.onType1MutationField2(type1MutationField2Selector -> type1MutationField2Selector
						.getName()
						.getValue()
					)
					.onType2MutationField2(type2MutationField2Selector -> type2MutationField2Selector
						.getId()
						.getValue(it.jackson.selectors.Type3MutationField2Selector::getValue)
					)
			)
		);
		final String expectedDocument = "mutation { field3 ( arg1: \"\"\"1\"\"\" ) {" +
			" ...on Type1MutationField2 { name_Type1MutationField2: name value_Type1MutationField2: value }" +
			" ...on Type2MutationField2 { id_Type2MutationField2: id value_Type2MutationField2: value { value } } } }";

		assertThat(gMutation.getDocument()).isEqualTo(jMutation.getDocument()).isEqualTo(expectedDocument);

		final String resultExample = "{" +
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

		it.gson.results.Mutation1Result gResult = gsonConvert(resultExample, it.gson.results.Mutation1Result.class);
		it.jackson.results.Mutation1Result jResult = jacksonConvert(resultExample, it.jackson.results.Mutation1Result.class);
		assertThat(gResult.toString()).isEqualTo(jResult.toString());
	}

	@Test
	void testDynamicInterface() {
		it.gson.mutation.DynamicMutation gMutation = new it.gson.mutation.DynamicMutation(selection -> selection
			.getField2(null, interface1MutationField2Selector -> interface1MutationField2Selector
				.onType1MutationField2(it.gson.selectors.Interface1MutationField2Selector.Type1MutationField2Selector::getName)
				.getId()
				.getName()
			)
		);
		it.jackson.mutation.DynamicMutation jMutation = new it.jackson.mutation.DynamicMutation(selection -> selection
			.getField2(null, interface1MutationField2Selector -> interface1MutationField2Selector
				.onType1MutationField2(it.jackson.selectors.Interface1MutationField2Selector.Type1MutationField2Selector::getName)
				.getId()
				.getName()
			)
		);
		final String expectedDocument = "mutation { field2 { ...on Type1MutationField2 { name_Type1MutationField2: name } id name } }";

		assertThat(gMutation.getDocument()).isEqualTo(jMutation.getDocument()).isEqualTo(expectedDocument);

		final String resultExample = "{" +
			"  \"field2\": [" +
			"    {" +
			"      \"id\": \"ID1\"," +
			"      \"id_Type1MutationField2\": \"N\"" +
			"    }," +
			"    {" +
			"      \"id\": \"ID2\"," +
			"      \"value_Type2MutationField2\": {\"value\": 2}" +
			"    }" +
			"  ]" +
			"}";

		it.gson.results.Mutation1Result gResult = gsonConvert(resultExample, it.gson.results.Mutation1Result.class);
		it.jackson.results.Mutation1Result jResult = jacksonConvert(resultExample, it.jackson.results.Mutation1Result.class);
		assertThat(gResult.toString()).isEqualTo(jResult.toString());
	}

}