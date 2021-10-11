package com.github.alme.graphql.generator.io;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.javaparser.ast.CompilationUnit;

import org.apache.maven.plugin.MojoExecutionException;

public class WriterFactory {

	private static final String LOG_CANNOT_CREATE = "Cannot create [%s] due to error.";
	private static final String FILE_EXTENSION = ".java";

	public WriterFactory(Path path) throws MojoExecutionException {
		try {
			Files.createDirectories(path);
		} catch (IOException e) {
			throw new MojoExecutionException(String.format(LOG_CANNOT_CREATE, path), e);
		}
	}

	public Writer getWriter(Path path) throws IOException {
		return Files.newBufferedWriter(path);
	}

	public void writeCompilationUnit(CompilationUnit compilationUnit, Path path, String name) {
		compilationUnit.setStorage(path.resolve(name + FILE_EXTENSION)).getStorage().ifPresent(CompilationUnit.Storage::save);
	}

}
