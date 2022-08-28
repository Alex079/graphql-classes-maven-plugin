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
	
	private static final Map<String, String> FORCED_ALIASES = new HashMap<>();

	static {
		FORCED_ALIASES.put("abstract", "abstractValue");
		FORCED_ALIASES.put("assert", "assertValue");
		FORCED_ALIASES.put("boolean", "booleanValue");
		FORCED_ALIASES.put("break", "breakValue");
		FORCED_ALIASES.put("byte", "byteValue");
		FORCED_ALIASES.put("case", "caseValue");
		FORCED_ALIASES.put("catch", "catchValue");
		FORCED_ALIASES.put("char", "charValue");
		FORCED_ALIASES.put("class", "classValue");
		FORCED_ALIASES.put("const", "constValue");
		FORCED_ALIASES.put("continue", "continueValue");
		FORCED_ALIASES.put("default", "defaultValue");
		FORCED_ALIASES.put("do", "doValue");
		FORCED_ALIASES.put("double", "doubleValue");
		FORCED_ALIASES.put("else", "elseValue");
		FORCED_ALIASES.put("enum", "enumValue");
		FORCED_ALIASES.put("extends", "extendsValue");
		FORCED_ALIASES.put("final", "finalValue");
		FORCED_ALIASES.put("finally", "finallyValue");
		FORCED_ALIASES.put("float", "floatValue");
		FORCED_ALIASES.put("for", "forValue");
		FORCED_ALIASES.put("goto", "gotoValue");
		FORCED_ALIASES.put("if", "ifValue");
		FORCED_ALIASES.put("implements", "implementsValue");
		FORCED_ALIASES.put("import", "importValue");
		FORCED_ALIASES.put("instanceof", "instanceofValue");
		FORCED_ALIASES.put("int", "intValue");
		FORCED_ALIASES.put("interface", "interfaceValue");
		FORCED_ALIASES.put("long", "longValue");
		FORCED_ALIASES.put("native", "nativeValue");
		FORCED_ALIASES.put("new", "newValue");
		FORCED_ALIASES.put("package", "packageValue");
		FORCED_ALIASES.put("private", "privateValue");
		FORCED_ALIASES.put("protected", "protectedValue");
		FORCED_ALIASES.put("public", "publicValue");
		FORCED_ALIASES.put("return", "returnValue");
		FORCED_ALIASES.put("short", "shortValue");
		FORCED_ALIASES.put("static", "staticValue");
		FORCED_ALIASES.put("strictfp", "strictfpValue");
		FORCED_ALIASES.put("super", "superValue");
		FORCED_ALIASES.put("switch", "switchValue");
		FORCED_ALIASES.put("synchronized", "synchronizedValue");
		FORCED_ALIASES.put("this", "thisValue");
		FORCED_ALIASES.put("throw", "throwValue");
		FORCED_ALIASES.put("throws", "throwsValue");
		FORCED_ALIASES.put("transient", "transientValue");
		FORCED_ALIASES.put("try", "tryValue");
		FORCED_ALIASES.put("void", "voidValue");
		FORCED_ALIASES.put("volatile", "volatileValue");
		FORCED_ALIASES.put("while", "whileValue");
	}

	@Override
	public void apply(GqlConfigurationBuilder builder) {
		builder.aliases(FORCED_ALIASES);
		if (aliasMap != null) {
			aliasMap.entrySet().stream()
				.filter(item ->
					item.getKey() != null && item.getValue() != null &&
					item.getKey().trim().length() > 0 && item.getValue().trim().length() > 0)
				.forEach(item -> builder.alias(item.getKey().trim(), item.getValue().trim()));
		}
		else if (aliasMapAlternative != null) {
			aliasMapAlternative.stream()
				.filter(Objects::nonNull)
				.map(item -> item.split(KEY_VALUE_SEPARATOR, 2))
				.filter(item -> (item.length == 2) && item[0].trim().length() > 0 && item[1].trim().length() > 0)
				.forEach(item -> builder.alias(item[0].trim(), item[1].trim()));
		}
	}

}
