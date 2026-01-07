package com.github.alme.graphql.generator.parameters;

import com.github.alme.graphql.generator.dto.GqlConfiguration;

import graphql.parser.ParserOptions;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ParserOptionsParameterApplier implements ParameterApplier {

	private final Integer parserMaxTokens;

	@Override
	public void apply(GqlConfiguration.GqlConfigurationBuilder builder) {
		builder.parserOptions(ParserOptions.getDefaultParserOptions().transform(options -> {
			if (parserMaxTokens != null) {
				options.maxTokens(parserMaxTokens);
			}
		}));
	}

}
