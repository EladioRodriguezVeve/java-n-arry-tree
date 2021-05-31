<!-- omit in toc -->
# Guide

<!-- omit in toc -->
### Table Of Contents

- [API](#api)
  - [NTree](#ntree)
    - [`replaceId(Object)`](#replaceidobject)
    - [`addNewRoot(NTreeNode)`](#addnewrootntreenode)
    - [`setRoot(NTreeNode)`](#setrootntreenode)
    - [`setRootSingleNode(NTreeNode)`](#setrootsinglenodentreenode)
  - [NTreeNode](#ntreenode)
  - [NearestCommonAncestorTool](#nearestcommonancestortool)
- [How To's](#how-tos)
  - [Instantiating a tree](#instantiating-a-tree)
  - [Adding all the nodes to a new tree in one statement](#adding-all-the-nodes-to-a-new-tree-in-one-statement)

---

## API

### NTree

#### `replaceId(Object)`

Replaces the id of the tree.

Example:

```java
NTree<String,Integer> tree = NTree.create("id");
tree.replaceId("newId");
```

---

#### `addNewRoot(NTreeNode)`
TODO CHANGE HOW IT WORKS!!!!!!!!!
Adds a node as the root of this tree. The node's treeOfBelonging
must be thhe same being added to and the node's parent must be null.

Example:

```java
NTree<String,Integer> tree = NTree.create("treId");
NTreeNode<String,Integer> root = new NTreeNode<String,Integer>("nodeId");
tree.addNewRoot(root);
```

---

#### `setRoot(NTreeNode)`

Creates a clone of the node and its descendants and sets the clone as root of
the tree.

Example:

```java
NTree<String,Integer> tree = ...
NTreeNode<String,Integer> root = ...
tree.setRoot(root);
```

Conceptual Diagram:

![NTree setRoot](images/NTree/setRoot.svg)

---

#### `setRootSingleNode(NTreeNode)`

Creates a clone of the node and sets the clone as the root of the tree and leaves
the old root's children as they were.

Example:

```java
NTree<String,Integer> tree = ...
NTreeNode<String,Integer> root = ...
tree.setRootSingleNode(root);
```

Conceptual Diagram:

![NTree setRootSingleNode](images/NTree/setRootSingleNode.svg)

---

### NTreeNode

### NearestCommonAncestorTool

## How To's

### Instantiating a tree

There are two ways to create an instance of `NTree`, one is using the
constructor and the other the static factory method `create(Object)`.

```java
NTree<String,Integer> tree1 = new NTree<>("treeId1");
NTree<String,Integer> tree2 = NTree.create("treeId2");
```

### Adding all the nodes to a new tree in one statement

Examples on how to create the following tree:

![desired tree](images/HowTo/createTreeInOneStatement.svg)

```java
NTree<String,Integer> t = new NTree<>("tree");
t.addNewRoot(
    t.n("A1").c(
        t.n("B1"),
        t.n("B2").c(
            t.n("C1"),
            t.n("C2")
        )
    )
);
```

```java
NTree<String,Integer> t2 = new NTree<>("tree2");
t2.addNewRoot(
    t2.createNode("A1").addNewChildren(
        t2.createNode("B1"),
        t2.createNode("B2").addNewChildren(
            t2.createNode("C1"),
            t2.createNode("C2")
        )
    )
);
```
