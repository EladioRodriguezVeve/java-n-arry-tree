# n-arry-tree

Author: Eladio Rodriguez Veve

## About n-arry-tree

This Java library lets you create and manipulate n-arry trees. An n-arry tree is
a tree composed of nodes where each node has zero or any number of child nodes.
The tree has zero or one root node.

The class that represents the tree is `NTree` and the one for the nodes is
`NTreeNode`. Each node has an id and a value field. Nodes that are siblings
cannot have the same id, similar to how two files cannot have the same name in a
file system.

There is a utility class named `NearestCommonAncestorTool` that can be used to
find the nearest common ancestor between two nodes in a node hierarchy.

## How to Build

> Note: this library was built with OpenJDK 11 and is is targeted for Java 11.

Clone this repo and from the root of the repo run `./gradlew build`

## How to add this library as a dependency using Maven

In your Maven pom file:

```xml
<repositories>
<repository>
    <id>central</id>
    <name>Central Repository</name>
    <url>https://repo.maven.apache.org/maven2</url>
</repository>
<!-- Other repositories -->
</repositories>

<dependencies>
<dependency>
    <groupId>io.github.eladiorodriguezveve</groupId>
    <artifactId>n-arry-tree</artifactId>
    <version>1.0.0</version>
</dependency>
<!-- Other dependencies -->
</dependencies>
```

## How to add this library as a dependency using Gradle

In your build.gradle file:

```groovy
// Declaring plugins, etc

repositories {
    mavenCentral()
    // Other repositories
}

dependencies {
    implementation 'io.github.eladiorodriguezveve:n-arry-tree:1.0.0'
    // Other dependencies
}
```

## Javadocs

Click [here](https://www.javadoc.io/doc/io.github.eladiorodriguezveve/n-arry-tree/1.0.0/veve/datastructures/trees/package-summary.html) to open the Javadocs

## Guide

Click [here](guide/guide.md) to access the guide.
