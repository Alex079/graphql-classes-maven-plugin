![Maven build](https://github.com/Alex079/graphql-classes-maven-plugin/workflows/Java%20CI%20with%20Maven/badge.svg)

# GraphQL Classes Generator

This is a maven 3 plugin for generating boilerplate Java classes from GraphQL types and operations.

## Features

- Uses only GraphQL SDL files as input
- Runs with or without POM configuration
- Can be used on both client and server side (types generated on server side can be used as return types for data fetchers or resolvers)
- Generates only classes necessary to represent types and operations
- Generated code has no dependencies by default

## Goals

### generate

This goal runs the classes generation and is bound to the phase generate-sources by default.

## Details

The plugin uses graphql-java and graphql-java-tools to parse GraphQL SDL files.
Freemarker templates are used to output Java source files.
The generated sources are added to maven build.

### Mapping from GraphQL to Java

<table>
  <thead>
    <tr>
      <th>GraphQL</th>
      <th>Java</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>enum</td>
      <td>enum</td>
    </tr>
    <tr>
      <td>interface</td>
      <td>interface with accessors for each GraphQL field</td>
    </tr>
    <tr>
      <td>union</td>
      <td>empty interface which is added to the list of implemented interfaces of Java classes representing union members</td>
    </tr>
    <tr>
      <td>type or input</td>
      <td>class with<br/>
- private fields with public accessors<br/>
- <code>String toString()</code><br/>
- <code>boolean equals(Object)</code><br/>
- <code>int hashCode()</code></td>
    </tr>
    <tr>
      <td>operation</td>
      <td>class with<br/>
- <code>String getDocument()</code><br/>
- <code>String getOperation()</code><br/>
-	<code>Variables getVariables()</code><br/>
-	<code>Map&lt;String, Object&gt; getVariablesAsMap()</code><br/>
-	<code>Class&lt;Result&gt; getResultClass()</code><br/>
- nested public static classes corresponding to field selection set with aliases and fragments resolved</td>
    </tr>
    <tr>
      <td>[]</td>
      <td>java.util.List</td>
    </tr>
    <tr>
      <td>!</td>
      <td>null check in setters</td>
    </tr>
  </tbody>
</table>

## Usage

### POM

|Parameter|Type|Default|Description|
|-|-|-|-|
|source|FileSet||A set of source files including both schema files and operation files|
|outputDirectory|File|"${project.build.directory}/generated-sources/java"|A root directory for generated files|
|packageName|String|"gql.generated"|A name of base package for generated classes|
|scalarMap|Map\<String, String\>||A mapping of GraphQL scalars to known java classes|
|importPackages|Set\<String\>||A set of packages to import into generated classes|
|jsonPropertyAnnotation|String||An annotation to be used on generated fields to avoid java keywords collisions|
|useChainedAccessors|boolean|false|A flag indicating whether generated setters should return <b>void</b> (when set to false) or <b>this</b> (when set to true)|
|generatedAnnotationVersion|String||A version of '@Generated' annotation to use on generated classes (1.8, 11, 15)|

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

### Without POM

|Property|Type|Default|Description|
|-|-|-|-|
|gql.sourceDirectory|File|(current directory)|A directory containing source files|
|gql.sourceIncludes|Set\<String\>||A set of patterns to include|
|gql.sourceExcludes|Set\<String\>||A set of patterns to exclude|
|gql.outputDirectory|File|"generated-sources/java"|A root directory for generated files|
|gql.packageName|String|"gql.generated"|A name of base package for generated classes|
|gql.scalarMap|Set\<String\>||A mapping of GraphQL scalars to known java classes formatted as a list of key=value pairs|
|gql.importPackages|Set\<String\>||A set of packages to import into generated classes|
|gql.jsonPropertyAnnotation|String||An annotation to be used on generated fields to avoid java keywords collisions|
|gql.useChainedAccessors|boolean|false|A flag indicating whether generated setters should return <b>void</b> (when set to false) or <b>this</b> (when set to true)|
|gql.generatedAnnotationVersion|String||A version of '@Generated' annotation to use on generated classes (1.8, 11, 15)|

```
mvn com.github.alex079:graphql-classes-maven-plugin:${VERSION}:generate \
-Dgql.sourceDirectory=src/main/resources \
-Dgql.sourceIncludes=*.graphql,*.graphqls \
-Dgql.outputDirectory=target/generated-sources/java \
-Dgql.importPackages=java.time \
-Dgql.scalarMap=CustomType1=String,CustomType2=Integer \
-Dgql.generatedAnnotationVersion=1.8
```

## Examples

<table>
  <thead>
    <tr>
      <th>GraphQL</th>
      <th>Java</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>
<pre>
type Sample {
	id: ID!
	scalarAddress: Address!
	number: Int!
}
</pre>
      </td>
      <td>
<pre>
public class Sample {
	private String id;
	public String getId() { ... }
	public void setId(String v) { ... }
	private Integer number;
	public Integer getNumber() { ... }
	public void setNumber(Integer v) { ... }
	private String scalarAddress;
	public String getScalarAddress() { ... }
	public void setScalarAddress(String v) { ... }
	...
}
</pre>
      </td>
    </tr>
  </tbody>
</table>
