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

### Serialization

All generated Java classes representing GraphQL types will have public getters for all fields. The private fields can be
renamed to avoid Java keyword collisions by setting `privateFieldPrefix` and/or `privateFieldSuffix`.

If a serialization engine accesses fields directly and not methods, it is possible to set `jsonPropertyAnnotation`
property (i.e.`com.google.gson.annotations.SerializedName`) to allow access by exact GraphQL field names.

If a deserialization engine uses setters, it is not possible to use the `BUILDER` value for `dataObjectEnhancement`
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
| generatedAnnotationVersion | `String`                    | Version of `@Generated` annotation to use on generated classes (i.e. "1.8", "11", "15").                                                                                                                                                                                                                                                                          |
| dataObjectEnhancement      | `DataObjectEnhancementType` | The type of data object enhancement. Can be empty or take one of the following values: METHOD_CHAINING (data object setters will return 'this' instead of 'void'), BUILDER (data objects will use builder pattern).                                                                                                                                               |
| generatedOutputTypes       | `Set<GeneratedOutputType>`  | The scope of the plugin output. Can be empty or take one or many values from the following list: SCHEMA_TYPES (all the types defined in GraphQL schema files), DEFINED_OPERATIONS (all the operations defined in input files), DYNAMIC_OPERATIONS (one operation per schema entry allowing to construct operations at runtime).<br/>_Default_: DEFINED_OPERATIONS |
| parserMaxTokens            | `Integer`                   | Maximum number of tokens to process by the GraphQL engine.                                                                                                                                                                                                                                                                                                        |

### Example

```
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
    <useChainedAccessors>true</useChainedAccessors>
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

## Without POM

| Command Line Property          | Type                        | Description                                                                                                                                                                                                                                                                                                                                                                          |
|--------------------------------|-----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| gql.sourceDirectory            | `File`                      | Directory containing source files.<br/>_Default_: current directory                                                                                                                                                                                                                                                                                                                  |
| gql.sourceIncludes             | `Set<String>`               | Set of patterns to include.                                                                                                                                                                                                                                                                                                                                                          |
| gql.sourceExcludes             | `Set<String>`               | Set of patterns to exclude.                                                                                                                                                                                                                                                                                                                                                          |
| gql.outputDirectory            | `File`                      | The root directory for generated files.<br/>_Default_: `"./generated-sources/java"`                                                                                                                                                                                                                                                                                                  |
| gql.packageName                | `String`                    | Name of the base package for generated classes.<br/>_Default_: `"gql.generated"`                                                                                                                                                                                                                                                                                                     |
| gql.scalarMap                  | `Set<String>`               | Mapping of GraphQL scalars to Java classes formatted as a list of key=value pairs.<br/>_Default_: `Int=Integer,Float=Double,ID=String`                                                                                                                                                                                                                                               |
| gql.aliasMap                   | `Set<String>`               | Mapping of GraphQL field names to GraphQL field aliases formatted as a list of key=value pairs. Can be used to avoid Java keyword collisions for dynamic operations only.                                                                                                                                                                                                            |
| gql.importPackages             | `Set<String>`               | Set of packages to import into generated classes.                                                                                                                                                                                                                                                                                                                                    |
| gql.jsonPropertyAnnotation     | `String`                    | Annotation to be used on generated private fields.                                                                                                                                                                                                                                                                                                                                   |
| gql.privateFieldPrefix         | `String`                    | Prefix to be added to generated private field names to avoid Java keywords collisions.                                                                                                                                                                                                                                                                                               |
| gql.privateFieldSuffix         | `String`                    | Suffix to be added to generated private field names to avoid Java keywords collisions.<br/>_Default_: `"__"`, when jsonPropertyAnnotation is set                                                                                                                                                                                                                                     |
| gql.generatedAnnotationVersion | `String`                    | Version of `@Generated` annotation to use on generated classes (i.e. "1.8", "11", "15").                                                                                                                                                                                                                                                                                             |
| gql.dataObjectEnhancement      | `DataObjectEnhancementType` | The type of data object enhancement. Can be empty or take one of the following values: METHOD_CHAINING (data object setters will return 'this' instead of 'void'), BUILDER (data objects will use builder pattern).                                                                                                                                                                  |
| gql.generatedOutputTypes       | `Set<GeneratedOutputType>`  | The scope of the plugin output. Can be empty or take one or many values from the following list: SCHEMA_TYPES (all the types defined in GraphQL schema files), DEFINED_OPERATIONS (_default value_, all the operations defined in input files), DYNAMIC_OPERATIONS (one operation per schema entry allowing to construct operations at runtime).<br/>_Default_: `DEFINED_OPERATIONS` |
| gql.parserMaxTokens            | `Integer`                   | Maximum number of tokens to process by the GraphQL engine.                                                                                                                                                                                                                                                                                                                           |

### Example

```
mvn com.github.alex079:graphql-classes-maven-plugin:${VERSION}:generate \
-Dgql.sourceDirectory=src/main/resources \
-Dgql.sourceIncludes=*.graphql,*.graphqls \
-Dgql.outputDirectory=target/generated-sources/java \
-Dgql.importPackages=java.time \
-Dgql.scalarMap=CustomType1=String,CustomType2=Integer \
-Dgql.generatedAnnotationVersion=1.8
```
