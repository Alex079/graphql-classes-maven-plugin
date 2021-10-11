package com.github.alme.graphql.generator.writer;

import java.time.Instant;

import com.github.alme.graphql.generator.GeneratorMojo;
import com.github.alme.graphql.generator.dto.GqlConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Util {

	private static final String JAVA8 = "1.8";
	private static final String JAVA8_GENERATED = "javax.annotation.Generated";
	private static final String JAVA_GENERATED = "javax.annotation.processing.Generated";

	public static String capitalize(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	public static String getter(String s) {
		return "get" + capitalize(s);
	}

	public static String setter(String s) {
		return "set" + capitalize(s);
	}

	public static void addImports(CompilationUnit compilationUnit, GqlConfiguration configuration) {
		configuration.getImportPackages().forEach(i -> compilationUnit.addImport(i, false, true));
	}

	public static void addClassAnnotations(NodeWithAnnotations<?> declaration, GqlConfiguration configuration) {
		String generatedAnnotationVersion = configuration.getGeneratedAnnotationVersion();
		if (generatedAnnotationVersion != null) {
			declaration.addAndGetAnnotation((JAVA8.equals(generatedAnnotationVersion)) ? JAVA8_GENERATED : JAVA_GENERATED)
				.addPair("value", new StringLiteralExpr(GeneratorMojo.class.getName()))
				.addPair("date", new StringLiteralExpr(Instant.now().toString()));
		}
		declaration.addSingleMemberAnnotation(SuppressWarnings.class, new ArrayInitializerExpr(NodeList.nodeList(
			new StringLiteralExpr("all"),
			new StringLiteralExpr("PMD")
		)));
	}

}
