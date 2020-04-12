![Maven build](https://github.com/Alex079/graphql-classes-maven-plugin/workflows/Java%20CI%20with%20Maven/badge.svg)

# GraphQL Classes Generator

This is a maven 3.x plugin intended to simplify communication with schema-first GraphQL servers from client applications written in Java. This plugin can generate client-side java classes for GraphQL operations using only schema file.

## Goals

### generate

The only goal of this plugin. This goal runs the classes generation and is bound to the phase generate-sources by default.

## Usage

### Configuration

|Parameter|Type|Required|Default|Description|
|-|-|-|-|-|
|outputDirectory|String|+|"${project.build.directory}/generated-sources/java"|A root directory to create files in|
|source|FileSet|+||A set of source files including both schema files and operation files|
|packageName|String|+|gql.generated|A name of the package to create files in|
|scalarMap|Map<String, String>|||A mapping of GraphQL scalars to known java classes|
|jsonPropertyAnnotation|String|||An annotation to be used on generated fields to avoid java keywords collisions|

### Example

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
    <outputDirectory>${project.build.directory}\generated-it-test-sources\java</outputDirectory>
    <scalarMap>
      <Address>String</Address>
    </scalarMap>
    <jsonPropertyAnnotation>com.google.gson.annotations.SerializedName</jsonPropertyAnnotation>
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

## Operation

The plugin uses graphql-java internally to build an AST and freemarker to create Java files form the AST. The generation process is therefore split in two steps: AST generation and files creation. The AST produced by graphql-java is compacted into a custom reduced AST which excludes optional content.
The actual client code generation is intentionally left out of the scope of this plugin.
