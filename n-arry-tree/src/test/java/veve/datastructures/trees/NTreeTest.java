package veve.datastructures.trees;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.gson.reflect.TypeToken;

import veve.datastructures.trees.NTreeConstants.NodeValueCloningMode;

public class NTreeTest {
	
	static final String IDS_INDEX = "idsIndex";
	static final String VALUES_INDEX = "valuesIndex";
	
	@Test void test_nodeValueCloningUsesCopyConstructor() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.nodeValueCloningUsesCopyConstructor();
		assertEquals(NodeValueCloningMode.BY_COPY_CONSTRUCTOR, tree.getNodeValueCloningMode());
	}
	
	@Test void test_nodeValueCloningUsesSerialization() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.nodeValueCloningUsesSerialization();
		assertEquals(NodeValueCloningMode.BY_SERIALIZATION, tree.getNodeValueCloningMode());
		assertEquals(null, tree.getNodeValueType());
	}
	
	@Test void test_nodeValueCloningUsesSerializationWithType() {
		NTree<String,Integer> tree = NTree.create("tree");
		Type type = new TypeToken<List<NTreeNode<String,String>>>(){}.getType();
		tree.nodeValueCloningUsesSerialization(type);
		assertEquals(NodeValueCloningMode.BY_SERIALIZATION, tree.getNodeValueCloningMode());
		assertEquals(type, tree.getNodeValueType());
	}
	
	@Test void test_isUnordered_default() {
		NTree<String,Integer> tree = NTree.create("tree");
		assertTrue(tree.isUnordered());
	}
	
	@Test void test_isUnordered() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.useNaturalOrdering();
		tree.dontUseOrdering();
		assertTrue(tree.isUnordered());
	}
	
	@Test void test_isNaturalOrdered() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.useNaturalOrdering();
		assertTrue(tree.isNaturalOrdered());
	}
	
	@Test void test_isCustomOrdered() {
		NTree<String,Integer> tree = NTree.create("tree");
		BiFunction<NTreeNode<String,Integer>,NTreeNode<String,Integer>,Integer> compareBiFunction =
				(nodeA, nodeB) -> nodeA.getValue() - nodeB.getValue();
		tree.useCustomOrdering(compareBiFunction);
		assertTrue(tree.isCustomOrdered());
	}
	
	@Test void test_addNewRootSubtree_root_from_other_tree_thowsException() {
		NTree<String,Integer> treeA = NTree.create("A");
		NTree<String,Integer> treeB = NTree.create("B");
		Exception exception = assertThrows(RuntimeException.class, () -> {
			treeA.addNewRootSubtree(treeB.createNode("id"));
		});
		String expectedMessage = "Node passed to veve.datastructures.trees.NTree.addNewRootSubtree(NTreeNode) must must be a root";
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage));
	}
	
	@Test void test_addNewRootSubtree_node_with_parent_thowsException() {
		NTree<String,Integer> tree = NTree.create("A");
		NTreeNode<String,Integer> node = tree.createNode("node");
		node.parent = tree.createNode("parent");
		Exception exception = assertThrows(RuntimeException.class, () -> {
			tree.addNewRootSubtree(node);
		});
		String expectedMessage = "Node passed to veve.datastructures.trees.NTree.addNewRootSubtree(NTreeNode) must must be a root";
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage));
	}
	
	@Test void test_setRootSubtree_new_root() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.addIndex(IDS_INDEX, node -> node.getId());
		NTreeNode<String,Integer> root = tree.createNode("root");
		
		tree.setRootSubtree(root);
		
		assertNotSame(tree.getRoot(), root);
		assertEquals(root, tree.getRoot());
		assertEquals(root, tree.getFirstNodeInIndex(IDS_INDEX, "root"));
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_setRootSubtree_existing_root() {
		NTree<String,Integer> tA = NTree.create("treeA");
		tA.addNewRootSubtree(
			tA.n("X1").c(
				tA.n("Y1"),
				tA.n("Y2")));
		NTree<String,Integer> tB = TestUtil.testTree();
		tB.addIndex(IDS_INDEX, node -> node.getId());
		
		tB.setRootSubtree(tA.getRoot());
		
		Multiset<String> tBids =  HashMultiset.create(tB.mapToList(node -> node.getId()));
		Multiset<String> tBexpectedIds =  HashMultiset.create(Arrays.asList("X1","Y1","Y2"));
		Multiset<String> tBkeysInIndex =  HashMultiset.create(tB.indexes.get(IDS_INDEX).keysList());
		assertNotSame(tB.getRoot(), tA.getRoot());
		assertTrue(tB.getRoot().equalsSubtree(tA.getRoot()));
		assertNotSame(tB.getRoot(), tA.getRoot());
		assertEquals(tBexpectedIds, tBids);
		assertEquals(tBexpectedIds, tBkeysInIndex);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_setRootSubtree_existing_node_from_other_tree() {
		NTree<String,Integer> tA = NTree.create("treeA");
		tA.addNewRootSubtree(
			tA.n("X1").c(
				tA.n("Y1").c(
						tA.n("Z1")),
				tA.n("Y2")));
		NTree<String,Integer> tB = TestUtil.testTree();
		tB.addIndex(IDS_INDEX, node -> node.getId());
		
		NTreeNode<String,Integer> y1 = tA.getRoot().findFirst(node -> node.getId().equals("Y1"));
		tB.setRootSubtree(y1);
		
		Multiset<String> tBids =  HashMultiset.create(tB.mapToList(node -> node.getId()));
		Multiset<String> tBexpectedIds =  HashMultiset.create(Arrays.asList("Y1","Z1"));
		Multiset<String> tBkeysInIndex =  HashMultiset.create(tB.indexes.get(IDS_INDEX).keysList());
		assertNotSame(tB.root, y1);
		assertTrue(tB.root.equalsSubtree(y1));
		assertEquals(tBexpectedIds, tBids);
		assertEquals(tBexpectedIds, tBkeysInIndex);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_setRootSubtree_existing_node_from_same_tree() {
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		NTreeNode<String,Integer> b1 = tree.findFirst(node -> node.getId().equals("B1"));
		tree.setRootSubtree(b1);
		
		Multiset<String> treeExpectedIds =  HashMultiset.create(Arrays.asList("B1","C1","C2"));
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> treeKeysInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		assertNotSame(tree.root, b1);
		assertTrue(tree.root.equalsSubtree(b1));
		assertEquals(treeExpectedIds, treeIds);
		assertEquals(treeExpectedIds, treeKeysInIndex);
	}
	
	@Test void test_setRootSubtree_same_root() {
		NTree<String,Integer> tree = TestUtil.testTree();
		
		assertNull(tree.setRootSubtree(tree.root));
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_setRoot_new_root() {
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.addIndex("idsIndex", node -> node.getId());
		
		NTreeNode<String,Integer> root = tree.n("X1");
		tree.setRoot(root);
		
		Multiset<String> treeExpectedIds =  HashMultiset.create(Arrays.asList("X1","B1","B2","C1","C2"));
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> treeKeysInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		assertFalse(tree.root.equalsSubtree(root));
		assertEquals(tree.root, root);
		assertNotSame(tree.root, root);
		assertEquals(treeExpectedIds, treeIds);
		assertEquals(treeExpectedIds, treeKeysInIndex);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_setRoot_existing_root() {
		NTree<String,Integer> tA = TestUtil.testTree();
		tA.addIndex(IDS_INDEX, node -> node.getId());
		
		NTree<String,Integer> tB = NTree.create("treeB");
		tB.addNewRootSubtree(
			tB.n("X1").c(
				tB.n("Y1")));
		tA.addIndex("idsIndex", node -> node.getId());
		
		tA.setRoot(tB.getRoot());
		
		Multiset<String> treeExpectedIds =  HashMultiset.create(Arrays.asList("X1","B1","B2","C1","C2"));
		Multiset<String> treeIds =  HashMultiset.create(tA.mapToList(node -> node.getId()));
		Multiset<String> treeKeysInIndex =  HashMultiset.create(tA.indexes.get(IDS_INDEX).keysList());
		assertFalse(tA.root.equalsSubtree(tB.root));
		assertEquals(tA.root, tB.root);
		assertNotSame(tA.root, tB.root);
		assertEquals(treeExpectedIds, treeIds);
		assertEquals(treeExpectedIds, treeKeysInIndex);
	}

	@SuppressWarnings("unchecked")
	@Test void test_setRoot_existing_node_from_other_tree() {
		NTree<String,Integer> tA = TestUtil.testTree();
		tA.addIndex(IDS_INDEX, node -> node.getId());
		
		NTree<String,Integer> tB = TestUtil.testTree();
		
		NTreeNode<String,Integer> tBb1 = tB.findFirst(node -> node.getId().equals("B1"));
		tA.setRoot(tBb1);
		
		Multiset<String> tAExpectedIds =  HashMultiset.create(Arrays.asList("B1","B1","B2","C1","C2"));
		Multiset<String> tAIds =  HashMultiset.create(tA.mapToList(node -> node.getId()));
		Multiset<String> tAKeysInIndex =  HashMultiset.create(tA.indexes.get(IDS_INDEX).keysList());
		assertFalse(tA.root.equalsSubtree(tB.root));
		assertEquals(tA.root, tBb1);
		assertNotSame(tA.root, tBb1);
		assertEquals(tAExpectedIds, tAIds);
		assertEquals(tAExpectedIds, tAKeysInIndex);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_setRoot_existing_node_from_same_tree() {
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		NTreeNode<String,Integer> b2 = tree.findFirst(node -> node.getId().equals("B2"));
		tree.setRoot(b2);
		
		Multiset<String> treeExpectedIds =  HashMultiset.create(Arrays.asList("B1","B2","B2","C1","C2"));
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> treeKeysInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		assertFalse(tree.root.equalsSubtree(b2));
		assertEquals(tree.root, b2);
		assertNotSame(tree.root, b2);
		assertEquals(treeExpectedIds, treeIds);
		assertEquals(treeExpectedIds, treeKeysInIndex);
	}
	
	@Test void test_clearTree() {
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		tree.clearTree();
		
		assertNull(tree.root);
		assertEquals(0, tree.size());
		assertEquals(0, tree.indexes.get(IDS_INDEX).indexTable.size());
	}
	
	@Test void test_size_root_is_null() {
		NTree<String,Integer> tree = NTree.create("tree");
		assertEquals(0, tree.size());
	}
	
	@Test void test_size_one() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.addNewRootSubtree(tree.n("A"));
		assertEquals(1, tree.size());
	}
	
	@Test void test_size() {
		NTree<String,Integer> tree = TestUtil.testTree();
		assertEquals(5, tree.size());
	}
	
	@Test void test_height_root_is_null() {
		NTree<String,Integer> tree = NTree.create("tree");
		assertEquals(0, tree.height());
	}
	
	@Test void test_height_one() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.addNewRootSubtree(tree.n("A"));
		assertEquals(1, tree.height());
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_height() {
		NTree<String,Integer> tA = NTree.create("tree");
		tA.addNewRootSubtree(
			tA.n("A1").c(
				tA.n("B1").c(
					tA.n("C1"),
					tA.n("C2").c(
						tA.n("D1"))),
				tA.n("B2")));
		assertEquals(4, tA.height());
		
		NTree<String,Integer> tB = NTree.create("tree");
		tB.addNewRootSubtree(
			tB.n("A1").c(
				tB.n("B1"),
				tB.n("B2").c(
					tB.n("C1").c(
						tB.n("D1")),
					tB.n("C2"))));
		assertEquals(4, tB.height());
		
		NTree<String,Integer> tC = NTree.create("tree");
		tC.addNewRootSubtree(
			tC.n("A1").c(
				tC.n("B1").c(
					tC.n("C1"),
					tC.n("C2")),
				tC.n("B2").c(
					tC.n("D1"),
					tC.n("D2"))));
		assertEquals(3, tC.height());
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_addIndex() {
		NTree<String,Integer> tree = TestUtil.testTree();
		
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		Multiset<String> treeExpectedIds =  HashMultiset.create(Arrays.asList("A1","B1","B2","C1","C2"));
		Multiset<String> treeKeysInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		assertEquals(treeExpectedIds, treeKeysInIndex);
	}
	
	@Test void test_getNodesInIndex() {
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.addIndex("oddValuesIndex", node -> node.getValue() % 2 != 0);
		
		List<NTreeNode<String,Integer>> nodesInIndex = tree.getNodesInIndex("oddValuesIndex", true);
		
		Multiset<Integer> expectedValues =  HashMultiset.create(Arrays.asList(1,3,5));
		Multiset<Integer> actualValues =  HashMultiset.create(nodesInIndex.stream().map(node -> node.getValue()).collect(Collectors.toList()));
		assertEquals(expectedValues, actualValues);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_getNodesInIndex_no_mapping() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.addNewRootSubtree(
			tree.n("A1",2).c(
				tree.n("B1",4).c(
					tree.n("C1"))));
		tree.addIndex("oddValuesIndex", node -> node.getValue() % 2 != 0);
		
		List<NTreeNode<String,Integer>> nodesInIndex = tree.getNodesInIndex("oddValuesIndex", true);
		
		assertEquals(0, nodesInIndex.size());
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_getNodesInIndex_some_null_values() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.addNewRootSubtree(
			tree.n("A1",1).c(
				tree.n("B1",2).c(
					tree.n("C1").c(
						tree.n("D1",1).c(
							tree.n("E1"))))));
		tree.addIndex(VALUES_INDEX, node -> node.getValue());
		
		List<NTreeNode<String,Integer>> nodesWith1 = tree.getNodesInIndex(VALUES_INDEX, 1);
		List<NTreeNode<String,Integer>> nodesWith2 = tree.getNodesInIndex(VALUES_INDEX, 2);
		List<NTreeNode<String,Integer>> nodesWith3 = tree.getNodesInIndex(VALUES_INDEX, 3);
		
		Multiset<Integer> expectedIndexValues =  HashMultiset.create(Arrays.asList(1,1,2));
		Multiset<String> keysInIndex =  HashMultiset.create(tree.indexes.get(VALUES_INDEX).keysList());
		assertEquals(expectedIndexValues, keysInIndex);
		assertEquals(2, nodesWith1.size());
		assertEquals(1, nodesWith2.size());
		assertEquals(0, nodesWith3.size());
	}
	
	@Test void test_getNodesInIndex_non_existing_index() {
		NTree<String,Integer> tree = NTree.create("tree");
		
		List<NTreeNode<String,Integer>> nodesInIndex = tree.getNodesInIndex("nonExistingIndex", "key");
		assertEquals(null, nodesInIndex);
	}
	
	@Test void test_getNodesInIndex_non_existing_node_in_index() {
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		List<NTreeNode<String,Integer>> nodesInIndex = tree.getNodesInIndex(IDS_INDEX, "nonExistingKey");
		assertEquals(0, nodesInIndex.size());
	}
	
	@Test void test_getNodesInIndex_wrong_key_type() {
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		List<NTreeNode<String,Integer>> nodesInIndex = tree.getNodesInIndex(IDS_INDEX, new Object());
		assertEquals(0, nodesInIndex.size());
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_recreateIndexes() {
		NTree<String,Integer> tree = TestUtil.testTree();
		TreeNodeIndex<String, Integer, String> idsIndex = new TreeNodeIndex<>(IDS_INDEX, tree, node -> node.getId());
		TreeNodeIndex<String, Integer, Integer> valuesIndex = new TreeNodeIndex<>(VALUES_INDEX, tree, node -> node.getValue());
		tree.indexes.put(IDS_INDEX, idsIndex);
		tree.indexes.put(VALUES_INDEX, valuesIndex);
		
		tree.recreateIndexes();
		
		Multiset<String> treeExpectedIds =  HashMultiset.create(Arrays.asList("A1","B1","B2","C1","C2"));
		Multiset<Integer> treeExpectedValues =  HashMultiset.create(Arrays.asList(1,2,3,4,5));
		Multiset<String> treeKeysInIdsIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		Multiset<Integer> treeKeysInValuesIndex =  HashMultiset.create(tree.indexes.get(VALUES_INDEX).keysList());
		assertEquals(treeExpectedIds, treeKeysInIdsIndex);
		assertEquals(treeExpectedValues, treeKeysInValuesIndex);
	}
	
	@Test void test_getIndexNames() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.addIndex("ids", node -> node.getId());
		tree.addIndex("values", node -> node.getValue());
		tree.addIndex("numberOfSiblings", node -> node.getParent().getChildrenSize() - 1);
		tree.addIndex("numberOfChildren", node -> node.getChildrenSize());
		
		Multiset<String> expectedIndexNames =  HashMultiset.create(Arrays.asList("ids","values","numberOfSiblings","numberOfChildren"));
		Multiset<String> indexNames = HashMultiset.create(tree.getIndexNames());
		assertEquals(expectedIndexNames, indexNames);
	}
	
	@Test void test_getFirstNodeInIndex() {
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		NTreeNode<String,Integer> b1 = tree.getFirstNodeInIndex(IDS_INDEX, "B1");
		assertEquals(b1, tree.getRoot().getChildById("B1"));
	}
	
	@Test void test_getFirstNodeInIndex_non_existing_index() {
		NTree<String,Integer> tree = NTree.create("tree");
		
		NTreeNode<String,Integer> node = tree.getFirstNodeInIndex("nonExistingIndex", "key");
		assertEquals(null ,node);
	}
	
	@Test void test_getFirstNodeInIndex_non_existing_node_in_index() {
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		NTreeNode<String,Integer> node = tree.getFirstNodeInIndex(IDS_INDEX, "nonExistingKey");
		assertEquals(null, node);
	}
	
	@Test void test_getFirstNodeInIndex_wrong_key_type() {
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		NTreeNode<String,Integer> node = tree.getFirstNodeInIndex(IDS_INDEX, new Object());
		assertEquals(null, node);
	}
	
	@Test void test_toJsonAndFromJson() {
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		tree.nodeValueCloningUsesSerialization(new TypeToken<Integer>(){}.getType());
		
		String json = tree.toJson();
		Type nodeValueType = new TypeToken<Integer>() {}.getType();
		NTree<String,Integer> treeClone =  NTree.fromJson(json, String.class, nodeValueType);
		assertEquals(tree, treeClone);
	}
	
	@Test void test_clone() {
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		tree.nodeValueCloningUsesSerialization(new TypeToken<Integer>(){}.getType());
		tree.useCustomOrdering((nodeA,nodeB) -> nodeA.getId().compareTo(nodeB.getId()));
		
		NTree<String,Integer> clone = tree.clone();
		
		assertTrue(tree.isClone(clone, clone.toList()));
		assertNotSame(tree, clone);
		assertNotSame(tree.root, clone.root);
		assertNotSame(tree.indexes, clone.indexes);
		assertNotEquals(tree.uuid, clone.uuid);
	}
	
	@Test void test_equals() {
		NTree<String,Integer> tA = TestUtil.testTree();
		NTree<String,Integer> tB = TestUtil.testTree();
		NTree<String,Integer> tC = NTree.create(tA.id);
		tC.addNewRootSubtree(tC.n("x"));
		NTree<String,Integer> tD =TestUtil.testTree();
		tD.id = "tD";
		
		assertEquals(tA, tB);
		assertNotSame(tA, tB);
		assertNotEquals(tA, tC);
		assertNotEquals(tA, tD);
	}
	
}
