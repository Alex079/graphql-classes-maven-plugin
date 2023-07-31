package com.github.alme.graphql.generator.dto;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;

import lombok.Value;

@Value
public class GqlContext {

	Log log;
	Map<String, String> scalars;
	Map<String, String> aliases;
	Map<String, String> schema = new HashMap<>();
	Map<Structure, Map<String, GqlStructure>> structures = new EnumMap<>(Structure.class);
	Map<String, GqlOperation> definedOperations = new HashMap<>();
	Map<String, Map<String, Collection<GqlSelection>>> definedSelections = new HashMap<>();
	Map<String, GqlOperation> dynamicOperations = new HashMap<>();
	Map<String, Collection<GqlSelection>> dynamicSelections = new HashMap<>();

	private Map<String, GqlStructure> getStructures(Structure category) {
		return structures.computeIfAbsent(category, s -> new HashMap<>());
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

	public Map<String, GqlStructure> getInputObjectTypes() {
		return getStructures(Structure.INPUT_OBJECT);
	}

	public Map<String, GqlStructure> getUnionTypes() {
		return getStructures(Structure.UNION);
	}

}
