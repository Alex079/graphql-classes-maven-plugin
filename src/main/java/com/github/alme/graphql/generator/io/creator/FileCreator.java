package com.github.alme.graphql.generator.io.creator;

import java.io.IOException;
import java.io.Writer;

import com.github.alme.graphql.generator.io.WriterFactory;

import org.apache.maven.plugin.MojoExecutionException;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
abstract class FileCreator {

	private static final String LOG_CANNOT_CREATE = "Cannot create %s.%s";
	private static final String CLASS_NAME_KEY = "className";
	private static final String CURRENT_PACKAGE_KEY = "currentPackage";

	private final WriterFactory writerFactory;
	private final Configuration freemarker;

	@SneakyThrows
	public void createFile(String packageName, String className, Object baseObject) {
		try (Writer writer = writerFactory.getWriter(packageName, className)) {
			freemarker.setSharedVariable(CLASS_NAME_KEY, className);
			freemarker.setSharedVariable(CURRENT_PACKAGE_KEY, packageName);
			freemarker.getTemplate(getTemplate()).process(baseObject, writer);
		} catch (TemplateException | IOException e) {
			throw new MojoExecutionException(LOG_CANNOT_CREATE.formatted(packageName, className), e);
		}
	}

	protected abstract String getTemplate();
}
