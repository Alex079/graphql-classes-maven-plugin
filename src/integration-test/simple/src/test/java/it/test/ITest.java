package it.test;

import static org.assertj.core.api.Assertions.assertThat;

import static it.test.support.Utils.gsonConvert;
import static it.test.support.Utils.jacksonConvert;

import java.util.Objects;

import org.junit.jupiter.api.Test;

class ITest {

	@Test
	void testEmptyType() {
		it.gson.types.EmptyType emptyType1WithGson = it.gson.types.EmptyType.builder().build();
		it.gson.types.EmptyType emptyType2WithGson = it.gson.types.EmptyType.builder().build();
		assertThat(emptyType1WithGson).isEqualTo(emptyType2WithGson)
			.extracting(Objects::toString).isEqualTo("{ }");

		it.jackson.types.EmptyType emptyType1WithJackson = new it.jackson.types.EmptyType();
		it.jackson.types.EmptyType emptyType2WithJackson = new it.jackson.types.EmptyType();
		assertThat(emptyType1WithJackson).isEqualTo(emptyType2WithJackson)
			.extracting(Objects::toString).isEqualTo("{ }");
	}

	@Test
	void testDocumentGeneration() {
		it.gson.updateField2Mutation.UpdateField2Mutation gMutation =
			new it.gson.updateField2Mutation.UpdateField2Mutation(var -> var.setId("2"));

		assertThat(gMutation.getDocument()).contains(
			"mutation updateField2($id: ID = \"\"\"0\"\"\") {\n" +
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
				"}"
		);

		it.jackson.updateField2Mutation.UpdateField2Mutation jMutation =
			new it.jackson.updateField2Mutation.UpdateField2Mutation(var -> var.setId("2"));

		assertThat(jMutation.getDocument()).contains(
			"mutation updateField2($id: ID = \"\"\"0\"\"\") {\n" +
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
				"}"
		);
	}

	@Test
	void testStringToResultConversion() {
		final String resultExample = "{\n" +
			"  \"field2\": {\n" +
			"    \"id\": \"ID\",\n" +
			"    \"name\": \"Name\",\n" +
			"    \"t1Value\": 11,\n" +
			"    \"t2Value\": {\n" +
			"      \"t3Value\": 33\n" +
			"    }\n" +
			"  }\n" +
			"}";

		it.gson.updateField2Mutation.UpdateField2MutationResult gResult =
			gsonConvert(resultExample, it.gson.updateField2Mutation.UpdateField2MutationResult.class);
		it.jackson.updateField2Mutation.UpdateField2MutationResult jResult =
			jacksonConvert(resultExample, it.jackson.updateField2Mutation.UpdateField2MutationResult.class);
		assertThat(gResult.toString()).isEqualTo(jResult.toString());
	}

//	@Test
//	void testDynamicUnion() {
//		it.gson.mutation.DynamicMutation gMutation = new it.gson.mutation.DynamicMutation(selection -> selection
//			.withTest1(grChSelector -> grChSelector
//				.onChannel(channel -> channel
//					.withName()
//					.withUsers(it.gson.selectors.UserSelector::withName)
//				)
//				.onGroup(group -> group
//					.withName()
//					.withUsers(it.gson.selectors.UserSelector::withName)
//				)
//			)
//		);
//		it.jackson.mutation.DynamicMutation jMutation = new it.jackson.mutation.DynamicMutation(selection -> selection
//			.withTest1(grChSelector -> grChSelector
//				.onChannel(channel -> channel
//					.withName()
//					.withUsers(it.jackson.selectors.UserSelector::withName)
//				)
//				.onGroup(group -> group
//					.withName()
//					.withUsers(it.jackson.selectors.UserSelector::withName)
//				)
//			)
//		);
//		final String expectedDocument = "mutation { test1 { " +
//			"...on Channel { name$Channel: name users$Channel: users { name } } " +
//			"...on Group { name$Group: name users$Group: users { name } } " +
//			"} }";
//
//		assertThat(gMutation.getDocument()).isEqualTo(jMutation.getDocument()).isEqualTo(expectedDocument);
//
//		final String resultExample = "{\n" +
//			"  \"test1\": [\n" +
//			"    {\n" +
//			"      \"name$Channel\": \"C\",\n" +
//			"      \"users$Channel\": [\n" +
//			"        {\"name\": \"U\"}\n" +
//			"      ]\n" +
//			"    },\n" +
//			"    {\n" +
//			"      \"name$Group\": \"G\",\n" +
//			"      \"users$Group\": [\n" +
//			"        {\"name\": \"U\"}\n" +
//			"      ]\n" +
//			"    }\n" +
//			"  ]\n" +
//			"}";
//
//		it.gson.results.MutationResult gResult = gsonConvert(resultExample, it.gson.results.MutationResult.class);
//		it.jackson.results.MutationResult jResult = jacksonConvert(resultExample, it.jackson.results.MutationResult.class);
//		assertThat(gResult.toString()).isEqualTo(jResult.toString());
//	}
//
//	@Test
//	void testDynamicInterface() {
//		it.gson.mutation.DynamicMutation gMutation = new it.gson.mutation.DynamicMutation(selection -> selection
//			.withTest2(i1Selector -> i1Selector
//				.onT1(it.gson.selectors.I1Selector.T1::withName)
//				.withId()
//				.withT1Name()
//			)
//		);
//		it.jackson.mutation.DynamicMutation jMutation = new it.jackson.mutation.DynamicMutation(selection -> selection
//			.withTest2(i1Selector -> i1Selector
//				.onT1(it.jackson.selectors.I1Selector.T1::withName)
//				.withId()
//				.withT1Name()
//			)
//		);
//		final String expectedDocument = "mutation { test2 { ...on T1 { name$T1: name } id t1Name } }";
//
//		assertThat(gMutation.getDocument()).isEqualTo(jMutation.getDocument()).isEqualTo(expectedDocument);
//
//		final String resultExample = "{\n" +
//			"  \"test2\": {\n" +
//			"    \"id\": \"ID\",\n" +
//			"    \"t1Name\": \"N\",\n" +
//			"    \"name$T1\": \"N2\"\n" +
//			"  }\n" +
//			"}";
//
//		it.gson.results.MutationResult gResult = gsonConvert(resultExample, it.gson.results.MutationResult.class);
//		it.jackson.results.MutationResult jResult = jacksonConvert(resultExample, it.jackson.results.MutationResult.class);
//		assertThat(gResult.toString()).isEqualTo(jResult.toString());
//	}

}