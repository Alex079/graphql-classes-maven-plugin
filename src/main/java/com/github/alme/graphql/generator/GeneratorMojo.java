package com.github.alme.graphql.generator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import com.github.alme.graphql.generator.dto.Context;
import com.github.alme.graphql.generator.io.GqlReader;
import com.github.alme.graphql.generator.io.GqlWriter;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GeneratorMojo extends AbstractMojo {

	@Parameter(required = true, defaultValue = "${project.build.directory}/generated-sources/java")
	private File outputDirectory;

	@Parameter(required = true)
	private FileSet source;

	@Parameter(required = true, defaultValue = "gql.generated")
	private String packageName;

	@Parameter
	private Map<String, String> scalarMap;

	@Parameter
	private String jsonPropertyAnnotation;

	@Parameter(defaultValue="${project}")
	private MavenProject project;

	@Override
	public void execute() throws MojoExecutionException {
		if (source == null) {
			throw new MojoExecutionException("Empty source.");
		}
		List<File> files;
		try {
			files = FileUtils.getFiles(
					source.getDirectory() == null ? project.getBasedir() : new File(source.getDirectory()),
					String.join(",", source.getIncludes()),
					String.join(",", source.getExcludes()));
		} catch (IOException e) {
			throw new MojoExecutionException("Cannot transform source.", e);
		}

		Context ctx = new Context(getLog());
		if (scalarMap != null) {
			ctx.getScalarMap().putAll(scalarMap);
			getLog().info(String.format("Scalar types remapping rules: %s.", ctx.getScalarMap()));
		}
		if (jsonPropertyAnnotation != null) {
			ctx.setJsonPropertyAnnotation(jsonPropertyAnnotation);
			getLog().info(String.format("Annotating serializable properties with [%s].", jsonPropertyAnnotation));
		}

		getLog().info(String.format("Source files detected: %s.", files.toString()));
		new GqlReader().read(ctx, files);
		getLog().debug(String.format("Current context: %s.", ctx));
		new GqlWriter().write(ctx, outputDirectory.getPath(), packageName);
		getLog().info("Generation is done.");
		project.addCompileSourceRoot(outputDirectory.getPath());
	}

}