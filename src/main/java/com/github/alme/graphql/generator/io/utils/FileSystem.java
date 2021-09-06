package com.github.alme.graphql.generator.io.utils;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.plugin.MojoExecutionException;

public class FileSystem {

	private static final String LOG_CANNOT_CREATE = "Cannot create [%s] due to error.";


	public void createDirectories(Path path) throws MojoExecutionException {
		try {
			Files.createDirectories(path);
		} catch (IOException e) {
			throw new MojoExecutionException(String.format(LOG_CANNOT_CREATE, path), e);
		}
	}

	public Writer getWriter(Path path) throws IOException {
		return Files.newBufferedWriter(path);
	}
}
