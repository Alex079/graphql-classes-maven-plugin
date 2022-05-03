package com.github.alme.graphql.generator.io;

import static java.util.Collections.emptyMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.io.StringReader;

import com.github.alme.graphql.generator.dto.GqlConfiguration;
import com.github.alme.graphql.generator.dto.GqlContext;
import com.github.alme.graphql.generator.dto.GqlStructure;

import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GqlReaderTest {

	private static final String MINIMAL_SCHEMA = " type Query {}";
	private static final String INVALID_SCHEMA = "type Type { a: }";
	private static final GqlConfiguration MINIMAL_CONFIG = GqlConfiguration.builder().build();

	@Mock
	private ReaderFactory readerFactory;

	@Mock
	private Log log;

	@InjectMocks
	private GqlReader reader;

	@Test
	void throwExceptionWhenInputIsEmpty() {
		when(readerFactory.getReader()).thenReturn(new StringReader("  "));
		GqlContext context = new GqlContext(log, emptyMap());

		assertThatThrownBy(() -> reader.read(context, MINIMAL_CONFIG))
			.isExactlyInstanceOf(graphql.parser.InvalidSyntaxException.class)
			.hasMessageContaining("offending token");
	}

	@Test
	void throwExceptionWhenInputIsInvalid() {
		when(readerFactory.getReader()).thenReturn(new StringReader(INVALID_SCHEMA));
		GqlContext context = new GqlContext(log, emptyMap());

		assertThatThrownBy(() -> reader.read(context, MINIMAL_CONFIG))
			.isExactlyInstanceOf(graphql.parser.InvalidSyntaxException.class)
			.hasMessageContaining("offending token");
	}

	@Test
	void parseMinimalSchema() {
		when(readerFactory.getReader()).thenReturn(new StringReader(MINIMAL_SCHEMA));
		GqlContext context = new GqlContext(log, emptyMap());

		reader.read(context, GqlConfiguration.builder().generateSchemaOtherTypes(true).build());

		assertThat(context.getObjectTypes())
			.hasSize(1)
			.containsEntry("Query", new GqlStructure("Query"));
	}

}