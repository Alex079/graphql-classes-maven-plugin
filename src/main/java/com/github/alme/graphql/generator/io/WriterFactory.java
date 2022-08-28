package com.github.alme.graphql.generator.io;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class WriterFactory {

	public Writer getWriter(Path path) throws IOException {
		Files.createDirectories(path.getParent());
		return Files.newBufferedWriter(path);
	}

}
