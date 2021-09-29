package com.github.alme.graphql.generator.parameters;

import static com.github.alme.graphql.generator.dto.GqlConfiguration.DataObjectEnhancementType.BUILDER;
import static com.github.alme.graphql.generator.dto.GqlConfiguration.DataObjectEnhancementType.METHOD_CHAINING;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
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
	void ignoreNull() {
		GqlConfiguration configuration = builder.accept(new DataObjectEnhancementTypeParameterApplier(null)).build();

		assertThat(configuration.isGenerateDtoBuilder()).isFalse();
		assertThat(configuration.isGenerateMethodChaining()).isFalse();

		verify(builder).accept(any());
		verify(builder).build();
		verify(builder, never()).generateDtoBuilder(anyBoolean());
		verify(builder, never()).generateMethodChaining(anyBoolean());
		verifyNoMoreInteractions(builder);
	}

	@SneakyThrows
	@Test
	void generateMethodChaining() {
		GqlConfiguration configuration = builder.accept(new DataObjectEnhancementTypeParameterApplier(METHOD_CHAINING)).build();

		assertThat(configuration.isGenerateDtoBuilder()).isFalse();
		assertThat(configuration.isGenerateMethodChaining()).isTrue();
		verify(builder).accept(any());
		verify(builder).build();
		verify(builder).generateDtoBuilder(false);
		verify(builder).generateMethodChaining(true);
		verifyNoMoreInteractions(builder);
	}

	@SneakyThrows
	@Test
	void generateDtoBuilder() {
		GqlConfiguration configuration = builder.accept(new DataObjectEnhancementTypeParameterApplier(BUILDER)).build();

		assertThat(configuration.isGenerateDtoBuilder()).isTrue();
		assertThat(configuration.isGenerateMethodChaining()).isTrue();
		verify(builder).accept(any());
		verify(builder).build();
		verify(builder).generateDtoBuilder(true);
		verify(builder).generateMethodChaining(true);
		verifyNoMoreInteractions(builder);
	}

}