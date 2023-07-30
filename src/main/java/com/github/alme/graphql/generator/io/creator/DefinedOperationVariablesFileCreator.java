package com.github.alme.graphql.generator.io.creator;

import com.github.alme.graphql.generator.io.WriterFactory;

import freemarker.template.Configuration;

public class DefinedOperationVariablesFileCreator extends FileCreator {

	public DefinedOperationVariablesFileCreator(WriterFactory writerFactory, Configuration freemarker) {
		super(writerFactory, freemarker);
	}

	@Override
	protected String getTemplate() {
		return "DEFINED_OPERATION_VARIABLES";
	}
}
