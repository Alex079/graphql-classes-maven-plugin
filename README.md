![Maven build](https://github.com/Alex079/graphql-classes-maven-plugin/workflows/Java%20CI%20with%20Maven/badge.svg)

# GraphQL Classes Generator

This is a maven 3 plugin for generating boilerplate Java classes from GraphQL types and operations.

## Features

- Uses only GraphQL files as input
- Runs with or without POM configuration
- Can be used on both client and server side
- Generates only classes necessary to represent types and operations
- Generated code has no dependencies by default

## Goals

### generate

This goal runs the classes generation and is bound to the phase generate-sources by default.

## Use cases

### Server side classes

Usually, a server app needs all GraphQL schema types to be represented by Java classes. The plugin can be configured
to output Java classes for GraphQL schema types by setting `generatedOutputTypes` property value to `SCHEMA_TYPES`.

<details><summary>Example</summary>

The GraphQL types
```graphql
type TreeNode {
    left: TreeNode
    right: TreeNode
    value: String
}
interface Interface1 {
    switch: TreeNode
}
```
could be translated into the following Java classes
```java
package pkg;
/* imports */
public interface Interface1
{
	TreeNode getSwitch();
}
```
```java
package pkg;
/* imports */
public class TreeNode
{
	private TreeNode _left_ = null;
	public TreeNode getLeft() {
		return this._left_;
	}
	public void setLeft(TreeNode v) {
		java.util.stream.Stream.of(v)
			.forEach($ -> {});
		this._left_ = v;
	}
	private String _value_ = null;
	public String getValue() {
		return this._value_;
	}
	public void setValue(String v) {
		java.util.stream.Stream.of(v)
			.forEach($ -> {});
		this._value_ = v;
	}
	private TreeNode _right_ = null;
	public TreeNode getRight() {
		return this._right_;
	}
	public void setRight(TreeNode v) {
		java.util.stream.Stream.of(v)
			.forEach($ -> {});
		this._right_ = v;
	}
	/* equals, hashCode, toString */
}
```
</details>

### Client-defined GraphQL operations

A client app can define its own operations. Usually, there could be a file with definitions of operations and
fragments. The `generatedOutputTypes` property value should be set to `DEFINED_OPERATIONS` to enable generation of Java
classes for GraphQL operations defined in files. The following classes will be generated for each operation:

- a wrapper class, which requires variables to be set and provides the operation name, document, variables and the root result class
- a variables class, which is used by wrapper class to collect variables
- a hierarchy of result classes, which contain the fields requested in the operation

The generated result classes will contain only the requested fields. Union (or interface) result classes will contain
all the requested fields of all the requested union members (or interface implementors). Aliases must be set to avoid
name collisions for fields from different fragments.

<details><summary>Result example</summary>

Input file with operations
```graphql
fragment ValueFragment on Type1MutationField2 {
    t1Value: value
}

fragment ValueFragment on Type2MutationField2 {
    t2Value: value {
        ...ValueFragment
    }
}

fragment ValueFragment on Type3MutationField2 {
    t3Value: value
}

mutation updateField2($id: [ID!]! = ["0"]) {
    field2(arg1: $id) {
        id name
        ...on Type2MutationField2 {
            ...ValueFragment
        }
        ... on Type1MutationField2 {
            ...ValueFragment
        }
    }
}
```
could be translated into the following classes
```java
package pkg;
/* imports */
public class UpdateField2MutationResult
{
	private java.util.List<pkg.field2.Interface1MutationField2Result> _field2_ = null;
	public java.util.List<pkg.field2.Interface1MutationField2Result> getField2() {
		return this._field2_;
	}
	public void setField2(java.util.List<pkg.field2.Interface1MutationField2Result> v) {
		java.util.stream.Stream.of(v)
			.filter(java.util.Objects::nonNull)
			.flatMap(java.util.Collection::stream)
			.forEach($ -> {});
		this._field2_ = v;
	}
	/* equals, hashCode, toString */
}
```
```java
package pkg.field2;
/* imports */
public class Interface1MutationField2Result
{
	private String _name_ = null;
	public String getName() {
		return this._name_;
	}
	public void setName(String v) {
		java.util.stream.Stream.of(v)
			.map(java.util.Objects::requireNonNull)
			.forEach($ -> {});
		this._name_ = v;
	}
	private pkg.field2.t2Value.Type3MutationField2Result _t2Value_ = null;
	public pkg.field2.t2Value.Type3MutationField2Result getT2Value() {
		return this._t2Value_;
	}
	public void setT2Value(pkg.field2.t2Value.Type3MutationField2Result v) {
		java.util.stream.Stream.of(v)
			.forEach($ -> {});
		this._t2Value_ = v;
	}
	private Integer _t1Value_ = null;
	public Integer getT1Value() {
		return this._t1Value_;
	}
	public void setT1Value(Integer v) {
		java.util.stream.Stream.of(v)
			.forEach($ -> {});
		this._t1Value_ = v;
	}
	private String _id_ = null;
	public String getId() {
		return this._id_;
	}
	public void setId(String v) {
		java.util.stream.Stream.of(v)
			.map(java.util.Objects::requireNonNull)
			.forEach($ -> {});
		this._id_ = v;
	}
	/* equals, hashCode, toString */
}
```
```java
package pkg.field2.t2Value;
/* imports */
public class Type3MutationField2Result
{
	private Integer _t3Value_ = null;
	public Integer getT3Value() {
		return this._t3Value_;
	}
	public void setT3Value(Integer v) {
		java.util.stream.Stream.of(v)
			.forEach($ -> {});
		this._t3Value_ = v;
	}
	/* equals, hashCode, toString */
}
```
</details>

### Dynamic GraphQL operations

A client app may need to create GraphQL operations at run time without defining them statically in files.
This can be achieved by setting `generatedOutputTypes` property to `DYNAMIC_OPERATIONS`. The following Java classes will
be generated for each schema operation:

- a wrapper class, which requires a selection set and provides the operation document and the root result class

Additionally, the following classes, shared by all dynamic operations, will be generated:

- selector classes, which are used by wrapper classes to collect field selection set
- result classes, which contain the response fields requested in any selection set

The generated result classes will contain all possible fields. This includes union (or interface) results which are
represented by classes containing all the fields of all the union members (or interface implementors) using
automatically generated aliases to avoid field name collisions.


<details><summary>Usage example</summary>

The following code in Java
```java
public class Class {
	public Query getQuery() {
		return new DynamicQuery(querySelector -> querySelector
			.getField2(
				field2Arguments -> field2Arguments
					.setArg2(List.of(new InputQueryArg2()))
					.setAfter("123"),
				unionQueryField2ConnectionSelector -> unionQueryField2ConnectionSelector
					.getPageInfo(pageInfoSelector -> pageInfoSelector
						.getStartCursor()
						.getEndCursor()
						.getHasNextPage()
						.getHasPreviousPage())
					.getEdges(unionQueryField2ConnectionEdgeSelector -> unionQueryField2ConnectionEdgeSelector
						.getCursor()
						.getNode(unionQueryField2Selector -> unionQueryField2Selector
							.onType1UnionField2(type1UnionField2Selector -> type1UnionField2Selector
								.getFieldA()
								.getSwitch(TreeNodeSelector::getValue))
							.onType2UnionField2(type2UnionField2Selector -> type2UnionField2Selector
								.getName()
								.getInt())
							.onType3UnionField2(type3UnionField2Selector -> type3UnionField2Selector
								.getName()
								.getFieldC())))));
	}
}
```
could produce a GraphQL operation like the below
```graphql
query {
  field2(
    arg2: [{ id: null }]
    after: """123"""
  ) {
    pageInfo {
      startCursor
      endCursor
      hasNextPage
      hasPreviousPage
    }
    edges {
      cursor
      node {
        ... on Type1UnionField2 {
          fieldA_Type1UnionField2: fieldA
          switch_Type1UnionField2: switch {
            value
          }
        }
        ... on Type2UnionField2 {
          name_Type2UnionField2: name
          int_Type2UnionField2: int
        }
        ... on Type3UnionField2 {
          name_Type3UnionField2: name
          fieldC_Type3UnionField2: fieldC
        }
      }
    }
  }
}
```
</details>

### Serialization

All generated Java classes representing GraphQL types will have public getters for all fields. The private fields can be
renamed to avoid Java keyword collisions by setting `privateFieldPrefix` and/or `privateFieldSuffix`.

If a serialization engine accesses fields directly and not methods, it is possible to set `jsonPropertyAnnotation`
property (i.e.`com.google.gson.annotations.SerializedName`) to allow access by exact GraphQL field names.

If a deserialization engine uses setters, it is not possible to use the `VALUE` or `BUILDER` for `dataObjectEnhancement`
property, because in this case setter methods will not be generated.

## Mapping from GraphQL to Java

| GraphQL                               | Java                                                                                                                             |
|---------------------------------------|----------------------------------------------------------------------------------------------------------------------------------|
| `scalar`                              | `String`, matching Java class or mapped Java class                                                                               |
| `enum`                                | `enum`                                                                                                                           |
| `interface`                           | `interface` with accessors for each GraphQL field                                                                                |
| `union`                               | empty `interface` which is added to the list of implemented interfaces of Java classes representing union members                |
| `type` or `input`                     | `class` with private fields with public accessors                                                                                |
| `query`, `mutation` or `subscription` | `class` accepting variables, arguments, selection sets and providing operation name, operation document, variables, result class |
| `[]`                                  | `java.util.List`                                                                                                                 |
| `!`                                   | null check in setters                                                                                                            |

# Plugin usage

## POM

| POM Property               | Type                        | Description                                                                                                                                                                                                                                                                                                                                                       |
|----------------------------|-----------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| source                     | `FileSet`                   | Set of source files including both schema files and operation files.                                                                                                                                                                                                                                                                                              |
| outputDirectory            | `File`                      | The root directory for generated files.<br/>_Default_: `"${project.build.directory}/generated-sources/java"`                                                                                                                                                                                                                                                      |
| packageName                | `String`                    | Name of the base package for generated classes.<br/>_Default_: `"gql.generated"`                                                                                                                                                                                                                                                                                  |
| scalarMap                  | `Map<String,String>`        | Mapping of GraphQL scalars to Java classes.<br/>_Default_:<br/>Int -> Integer<br/>Float -> Double<br/>ID -> String                                                                                                                                                                                                                                                |
| aliasMap                   | `Map<String,String>`        | Mapping of GraphQL field names to GraphQL field aliases. Can be used to avoid Java keyword collisions for dynamic operations only.                                                                                                                                                                                                                                |
| importPackages             | `Set<String>`               | Set of packages to import into generated classes.                                                                                                                                                                                                                                                                                                                 |
| jsonPropertyAnnotation     | `String`                    | Annotation to be used on generated private fields.                                                                                                                                                                                                                                                                                                                |
| privateFieldPrefix         | `String`                    | Prefix to be added to generated private field names to avoid Java keywords collisions.                                                                                                                                                                                                                                                                            |
| privateFieldSuffix         | `String`                    | Suffix to be added to generated private field names to avoid Java keywords collisions.<br/>_Default_: `"__"`, when jsonPropertyAnnotation is set                                                                                                                                                                                                                  |
| generatedAnnotationVersion | `String`                    | The version of `@Generated` annotation to use on generated classes (i.e. "1.8", "11", "15", ...). At the moment, the real difference is between "1.8" and the rest.                                                                                                                                                                                               |
| dataObjectEnhancement      | `DataObjectEnhancementType` | The type of data object enhancement. Can be empty or take one of the following values: METHOD_CHAINING (data object setters will return 'this' instead of 'void'), BUILDER (data objects will use builder pattern), VALUE (data objects will use value pattern).                                                                                                  |
| generatedOutputTypes       | `Set<GeneratedOutputType>`  | The scope of the plugin output. Can be empty or take one or many values from the following list: SCHEMA_TYPES (all the types defined in GraphQL schema files), DEFINED_OPERATIONS (all the operations defined in input files), DYNAMIC_OPERATIONS (one operation per schema entry allowing to construct operations at runtime).<br/>_Default_: DEFINED_OPERATIONS |
| parserMaxTokens            | `Integer`                   | Maximum number of tokens to process by the GraphQL engine.                                                                                                                                                                                                                                                                                                        |

<details><summary>Example</summary>

```xml
<plugin>
  <groupId>com.github.alex079</groupId>
  <artifactId>graphql-classes-maven-plugin</artifactId>
  <version>${VERSION}</version>
  <configuration>
    <source>
      <includes>
        <include>schema.graphqls</include>
        <include>*.graphql</include>
      </includes>
    </source>
    <packageName>integration.test</packageName>
    <outputDirectory>${project.build.directory}\generated-gql-sources\java</outputDirectory>
    <scalarMap>
      <Address>String</Address>
    </scalarMap>
    <importPackages>
      <value>java.math</value>
    </importPackages>
    <jsonPropertyAnnotation>com.google.gson.annotations.SerializedName</jsonPropertyAnnotation>
    <dataObjectEnhancement>METHOD_CHAINING</dataObjectEnhancement>
    <generatedAnnotationVersion>1.8</generatedAnnotationVersion>
  </configuration>
  <executions>
    <execution>
      <goals>
        <goal>generate</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```
</details>

## Without POM

| Command Line Property          | Type                        | Description                                                                                                                                                                                                                                                                                                                                                         |
|--------------------------------|-----------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| gql.sourceDirectory            | `File`                      | Directory containing source files.<br/>_Default_: current directory                                                                                                                                                                                                                                                                                                 |
| gql.sourceIncludes             | `Set<String>`               | Set of patterns to include.                                                                                                                                                                                                                                                                                                                                         |
| gql.sourceExcludes             | `Set<String>`               | Set of patterns to exclude.                                                                                                                                                                                                                                                                                                                                         |
| gql.outputDirectory            | `File`                      | The root directory for generated files.<br/>_Default_: `"./generated-sources/java"`                                                                                                                                                                                                                                                                                 |
| gql.packageName                | `String`                    | Name of the base package for generated classes.<br/>_Default_: `"gql.generated"`                                                                                                                                                                                                                                                                                    |
| gql.scalarMap                  | `Set<String>`               | Mapping of GraphQL scalars to Java classes formatted as a list of key=value pairs.<br/>_Default_: `Int=Integer,Float=Double,ID=String`                                                                                                                                                                                                                              |
| gql.aliasMap                   | `Set<String>`               | Mapping of GraphQL field names to GraphQL field aliases formatted as a list of key=value pairs. Can be used to avoid Java keyword collisions for dynamic operations only.                                                                                                                                                                                           |
| gql.importPackages             | `Set<String>`               | Set of packages to import into generated classes.                                                                                                                                                                                                                                                                                                                   |
| gql.jsonPropertyAnnotation     | `String`                    | Annotation to be used on generated private fields.                                                                                                                                                                                                                                                                                                                  |
| gql.privateFieldPrefix         | `String`                    | Prefix to be added to generated private field names to avoid Java keywords collisions.                                                                                                                                                                                                                                                                              |
| gql.privateFieldSuffix         | `String`                    | Suffix to be added to generated private field names to avoid Java keywords collisions.<br/>_Default_: `"__"`, when jsonPropertyAnnotation is set                                                                                                                                                                                                                    |
| gql.generatedAnnotationVersion | `String`                    | The version of `@Generated` annotation to use on generated classes (i.e. "1.8", "11", "15", ...). At the moment, the real difference is between "1.8" and the rest.                                                                                                                                                                                                 |
| gql.dataObjectEnhancement      | `DataObjectEnhancementType` | The type of data object enhancement. Can be empty or take one of the following values: METHOD_CHAINING (data object setters will return 'this' instead of 'void'), BUILDER (data objects will use builder pattern), VALUE (data objects will use value pattern).                                                                                                    |
| gql.generatedOutputTypes       | `Set<GeneratedOutputType>`  | The scope of the plugin output. Can be empty or take one or many values from the following list: SCHEMA_TYPES (all the types defined in GraphQL schema files), DEFINED_OPERATIONS (all the operations defined in input files), DYNAMIC_OPERATIONS (one operation per schema entry allowing to construct operations at runtime).<br/>_Default_: `DEFINED_OPERATIONS` |
| gql.parserMaxTokens            | `Integer`                   | Maximum number of tokens to process by the GraphQL engine.                                                                                                                                                                                                                                                                                                          |

<details><summary>Example</summary>

```shell
mvn com.github.alex079:graphql-classes-maven-plugin:${VERSION}:generate \
-Dgql.sourceDirectory=src/main/resources \
-Dgql.sourceIncludes=*.graphql,*.graphqls \
-Dgql.outputDirectory=target/generated-sources/java \
-Dgql.importPackages=java.time \
-Dgql.scalarMap=CustomType1=String,CustomType2=Integer \
-Dgql.generatedAnnotationVersion=1.8
```
</details>
