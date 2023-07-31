package com.github.alme.graphql.generator.io.creator;

import com.github.alme.graphql.generator.io.WriterFactory;

import freemarker.template.Configuration;

public class DefinedOperationFileCreator extends FileCreator {

	public DefinedOperationFileCreator(WriterFactory writerFactory, Configuration freemarker) {
		super(writerFactory, freemarker);
	}

	@Override
	protected String getTemplate() {
		return "DEFINED_OPERATION";
	}
}
