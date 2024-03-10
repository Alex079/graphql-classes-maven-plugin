package com.github.alme.graphql.generator.parameters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.github.alme.graphql.generator.dto.GqlConfiguration;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;

@ExtendWith(MockitoExtension.class)
class ScalarMapParameterApplierTest {

	@Spy
	private GqlConfiguration.GqlConfigurationBuilder builder = GqlConfiguration.builder();

	@SneakyThrows
	@Test
	void ignoreIncompleteAlternativeProperties() {
		GqlConfiguration configuration = builder
			.accept(new ScalarMapParameterApplier(null, Lists.list("A=", "=B", "=", "C", null)))
			.build();

		assertThat(configuration.getScalars()).containsOnlyKeys("ID", "Int", "Float");
		verify(builder).accept(any());
		verify(builder).build();
		verify(builder).scalars(anyMap());
		verifyNoMoreInteractions(builder);
	}

	@SneakyThrows
	@Test
	void parseValidAlternativeProperties() {
		GqlConfiguration configuration = builder
			.accept(new ScalarMapParameterApplier(null, Lists.list("A=B")))
			.build();

		assertThat(configuration.getScalars()).containsOnlyKeys("ID", "Int", "Float", "A").containsEntry("A", "B");
		verify(builder).accept(any());
		verify(builder).build();
		verify(builder).scalars(anyMap());
		verify(builder).scalar("A", "B");
		verifyNoMoreInteractions(builder);
	}

	@SneakyThrows
	@Test
	void ignoreAlternativePropertiesWhenMapIsPresent() {
		GqlConfiguration configuration = builder
			.accept(new ScalarMapParameterApplier(Collections.emptyMap(), Lists.list("A=B")))
			.build();

		assertThat(configuration.getScalars()).containsOnlyKeys("ID", "Int", "Float");
		verify(builder).accept(any());
		verify(builder).build();
		verify(builder).scalars(anyMap());
		verifyNoMoreInteractions(builder);
	}

	@SneakyThrows
	@Test
	void parseInputPropertiesAndIgnoreIncomplete() {
		Map<String, String> inputMap = new HashMap<>();
		inputMap.put("A ", " B");
		inputMap.put("", " C ");
		inputMap.put("D", null);
		GqlConfiguration configuration = builder
			.accept(new ScalarMapParameterApplier(inputMap, null))
			.build();

		assertThat(configuration.getScalars()).containsOnlyKeys("ID", "Int", "Float", "A").containsEntry("A", "B");
		verify(builder).accept(any());
		verify(builder).build();
		verify(builder).scalars(anyMap());
		verify(builder).scalar("A", "B");
		verifyNoMoreInteractions(builder);
	}

	@SneakyThrows
	@Test
	void containsOnlyDefaultWhenNothingIsPassed() {
		GqlConfiguration configuration = builder
			.accept(new ScalarMapParameterApplier(null, null))
			.build();

		assertThat(configuration.getScalars())
			.containsEntry("ID", "String")
			.containsEntry("Int", "Integer")
			.containsEntry("Float", "Double");
		verify(builder).accept(any());
		verify(builder).build();
		verify(builder).scalars(anyMap());
		verifyNoMoreInteractions(builder);
	}

}