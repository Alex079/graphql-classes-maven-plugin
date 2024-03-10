package com.github.alme.graphql.generator.parameters;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import com.github.alme.graphql.generator.dto.GqlConfiguration.GqlConfigurationBuilder;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AliasMapParameterApplier implements ParameterApplier {

	private final Map<String, String> aliasMap;
	private final Collection<String> aliasMapAlternative;

	private static final String KEY_VALUE_SEPARATOR = "=";

	private static final Map<String, String> DEFAULT_ALIASES = Map.ofEntries(
		Map.entry("abstract", "abstractValue"),
		Map.entry("assert", "assertValue"),
		Map.entry("boolean", "booleanValue"),
		Map.entry("break", "breakValue"),
		Map.entry("byte", "byteValue"),
		Map.entry("case", "caseValue"),
		Map.entry("catch", "catchValue"),
		Map.entry("char", "charValue"),
		Map.entry("class", "classValue"),
		Map.entry("const", "constValue"),
		Map.entry("continue", "continueValue"),
		Map.entry("default", "defaultValue"),
		Map.entry("do", "doValue"),
		Map.entry("double", "doubleValue"),
		Map.entry("else", "elseValue"),
		Map.entry("enum", "enumValue"),
		Map.entry("extends", "extendsValue"),
		Map.entry("final", "finalValue"),
		Map.entry("finally", "finallyValue"),
		Map.entry("float", "floatValue"),
		Map.entry("for", "forValue"),
		Map.entry("goto", "gotoValue"),
		Map.entry("if", "ifValue"),
		Map.entry("implements", "implementsValue"),
		Map.entry("import", "importValue"),
		Map.entry("instanceof", "instanceofValue"),
		Map.entry("int", "intValue"),
		Map.entry("interface", "interfaceValue"),
		Map.entry("long", "longValue"),
		Map.entry("native", "nativeValue"),
		Map.entry("new", "newValue"),
		Map.entry("package", "packageValue"),
		Map.entry("private", "privateValue"),
		Map.entry("protected", "protectedValue"),
		Map.entry("public", "publicValue"),
		Map.entry("return", "returnValue"),
		Map.entry("short", "shortValue"),
		Map.entry("static", "staticValue"),
		Map.entry("strictfp", "strictfpValue"),
		Map.entry("super", "superValue"),
		Map.entry("switch", "switchValue"),
		Map.entry("synchronized", "synchronizedValue"),
		Map.entry("this", "thisValue"),
		Map.entry("throw", "throwValue"),
		Map.entry("throws", "throwsValue"),
		Map.entry("transient", "transientValue"),
		Map.entry("try", "tryValue"),
		Map.entry("void", "voidValue"),
		Map.entry("volatile", "volatileValue"),
		Map.entry("while", "whileValue"),
		Map.entry("yield", "yieldValue")
	);

	@Override
	public void apply(GqlConfigurationBuilder builder) {
		builder.aliases(DEFAULT_ALIASES);
		if (aliasMap != null) {
			aliasMap.entrySet().stream()
				.filter(item ->
					item.getKey() != null && item.getValue() != null &&
					!item.getKey().isBlank() && !item.getValue().isBlank())
				.forEach(item -> builder.alias(item.getKey().trim(), item.getValue().trim()));
		}
		else if (aliasMapAlternative != null) {
			aliasMapAlternative.stream()
				.filter(Objects::nonNull)
				.map(item -> item.split(KEY_VALUE_SEPARATOR, 2))
				.filter(item -> (item.length == 2) && !item[0].isBlank() && !item[1].isBlank())
				.forEach(item -> builder.alias(item[0].trim(), item[1].trim()));
		}
	}

}
