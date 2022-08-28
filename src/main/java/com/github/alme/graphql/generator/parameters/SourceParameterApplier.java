package com.github.alme.graphql.generator.parameters;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.github.alme.graphql.generator.dto.GqlConfiguration.GqlConfigurationBuilder;

import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SourceParameterApplier implements ParameterApplier {

	private final MavenProject project;
	private final FileSet source;
	private final File sourceDirectoryAlternative;
	private final Collection<String> sourceIncludesAlternative;
	private final Collection<String> sourceExcludesAlternative;

	private static final String LIST_SEPARATOR = ",";

	@Override
	public void apply(GqlConfigurationBuilder builder) throws MojoExecutionException
	{
		File directory = getSourceDirectory(source, sourceDirectoryAlternative);
		String includes = getSourceIncludes(source, sourceIncludesAlternative);
		String excludes = getSourceExcludes(source, sourceExcludesAlternative);
		List<File> sourceFiles;
		try {
			sourceFiles = FileUtils.getFiles(directory, includes, excludes);
		}
		catch (IOException e) {
			throw new MojoExecutionException("Could not collect source files.", e);
		}
		if (sourceFiles.isEmpty()) {
			throw new MojoExecutionException("Could not find any source files.");
		}
		builder.sourceFiles(sourceFiles.stream().map(File::toPath).collect(Collectors.toSet()));
	}

	private File getSourceDirectory(FileSet source, File sourceDirectoryAlternative) {
		if (source == null && sourceDirectoryAlternative != null) {
			return sourceDirectoryAlternative;
		}
		if (source != null && source.getDirectory() != null) {
			return new File(source.getDirectory());
		}
		if (project.getBasedir() != null) {
			return project.getBasedir();
		}
		return new File("").getAbsoluteFile();
	}

	private static String getSourceIncludes(FileSet source, Collection<String> sourceIncludesAlternative) {
		return join(source == null ? sourceIncludesAlternative : source.getIncludes());
	}

	private static String getSourceExcludes(FileSet source, Collection<String> sourceExcludesAlternative) {
		return join(source == null ? sourceExcludesAlternative : source.getExcludes());
	}

	private static String join(Iterable<? extends CharSequence> i) {
		return i == null ? "" : String.join(LIST_SEPARATOR, i);
	}

}
