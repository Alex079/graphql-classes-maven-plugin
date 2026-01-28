package com.github.alme.graphql.generator.io.creator;

import com.github.alme.graphql.generator.dto.Structure;
import com.github.alme.graphql.generator.io.WriterFactory;

import freemarker.template.Configuration;

public class StructureFileCreator extends FileCreator {

	private final Structure structure;

	public StructureFileCreator(WriterFactory writerFactory, Configuration freemarker, Structure structure) {
		super(writerFactory, freemarker);
		this.structure = structure;
	}

	@Override
	protected String getTemplate() {
		return structure.name() + ".ftl";
	}
}
