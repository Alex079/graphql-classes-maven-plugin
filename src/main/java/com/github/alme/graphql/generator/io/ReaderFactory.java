package com.github.alme.graphql.generator.io;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;

import org.apache.maven.plugin.logging.Log;

import graphql.parser.MultiSourceReader;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReaderFactory {

	private final Collection<Path> sources;
	private final Log log;

	public Reader getReader() {
		return sources.stream()
			.map(path -> {
				try {
					return new FileInfo(Files.newBufferedReader(path), path.toString());
				} catch (IOException e) {
					log.error("Skipping [%s].".formatted(path), e);
					return null;
				}
			})
			.filter(Objects::nonNull)
			.reduce(
				MultiSourceReader.newMultiSourceReader(),
				(multiReader, fileInfo) -> multiReader.reader(fileInfo.reader, fileInfo.path),
				(r1, r2) -> null /* unused */)
			.build();
	}

	private record FileInfo(Reader reader, String path) { }

}
