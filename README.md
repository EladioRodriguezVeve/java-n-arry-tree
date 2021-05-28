# n-arry-tree

Author: Eladio Rodriguez Veve

## About n-arry-tree

This Java library lets you create and manipulate n-arry trees. An n-arry tree is a tree composed of nodes where each node has zero or any number of child nodes. The tree has zero or one root node.

The class that represents the tree is `NTree` and the one for the nodes is `NTreeNode`.
Each node has an id and a value field. Nodes that are siblings cannot have the same id, similar to how two files cannot have the same name in a file system.

There is a utility class named `NearestCommonAncestorTool` that can be used to find the nearest common ancestor between two nodes in a
node hierarchy

## How to Build

> Note: this library was built with OpenJDK 11 and is is targeted for Java 11.

Clone this repo and from the root of the repo run `./gradlew build`

## Javadocs

Click [here](javadocs/index.html) to access the javadocs.

## How to Use Guide

Click [here](guide/guide.md) to access the guide.
