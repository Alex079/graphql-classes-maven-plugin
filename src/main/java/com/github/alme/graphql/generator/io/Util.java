package com.github.alme.graphql.generator.io;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import graphql.language.DescribedNode;
import graphql.language.Description;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Util {

	private static final Parser PARSER = Parser.builder().build();
	private static final HtmlRenderer RENDERER = HtmlRenderer.builder().build();

	public static String firstUpper(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	public static String firstLower(String s) {
		return s.substring(0, 1).toLowerCase() + s.substring(1);
	}

	public static List<String> extractJavadoc(DescribedNode<?> node) {
		return Optional.ofNullable(node)
			.map(DescribedNode::getDescription)
			.map(Description::getContent)
			.map(PARSER::parse)
			.map(RENDERER::render)
			.map(v -> v.split("\n"))
			.map(Arrays::asList)
			.orElseGet(Collections::emptyList);
	}

}
