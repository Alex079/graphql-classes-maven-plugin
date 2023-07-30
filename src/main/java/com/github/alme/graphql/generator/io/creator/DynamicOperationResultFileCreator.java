package com.github.alme.graphql.generator.io.creator;

import com.github.alme.graphql.generator.io.WriterFactory;

import freemarker.template.Configuration;

public class DynamicOperationResultFileCreator extends FileCreator {

	public DynamicOperationResultFileCreator(WriterFactory writerFactory, Configuration freemarker) {
		super(writerFactory, freemarker);
	}

	@Override
	protected String getTemplate() {
		return "DYNAMIC_OPERATION_RESULT";
	}
}
