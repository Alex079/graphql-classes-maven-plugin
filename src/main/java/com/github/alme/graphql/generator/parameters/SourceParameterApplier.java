package com.github.alme.graphql.generator.parameters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.alme.graphql.generator.dto.GqlConfiguration.GqlConfigurationBuilder;

import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

public class SourceParameterApplier implements ParameterApplier {

	private final Set<FileSet> sources;
	private final File baseDirectory;
	private static final String LIST_SEPARATOR = ",";

	public SourceParameterApplier(
		MavenProject project, Collection<FileSet> sources, FileSet source,
		String sourceDirectoryAlternative, Collection<String> sourceIncludesAlternative, Collection<String> sourceExcludesAlternative
	) {
		this.sources = new HashSet<>();
		if (sources != null) {
			this.sources.addAll(sources);
		}
		if (source != null) {
			this.sources.add(source);
		}
		if (this.sources.isEmpty()) {
			this.sources.add(getAlternative(sourceDirectoryAlternative, sourceIncludesAlternative, sourceExcludesAlternative));
		}
		this.baseDirectory = Optional.ofNullable(project.getBasedir()).orElseGet(() -> new File("").getAbsoluteFile());
	}

	private static FileSet getAlternative(String directory, Collection<String> includes, Collection<String> excludes) {
		FileSet source;
		source = new FileSet();
		source.setDirectory(directory);
		source.setIncludes(new ArrayList<>(includes));
		source.setExcludes(new ArrayList<>(excludes));
		return source;
	}

	@Override
	public void apply(GqlConfigurationBuilder builder) throws MojoExecutionException {
		Set<Path> sourceFiles = sources.stream()
			.map(this::getFileNames)
			.flatMap(Collection::stream)
			.map(Paths::get)
			.collect(Collectors.toSet());
		if (sourceFiles.isEmpty()) {
			throw new MojoExecutionException("Could not find any source files.");
		}
		builder.sourceFiles(sourceFiles);
	}

	private List<String> getFileNames(FileSet source) {
		try {
			return FileUtils.getFileNames(
				Optional.ofNullable(source.getDirectory()).map(File::new).map(File::getAbsoluteFile).orElse(baseDirectory),
				String.join(LIST_SEPARATOR, source.getIncludes()),
				String.join(LIST_SEPARATOR, source.getExcludes()),
				true);
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not process " + source, e);
		}
	}

}
