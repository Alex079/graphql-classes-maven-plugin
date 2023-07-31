package com.github.alme.graphql.generator.io;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WriterFactory {

	private static final String FILE_EXTENSION = ".java";
	private static final char SUBPACKAGE_SEPARATOR = '.';
	private static final char FS_SEPARATOR = File.separatorChar;

	private final Path outputRoot;

	public Writer getWriter(String packageName, String className) throws IOException {
		Path filePath = outputRoot.resolve(packageName.replace(SUBPACKAGE_SEPARATOR, FS_SEPARATOR)).resolve(className + FILE_EXTENSION);
		Files.createDirectories(filePath.getParent());
		return Files.newBufferedWriter(filePath);
	}

}
