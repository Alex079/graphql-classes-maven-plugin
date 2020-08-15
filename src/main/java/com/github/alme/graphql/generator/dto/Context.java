package com.github.alme.graphql.generator.dto;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Data;
import org.apache.maven.plugin.logging.Log;

@Data
public class Context {

	private String jsonPropertyAnnotation;
	private final Log log;
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

	public Context(Log log) {
		this.log = log;
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
