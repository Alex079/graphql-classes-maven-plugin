package com.github.alme.graphql.generator.dto;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;

public class Context {

	private final Map<Structure, Map<String, GqlStructure>> structures = new EnumMap<>(Structure.class);

	private final Map<String, String> scalarMap = new HashMap<>();
	{ // built-in
		scalarMap.put("Int", "Integer");
		scalarMap.put("Float", "Double");
//		scalarMap.put("String", "String");
//		scalarMap.put("Boolean", "Boolean");
		scalarMap.put("ID", "String");
	}

	private String jsonPropertyAnnotation;

	private final Collection<GqlField> schema = new HashSet<>();

	private final Map<String, GqlOperation> operations = new HashMap<>();

	private final Log log;

	public Context(Log log) {
		this.log = log;
	}
	public Map<Structure, Map<String, GqlStructure>> getStructures() {
		return structures;
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
	public Collection<GqlField> getSchema() {
		return schema;
	}
	public Map<String, GqlOperation> getOperations() {
		return operations;
	}
	public Map<String, String> getScalarMap() {
		return scalarMap;
	}
	public String getJsonPropertyAnnotation() {
		return jsonPropertyAnnotation;
	}
	public void setJsonPropertyAnnotation(String v) {
		jsonPropertyAnnotation = v;
	}
	public Log getLog() {
		return log;
	}
	@Override
	public String toString() {
		return new StringBuilder()
			.append("{structures=").append(structures)
			.append(", scalarMap=").append(scalarMap)
			.append(", schema=").append(schema)
			.append(", operations=").append(operations)
			.append(", jsonPropertyAnnotation=").append(jsonPropertyAnnotation)
			.append("}").toString();

	}

}
