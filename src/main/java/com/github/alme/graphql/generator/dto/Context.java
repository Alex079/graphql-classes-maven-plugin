package com.github.alme.graphql.generator.dto;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.logging.Log;

import lombok.Data;

@Data
public class Context {

	private static final String TYPES_SUBPACKAGE = "types";
	private static final String SUBPACKAGE_SEPARATOR = ".";

	private String jsonPropertyAnnotation;
	private boolean useChainedAccessors;

	private final Log log;
	private final String basePackageName;
	private final String typesPackageName;
	private final Path basePackagePath;
	private final Path typesPackagePath;
	private final Map<String, String> schema = new HashMap<>();
	private final Map<Structure, Map<String, GqlStructure>> structures = new EnumMap<>(Structure.class);
	private final Map<String, GqlOperation> operations = new HashMap<>();
	private final Map<String, String> scalarMap = new HashMap<>();
	private final Set<String> importPackages = new HashSet<>();

	{ // built-in
		scalarMap.put("Int", "Integer");
		scalarMap.put("Float", "Double");
		scalarMap.put("ID", "String");
		importPackages.add("java.util");
	}

	public Context(Log log, String packageName, String outputRoot) {
		this.log = log;
		this.basePackageName = packageName;
		this.typesPackageName = packageName + SUBPACKAGE_SEPARATOR + TYPES_SUBPACKAGE;
		this.basePackagePath = Paths.get(outputRoot, packageName.replace(SUBPACKAGE_SEPARATOR, File.separator));
		this.typesPackagePath = Paths.get(outputRoot, typesPackageName.replace(SUBPACKAGE_SEPARATOR, File.separator));
	}

	private Map<String, GqlStructure> getStructures(Structure category) {
		return structures.computeIfAbsent(category, (k) -> new HashMap<>());
	}

	public Map<String, GqlStructure> getInterfaceTypes() {
		return getStructures(Structure.INTERFACE);
	}

	public Map<String, GqlStructure> getEnumTypes() {
		return getStructures(Structure.ENUM);
	}

	public Map<String, GqlStructure> getObjectTypes() {
		return getStructures(Structure.OBJECT);
	}

	public Map<String, GqlStructure> getUnionTypes() {
		return getStructures(Structure.UNION);
	}

}
