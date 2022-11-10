package com.github.alme.graphql.generator.parameters;

import static com.github.alme.graphql.generator.dto.GqlConfiguration.DataObjectEnhancementType.BUILDER;
import static com.github.alme.graphql.generator.dto.GqlConfiguration.DataObjectEnhancementType.METHOD_CHAINING;
import static com.github.alme.graphql.generator.dto.GqlConfiguration.DataObjectEnhancementType.VALUE;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.github.alme.graphql.generator.dto.GqlConfiguration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;

@ExtendWith(MockitoExtension.class)
class DataObjectEnhancementTypeParameterApplierTest {

	@Spy
	private GqlConfiguration.GqlConfigurationBuilder builder = GqlConfiguration.builder();

	@SneakyThrows
	@Test
	void generatePlain() {
		GqlConfiguration configuration = builder.accept(new DataObjectEnhancementTypeParameterApplier(null)).build();

		assertThat(configuration.isGenerateDtoBuilder()).isFalse();
		assertThat(configuration.isGenerateDtoMethodChaining()).isFalse();
		assertThat(configuration.isGenerateDtoSetters()).isTrue();
		assertThat(configuration.isGenerateDtoConstructor()).isFalse();

		verify(builder).accept(any());
		verify(builder).build();
		verify(builder).generateDtoBuilder(false);
		verify(builder).generateDtoMethodChaining(false);
		verify(builder).generateDtoSetters(true);
		verify(builder).generateDtoConstructor(false);
		verifyNoMoreInteractions(builder);
	}

	@SneakyThrows
	@Test
	void generateMethodChaining() {
		GqlConfiguration configuration = builder.accept(new DataObjectEnhancementTypeParameterApplier(METHOD_CHAINING)).build();

		assertThat(configuration.isGenerateDtoBuilder()).isFalse();
		assertThat(configuration.isGenerateDtoMethodChaining()).isTrue();
		assertThat(configuration.isGenerateDtoSetters()).isTrue();
		assertThat(configuration.isGenerateDtoConstructor()).isFalse();

		verify(builder).accept(any());
		verify(builder).build();
		verify(builder).generateDtoBuilder(false);
		verify(builder).generateDtoMethodChaining(true);
		verify(builder).generateDtoSetters(true);
		verify(builder).generateDtoConstructor(false);
		verifyNoMoreInteractions(builder);
	}

	@SneakyThrows
	@Test
	void generateValue() {
		GqlConfiguration configuration = builder.accept(new DataObjectEnhancementTypeParameterApplier(VALUE)).build();

		assertThat(configuration.isGenerateDtoBuilder()).isFalse();
		assertThat(configuration.isGenerateDtoMethodChaining()).isFalse();
		assertThat(configuration.isGenerateDtoSetters()).isFalse();
		assertThat(configuration.isGenerateDtoConstructor()).isTrue();

		verify(builder).accept(any());
		verify(builder).build();
		verify(builder).generateDtoBuilder(false);
		verify(builder).generateDtoMethodChaining(false);
		verify(builder).generateDtoSetters(false);
		verify(builder).generateDtoConstructor(true);
		verifyNoMoreInteractions(builder);
	}

	@SneakyThrows
	@Test
	void generateDtoBuilder() {
		GqlConfiguration configuration = builder.accept(new DataObjectEnhancementTypeParameterApplier(BUILDER)).build();

		assertThat(configuration.isGenerateDtoBuilder()).isTrue();
		assertThat(configuration.isGenerateDtoMethodChaining()).isTrue();
		assertThat(configuration.isGenerateDtoSetters()).isFalse();
		assertThat(configuration.isGenerateDtoConstructor()).isTrue();

		verify(builder).accept(any());
		verify(builder).build();
		verify(builder).generateDtoBuilder(true);
		verify(builder).generateDtoMethodChaining(true);
		verify(builder).generateDtoSetters(false);
		verify(builder).generateDtoConstructor(true);
		verifyNoMoreInteractions(builder);
	}

}