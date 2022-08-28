package com.github.alme.graphql.generator.parameters;

import com.github.alme.graphql.generator.dto.GqlConfiguration.GqlConfigurationBuilder;

import org.apache.maven.plugin.MojoExecutionException;

public interface ParameterApplier {

	void apply(GqlConfigurationBuilder builder) throws MojoExecutionException;

}
