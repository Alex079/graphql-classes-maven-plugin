package com.github.alme.graphql.generator.parameters;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import com.github.alme.graphql.generator.dto.GqlConfiguration.GqlConfigurationBuilder;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

public class SourceParameterApplier implements ParameterApplier {

	private final FileSet source;

	public SourceParameterApplier(MavenProject project, FileSet source,
			String sourceDirectoryAlternative, Collection<String> sourceIncludesAlternative, Collection<String> sourceExcludesAlternative) {
		if (source == null) {
			this.source = new FileSet();
			this.source.setDirectory(sourceDirectoryAlternative);
			this.source.setIncludes(new ArrayList<>(sourceIncludesAlternative));
			this.source.setExcludes(new ArrayList<>(sourceExcludesAlternative));
		}
		else {
			this.source = source;
		}
		if (this.source.getDirectory() == null) {
			File defaultDirectory = project.getBasedir() == null ? new File("") : project.getBasedir();
			this.source.setDirectory(defaultDirectory.getAbsolutePath());
		}
	}

	@Override
	public void apply(GqlConfigurationBuilder builder) throws MojoExecutionException
	{
		String[] includedFiles = new FileSetManager().getIncludedFiles(source);
		if (includedFiles.length == 0) {
			throw new MojoExecutionException("Could not find any source files.");
		}
		builder.sourceFiles(Arrays.stream(includedFiles).map(file -> Paths.get(source.getDirectory(), file)).collect(Collectors.toSet()));
	}

}
