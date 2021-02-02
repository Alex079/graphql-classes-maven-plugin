![Maven build](https://github.com/Alex079/graphql-classes-maven-plugin/workflows/Java%20CI%20with%20Maven/badge.svg)

# GraphQL Classes Generator

This is a maven 3 plugin for generating boilerplate Java classes from GraphQL types and operations.

## Features

- Uses only GraphQL SDL files as input
- Runs with or without POM configuration
- Can be used on both server and client side
- Generates only classes necessary to represent types and operations

## Goals

### generate

This goal runs the classes generation and is bound to the phase generate-sources by default.

## Operation

The plugin uses graphql-java to parse GraphQL SDL files.
Freemarker templates are used to output Java source files.
The generated sources are added to maven build.

## Usage

### POM

|Parameter|Type|Default|Description|
|-|-|-|-|
|source|FileSet||A set of source files including both schema files and operation files|
|outputDirectory|File|"${project.build.directory}/generated-sources/java"|A root directory to create files in|
|packageName|String|"gql.generated"|A name of the package to create files in|
|scalarMap|Map\<String, String\>||A mapping of GraphQL scalars to known java classes|
|importPackages|Set\<String\>||A set of packages to import into generated classes|
|jsonPropertyAnnotation|String||An annotation to be used on generated fields to avoid java keywords collisions|
|useChainedAccessors|boolean|false|A flag indicating whether generated setters should return <b>void</b> (when set to false) or <b>this</b> (when set to true)|

```
<plugin>
  <groupId>com.github.alex079</groupId>
  <artifactId>graphql-classes-maven-plugin</artifactId>
  <version>@project.version@</version>
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
|gql.outputDirectory|File|"generated-sources/java"|A root directory to create files in|
|gql.packageName|String|"gql.generated"|A name of the package to create files in|
|gql.scalarMap|Set\<String\>||A mapping of GraphQL scalars to known java classes formatted as a list of key=value pairs|
|gql.importPackages|Set\<String\>||A set of packages to import into generated classes|
|gql.jsonPropertyAnnotation|String||An annotation to be used on generated fields to avoid java keywords collisions|
|gql.useChainedAccessors|boolean|false|A flag indicating whether generated setters should return <b>void</b> (when set to false) or <b>this</b> (when set to true)|

```
mvn com.github.alex079:graphql-classes-maven-plugin:${VERSION}:generate \
-Dgql.sourceDirectory=src/main/resources \
-Dgql.sourceIncludes=*.graphql,*.graphqls \
-Dgql.outputDirectory=target/generated-sources/java \
-Dgql.importPackages=java.time \
-Dgql.scalarMap=CustomType1=String,CustomType2=Integer
```
