package com.github.alme.graphql.generator.parameters;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.github.alme.graphql.generator.dto.GqlConfiguration.GqlConfigurationBuilder;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AliasMapParameterApplier implements ParameterApplier {

	private final Map<String, String> aliasMap;
	private final Collection<String> aliasMapAlternative;

	private static final String KEY_VALUE_SEPARATOR = "=";
	
	private static final Map<String, String> DEFAULT_ALIASES = new HashMap<>();
	static {
		DEFAULT_ALIASES.put("abstract", "abstractValue");
		DEFAULT_ALIASES.put("assert", "assertValue");
		DEFAULT_ALIASES.put("boolean", "booleanValue");
		DEFAULT_ALIASES.put("break", "breakValue");
		DEFAULT_ALIASES.put("byte", "byteValue");
		DEFAULT_ALIASES.put("case", "caseValue");
		DEFAULT_ALIASES.put("catch", "catchValue");
		DEFAULT_ALIASES.put("char", "charValue");
		DEFAULT_ALIASES.put("class", "classValue");
		DEFAULT_ALIASES.put("const", "constValue");
		DEFAULT_ALIASES.put("continue", "continueValue");
		DEFAULT_ALIASES.put("default", "defaultValue");
		DEFAULT_ALIASES.put("do", "doValue");
		DEFAULT_ALIASES.put("double", "doubleValue");
		DEFAULT_ALIASES.put("else", "elseValue");
		DEFAULT_ALIASES.put("enum", "enumValue");
		DEFAULT_ALIASES.put("extends", "extendsValue");
		DEFAULT_ALIASES.put("final", "finalValue");
		DEFAULT_ALIASES.put("finally", "finallyValue");
		DEFAULT_ALIASES.put("float", "floatValue");
		DEFAULT_ALIASES.put("for", "forValue");
		DEFAULT_ALIASES.put("goto", "gotoValue");
		DEFAULT_ALIASES.put("if", "ifValue");
		DEFAULT_ALIASES.put("implements", "implementsValue");
		DEFAULT_ALIASES.put("import", "importValue");
		DEFAULT_ALIASES.put("instanceof", "instanceofValue");
		DEFAULT_ALIASES.put("int", "intValue");
		DEFAULT_ALIASES.put("interface", "interfaceValue");
		DEFAULT_ALIASES.put("long", "longValue");
		DEFAULT_ALIASES.put("native", "nativeValue");
		DEFAULT_ALIASES.put("new", "newValue");
		DEFAULT_ALIASES.put("package", "packageValue");
		DEFAULT_ALIASES.put("private", "privateValue");
		DEFAULT_ALIASES.put("protected", "protectedValue");
		DEFAULT_ALIASES.put("public", "publicValue");
		DEFAULT_ALIASES.put("return", "returnValue");
		DEFAULT_ALIASES.put("short", "shortValue");
		DEFAULT_ALIASES.put("static", "staticValue");
		DEFAULT_ALIASES.put("strictfp", "strictfpValue");
		DEFAULT_ALIASES.put("super", "superValue");
		DEFAULT_ALIASES.put("switch", "switchValue");
		DEFAULT_ALIASES.put("synchronized", "synchronizedValue");
		DEFAULT_ALIASES.put("this", "thisValue");
		DEFAULT_ALIASES.put("throw", "throwValue");
		DEFAULT_ALIASES.put("throws", "throwsValue");
		DEFAULT_ALIASES.put("transient", "transientValue");
		DEFAULT_ALIASES.put("try", "tryValue");
		DEFAULT_ALIASES.put("void", "voidValue");
		DEFAULT_ALIASES.put("volatile", "volatileValue");
		DEFAULT_ALIASES.put("while", "whileValue");
		DEFAULT_ALIASES.put("yield", "yieldValue");
	}

	@Override
	public void apply(GqlConfigurationBuilder builder) {
		builder.aliases(DEFAULT_ALIASES);
		if (aliasMap != null) {
			aliasMap.entrySet().stream()
				.filter(item ->
					item.getKey() != null && item.getValue() != null &&
					!item.getKey().trim().isEmpty() && !item.getValue().trim().isEmpty())
				.forEach(item -> builder.alias(item.getKey().trim(), item.getValue().trim()));
		}
		else if (aliasMapAlternative != null) {
			aliasMapAlternative.stream()
				.filter(Objects::nonNull)
				.map(item -> item.split(KEY_VALUE_SEPARATOR, 2))
				.filter(item -> (item.length == 2) && !item[0].trim().isEmpty() && !item[1].trim().isEmpty())
				.forEach(item -> builder.alias(item[0].trim(), item[1].trim()));
		}
	}

}
