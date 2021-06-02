package veve.datastructures.trees;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

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
import veve.datastructures.trees.NTreeConstants.TreeTraversalOrder;

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
	
	@Test void test_dontUseOrdering() {
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
	
	@Test void test_addNewRoot_root_from_other_tree_returns_false() {
		NTree<String,Integer> treeA = NTree.create("A");
		NTree<String,Integer> treeB = NTree.create("B");
		boolean wasAdded = treeA.addNewRoot(treeB.createNode("id"));
		assertFalse(wasAdded);
		assertNull(treeA.root);
	}
	
	@Test void test_addNewRoot_node_with_parent_returns_false() {
		NTree<String,Integer> tree = NTree.create("A");
		NTreeNode<String,Integer> node = tree.createNode("node");
		node.parent = tree.createNode("parent");
		boolean wasAdded = tree.addNewRoot(node);
		assertFalse(wasAdded);
		assertNull(tree.root);
	}
	
	@Test void test_addNewRoot_tree_has_rrot_already_returns_false() {
		NTree<String,Integer> tree = NTree.create("A");
		NTreeNode<String,Integer> root = tree.createNode("root");
		tree.addNewRoot(root);
		NTreeNode<String,Integer> node = tree.createNode("node");
		boolean wasAdded = tree.addNewRoot(node);
		assertFalse(wasAdded);
		assertSame(root, tree.root);
	}
	
	@Test void test_setRoot_new_root() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.addIndex(IDS_INDEX, node -> node.getId());
		NTreeNode<String,Integer> root = tree.createNode("root");
		
		tree.setRoot(root);
		
		assertNotSame(tree.getRoot(), root);
		assertEquals(root, tree.getRoot());
		assertEquals(root, tree.firstNodeInIndexWithKey(IDS_INDEX, "root"));
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_setRoot_existing_root() {
		NTree<String,Integer> tA = NTree.create("treeA");
		tA.addNewRoot(
			tA.n("X1").c(
				tA.n("Y1"),
				tA.n("Y2")));
		NTree<String,Integer> tB = TestUtil.testTree();
		tB.addIndex(IDS_INDEX, node -> node.getId());
		
		tB.setRoot(tA.getRoot());
		
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
	@Test void test_setRoot_existing_node_from_other_tree() {
		NTree<String,Integer> tA = NTree.create("treeA");
		tA.addNewRoot(
			tA.n("X1").c(
				tA.n("Y1").c(
						tA.n("Z1")),
				tA.n("Y2")));
		NTree<String,Integer> tB = TestUtil.testTree();
		tB.addIndex(IDS_INDEX, node -> node.getId());
		
		NTreeNode<String,Integer> y1 = tA.getRoot().findFirst(node -> node.getId().equals("Y1"));
		tB.setRoot(y1);
		
		Multiset<String> tBids =  HashMultiset.create(tB.mapToList(node -> node.getId()));
		Multiset<String> tBexpectedIds =  HashMultiset.create(Arrays.asList("Y1","Z1"));
		Multiset<String> tBkeysInIndex =  HashMultiset.create(tB.indexes.get(IDS_INDEX).keysList());
		assertNotSame(tB.root, y1);
		assertTrue(tB.root.equalsSubtree(y1));
		assertEquals(tBexpectedIds, tBids);
		assertEquals(tBexpectedIds, tBkeysInIndex);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_setRoot_existing_node_from_same_tree() {
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		NTreeNode<String,Integer> b1 = tree.findFirst(node -> node.getId().equals("B1"));
		tree.setRoot(b1);
		
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
		
		assertNull(tree.setRoot(tree.root));
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_setRootSingleNode_new_root() {
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.addIndex("idsIndex", node -> node.getId());
		
		NTreeNode<String,Integer> root = tree.n("X1");
		tree.setRootSingleNode(root);
		
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
	@Test void test_setRootSingleNode_existing_root() {
		NTree<String,Integer> tA = TestUtil.testTree();
		tA.addIndex(IDS_INDEX, node -> node.getId());
		
		NTree<String,Integer> tB = NTree.create("treeB");
		tB.addNewRoot(
			tB.n("X1").c(
				tB.n("Y1")));
		tA.addIndex("idsIndex", node -> node.getId());
		
		tA.setRootSingleNode(tB.getRoot());
		
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
	@Test void test_setRootSingleNode_existing_node_from_other_tree() {
		NTree<String,Integer> tA = TestUtil.testTree();
		tA.addIndex(IDS_INDEX, node -> node.getId());
		
		NTree<String,Integer> tB = TestUtil.testTree();
		
		NTreeNode<String,Integer> tBb1 = tB.findFirst(node -> node.getId().equals("B1"));
		tA.setRootSingleNode(tBb1);
		
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
	@Test void test_setRootSingleNode_existing_node_from_same_tree() {
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		NTreeNode<String,Integer> b2 = tree.findFirst(node -> node.getId().equals("B2"));
		tree.setRootSingleNode(b2);
		
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
		tree.addNewRoot(tree.n("A"));
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
		tree.addNewRoot(tree.n("A"));
		assertEquals(1, tree.height());
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_height() {
		NTree<String,Integer> tA = NTree.create("tree");
		tA.addNewRoot(
			tA.n("A1").c(
				tA.n("B1").c(
					tA.n("C1"),
					tA.n("C2").c(
						tA.n("D1"))),
				tA.n("B2")));
		assertEquals(4, tA.height());
		
		NTree<String,Integer> tB = NTree.create("tree");
		tB.addNewRoot(
			tB.n("A1").c(
				tB.n("B1"),
				tB.n("B2").c(
					tB.n("C1").c(
						tB.n("D1")),
					tB.n("C2"))));
		assertEquals(4, tB.height());
		
		NTree<String,Integer> tC = NTree.create("tree");
		tC.addNewRoot(
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
	
	@Test void test_nodesInIndexWithKey() {
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.addIndex("oddValuesIndex", node -> node.getValue() % 2 != 0);
		
		List<NTreeNode<String,Integer>> nodesInIndex = tree.nodesInIndexWithKey("oddValuesIndex", true);
		
		Multiset<Integer> expectedValues =  HashMultiset.create(Arrays.asList(1,3,5));
		Multiset<Integer> actualValues =  HashMultiset.create(nodesInIndex.stream().map(node -> node.getValue()).collect(Collectors.toList()));
		assertEquals(expectedValues, actualValues);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_nodesInIndexWithKey_no_mapping() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.addNewRoot(
			tree.n("A1",2).c(
				tree.n("B1",4).c(
					tree.n("C1"))));
		tree.addIndex("oddValuesIndex", node -> node.getValue() % 2 != 0);
		
		List<NTreeNode<String,Integer>> nodesInIndex = tree.nodesInIndexWithKey("oddValuesIndex", true);
		
		assertEquals(0, nodesInIndex.size());
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_nodesInIndexWithKey_some_null_values() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.addNewRoot(
			tree.n("A1",1).c(
				tree.n("B1",2).c(
					tree.n("C1").c(
						tree.n("D1",1).c(
							tree.n("E1"))))));
		tree.addIndex(VALUES_INDEX, node -> node.getValue());
		
		List<NTreeNode<String,Integer>> nodesWith1 = tree.nodesInIndexWithKey(VALUES_INDEX, 1);
		List<NTreeNode<String,Integer>> nodesWith2 = tree.nodesInIndexWithKey(VALUES_INDEX, 2);
		List<NTreeNode<String,Integer>> nodesWith3 = tree.nodesInIndexWithKey(VALUES_INDEX, 3);
		
		Multiset<Integer> expectedIndexValues =  HashMultiset.create(Arrays.asList(1,1,2));
		Multiset<String> keysInIndex =  HashMultiset.create(tree.indexes.get(VALUES_INDEX).keysList());
		assertEquals(expectedIndexValues, keysInIndex);
		assertEquals(2, nodesWith1.size());
		assertEquals(1, nodesWith2.size());
		assertEquals(0, nodesWith3.size());
	}
	
	@Test void test_nodesInIndexWithKey_non_existing_index() {
		NTree<String,Integer> tree = NTree.create("tree");
		
		List<NTreeNode<String,Integer>> nodesInIndex = tree.nodesInIndexWithKey("nonExistingIndex", "key");
		assertEquals(null, nodesInIndex);
	}
	
	@Test void test_nodesInIndexWithKey_non_existing_node_in_index() {
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		List<NTreeNode<String,Integer>> nodesInIndex = tree.nodesInIndexWithKey(IDS_INDEX, "nonExistingKey");
		assertEquals(0, nodesInIndex.size());
	}
	
	@Test void test_gnodesInIndexWithKey_wrong_key_type() {
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		List<NTreeNode<String,Integer>> nodesInIndex = tree.nodesInIndexWithKey(IDS_INDEX, new Object());
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
		tree.addIndex("numberOfSiblings", node -> node.getParent().childrenSize() - 1);
		tree.addIndex("numberOfChildren", node -> node.childrenSize());
		
		Multiset<String> expectedIndexNames =  HashMultiset.create(Arrays.asList("ids","values","numberOfSiblings","numberOfChildren"));
		Multiset<String> indexNames = HashMultiset.create(tree.getIndexNames());
		assertEquals(expectedIndexNames, indexNames);
	}
	
	@Test void test_getFirstNodeInIndex() {
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		NTreeNode<String,Integer> b1 = tree.firstNodeInIndexWithKey(IDS_INDEX, "B1");
		assertEquals(b1, tree.getRoot().childWithId("B1"));
	}
	
	@Test void test_firstNodeInIndexWithKey_non_existing_index() {
		NTree<String,Integer> tree = NTree.create("tree");
		
		NTreeNode<String,Integer> node = tree.firstNodeInIndexWithKey("nonExistingIndex", "key");
		assertEquals(null ,node);
	}
	
	@Test void test_firstNodeInIndexWithKey_non_existing_node_in_index() {
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		NTreeNode<String,Integer> node = tree.firstNodeInIndexWithKey(IDS_INDEX, "nonExistingKey");
		assertEquals(null, node);
	}
	
	@Test void firstNodeInIndexWithKey() {
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		NTreeNode<String,Integer> node = tree.firstNodeInIndexWithKey(IDS_INDEX, new Object());
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
	
	@Test void test_findAll_root_is_null() {
		NTree<String,Integer> tree = NTree.create("tree");
		
		List<NTreeNode<String,Integer>> nodes =  tree.findAll(node -> node.getParent() == null);
		
    	assertEquals(0, nodes.size());
	}
	
	@Test void test_findAll_no_matches_found() {
		NTree<String,Integer> tree = TestUtil.testTreeWithNullValues();
		
		List<NTreeNode<String,Integer>> nodes =  tree.findAll(node -> node.getValue() < 0);
		
    	assertEquals(0, nodes.size());
	}
	
	@Test void test_findAll() {
		NTree<String,Integer> tree = TestUtil.testTreeWithNullValues();
		
		List<NTreeNode<String,Integer>> oddValueNodes =  tree.findAll(node -> node.getValue() % 2 != 0);
		
		NTreeNode<String,Integer> b2 = tree.n("B2", 3);
		NTreeNode<String,Integer> c2 = tree.n("C2", 5);
		Multiset<NTreeNode<String,Integer>> expected =  HashMultiset.create(Arrays.asList(b2,c2));
    	Multiset<NTreeNode<String,Integer>> result =  HashMultiset.create(oddValueNodes);
    	assertEquals(expected, result);
	}
	
	@Test void test_findFirst_root_is_null() {
		NTree<String,Integer> tree = NTree.create("tree");
		
		NTreeNode<String,Integer> node =  tree.findFirst(n -> n.getId() != null);
		
    	assertNull(node);
	}
	
	@Test void test_findFirst_root_no_match() {
		NTree<String,Integer> tree = TestUtil.testTreeWithNullValues();
		
		NTreeNode<String,Integer> node =  tree.findFirst(n -> n.getId().equals("XX"));
		
		assertNull(node);
	}
	
	@Test void test_findFirst() {
		NTree<String,Integer> tree = TestUtil.testTreeWithNullValues();
		
		NTreeNode<String,Integer> node =  tree.findFirst(n -> n.getId().equals("C2"));
		
		NTreeNode<String,Integer> expected = tree.n("C2", 5);
    	assertEquals(expected, node);
	}
	
	@Test void test_toList_root_is_null() {
		NTree<String,Integer> tree = NTree.create("tree");
		
		List<NTreeNode<String,Integer>> nodes =  tree.toList();
		
    	assertEquals(0, nodes.size());
	}
	
	@Test void test_toList() {
		NTree<String,Integer> tree = TestUtil.testTree();
		
		List<NTreeNode<String,Integer>> nodes =  tree.toList();
		
		NTreeNode<String,Integer> a1 = tree.n("A1", 1);
		NTreeNode<String,Integer> b1 = tree.n("B1", 2);
		NTreeNode<String,Integer> b2 = tree.n("B2", 3);
		NTreeNode<String,Integer> c1 = tree.n("C1", 4);
		NTreeNode<String,Integer> c2 = tree.n("C2", 5);
		Multiset<NTreeNode<String,Integer>> expected =  HashMultiset.create(Arrays.asList(a1,b1,b2,c1,c2));
		Multiset<NTreeNode<String,Integer>> actual = HashMultiset.create(nodes);
    	assertEquals(expected, actual);
	}
	
	@Test void test_nodesInLevel_root_is_null() {
		NTree<String,Integer> tree = NTree.create("tree");
		
		List<NTreeNode<String,Integer>> nodes =  tree.nodesInLevel(1);
		
    	assertEquals(0, nodes.size());
	}
	
	@Test void test_nodesInLevel_level_less_than_1() {
		NTree<String,Integer> tree = TestUtil.testTree();
		
		Exception exception = assertThrows(RuntimeException.class, () -> {
			tree.nodesInLevel(0);
		});
		String expectedMessage = "level cannot be less than 1";
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage));
	}
	
	@Test void test_nodesInLevel_level_non_existing_level() {
		NTree<String,Integer> tree = TestUtil.testTree();
		
		List<NTreeNode<String,Integer>> nodes =  tree.nodesInLevel(10);
		
    	assertEquals(0, nodes.size());
	}
	
	@Test void test_nodesInLevel_level() {
		NTree<String,Integer> tree = TestUtil.testTree();
		
		List<NTreeNode<String,Integer>> nodes =  tree.nodesInLevel(3);
		
		NTreeNode<String,Integer> c1 = tree.n("C1", 4);
		NTreeNode<String,Integer> c2 = tree.n("C2", 5);
		Multiset<NTreeNode<String,Integer>> expected =  HashMultiset.create(Arrays.asList(c1,c2));
		Multiset<NTreeNode<String,Integer>> actual = HashMultiset.create(nodes);
    	assertEquals(expected, actual);
	}
	
	@Test void test_mapToList() {
		NTree<String,Integer> tree = TestUtil.testTreeWithNullValues();
		
		Multiset<String> ids = HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<Integer> values = HashMultiset.create(tree.mapToList(node -> node.getValue()));
		Multiset<Integer> valuesX2 = HashMultiset.create(tree.mapToList(node -> node.getValue()*2));
		
		Multiset<String> expIds = HashMultiset.create(Arrays.asList("A1","B1","B2","C1","C2"));
		Multiset<Integer> expValues = HashMultiset.create(Arrays.asList(null,null,2,3,5));
		Multiset<Integer> expValuesX2 = HashMultiset.create(Arrays.asList(null,null,4,6,10));
		assertEquals(expIds, ids);
		assertEquals(expValues, values);
		assertEquals(expValuesX2, valuesX2);
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
	
	@SuppressWarnings("unchecked")
	@Test void test_forEachNode_root_is_null() {
		NTree<String,Integer> tree = NTree.create("tree");
		
		tree.forEachNode(TreeTraversalOrder.PRE_ORDER, node -> node.c(tree.n("X")));
		
		assertNull(tree.root);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_forEachPreOrder_root_is_null() {
		NTree<String,Integer> tree = NTree.create("tree");
		
		tree.forEachPreOrder(node -> node.c(tree.n("X")));
		
		assertNull(tree.root);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_forEachPostOrder_root_is_null() {
		NTree<String,Integer> tree = NTree.create("tree");
		
		tree.forEachPostOrder(node -> node.c(tree.n("X")));
		
		assertNull(tree.root);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_forEachLevelOrder_root_is_null() {
		NTree<String,Integer> tree = NTree.create("tree");
		
		tree.forEachLevelOrder(node -> node.c(tree.n("X")));
		
		assertNull(tree.root);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_forEachLevelOrderFromBottom_root_is_null() {
		NTree<String,Integer> tree = NTree.create("tree");
		
		tree.forEachLevelOrderFromBottom(node -> node.c(tree.n("X")));
		
		assertNull(tree.root);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_forEachOfLevel_root_is_null() {
		NTree<String,Integer> tree = NTree.create("tree");
		
		tree.forEachOfLevel(1, node -> node.c(tree.n("X")));
		
		assertNull(tree.root);
	}
	
	@Test void test_stream_root_is_null() {
		NTree<String,Integer> tree = NTree.create("tree");
		
		List<NTreeNode<String,Integer>> nodes = tree.stream().collect(Collectors.toList());
		
		assertEquals(0, nodes.size());
	}
	
	@Test void test_stream_root() {
		NTree<String,Integer> tree = TestUtil.testTree();
		
		List<NTreeNode<String,Integer>> nodes = tree.stream().collect(Collectors.toList());
		
		assertEquals(5, nodes.size());
	}
	
	@Test void test_isClone_same_instance() {
		NTree<String,Integer> tree = TestUtil.testTree();
		NTree<String,Integer> clone = tree;
		assertFalse(tree.isClone(clone, null));
	}
	
	@Test void test_isClone_true_created_independently() {
		Type nodeValueType = new TypeToken<Integer>() {}.getType();
		
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.incrementVersion();
		tree.nodeValueCloningUsesSerialization(nodeValueType);
		tree.useCustomOrdering((nodeA,nodeB) -> nodeA.compareTo(nodeB));
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		NTree<String,Integer> clone = TestUtil.testTree();
		clone.incrementVersion();
		clone.nodeValueCloningUsesSerialization(nodeValueType);
		clone.useCustomOrdering((nodeA,nodeB) -> nodeA.compareTo(nodeB));
		clone.addIndex(IDS_INDEX, node -> node.getId());
		
		assertTrue(tree.isClone(clone, tree.toList()));
	}
	
	@Test void test_isClone_true_created_by_cloniing() {
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.incrementVersion();
		tree.nodeValueCloningUsesSerialization(new TypeToken<Integer>() {}.getType());
		tree.useCustomOrdering((nodeA,nodeB) -> nodeA.compareTo(nodeB));
		tree.addIndex(IDS_INDEX, node -> node.getId());
		NTree<String,Integer> clone = tree.clone();
		
		assertTrue(tree.isClone(clone, tree.toList()));
	}
	
	@Test void test_treeGraph_root_is_null() {
		NTree<String,Integer> tree = NTree.create("tree");
		
		assertEquals("Empty Tree", tree.treeGraph());
	}
	
	@Test void test_equals() {
		NTree<String,Integer> tA = TestUtil.testTree();
		NTree<String,Integer> tB = TestUtil.testTree();
		NTree<String,Integer> tC = NTree.create(tA.id);
		tC.addNewRoot(tC.n("x"));
		NTree<String,Integer> tD =TestUtil.testTree();
		tD.id = "tD";
		
		assertEquals(tA, tB);
		assertNotSame(tA, tB);
		assertNotEquals(tA, tC);
		assertNotEquals(tA, tD);
	}
	
	@Test void test_toString_root_is_null() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.useCustomOrdering((nodeA,nodeB) -> nodeA.compareTo(nodeB));
		
		String treString = tree.toString();
		treString = treString.replaceFirst("  \"uuid\": \"[0-9a-f-]{1,}\"", "  \"uuid\": x");
		
		String expected = "{\n"
				+ "  \"uuid\": x,\n"
				+ "  \"id\": \"tree\",\n"
				+ "  \"version\": 1,\n"
				+ "  \"root\": null,\n"
				+ "  \"nodeValueCloningMode\": null,\n"
				+ "  \"isOrdered\": true,\n"
				+ "  \"nodeComparator\": {\n"
				+ "    \"compareBiFunction\": {}\n"
				+ "  }\n"
				+ "}";
		assertEquals(expected, treString);
	}
	
	@Test void test_toString() {
		NTree<String,Integer> tree = TestUtil.testTreeWithNullValues();
		
		String treString = tree.toString();
		treString = treString.replaceAll("  \"uuid\": \"[0-9a-f-]{1,}\"", "  \"uuid\": x");
		
		String expected = "{\n"
				+ "  \"uuid\": x,\n"
				+ "  \"id\": \"t\",\n"
				+ "  \"version\": 1,\n"
				+ "  \"root\": {\n"
				+ "    \"id\": \"A1\",\n"
				+ "    \"value\": null,\n"
				+ "    \"version\": 1,\n"
				+ "    \"children\": {\n"
				+ "      \"B2\": {\n"
				+ "        \"id\": \"B2\",\n"
				+ "        \"value\": 3,\n"
				+ "        \"version\": 1,\n"
				+ "        \"children\": {},\n"
				+ "        \"uuid\": x\n"
				+ "      },\n"
				+ "      \"B1\": {\n"
				+ "        \"id\": \"B1\",\n"
				+ "        \"value\": 2,\n"
				+ "        \"version\": 1,\n"
				+ "        \"children\": {\n"
				+ "          \"C1\": {\n"
				+ "            \"id\": \"C1\",\n"
				+ "            \"value\": null,\n"
				+ "            \"version\": 1,\n"
				+ "            \"children\": {},\n"
				+ "            \"uuid\": x\n"
				+ "          },\n"
				+ "          \"C2\": {\n"
				+ "            \"id\": \"C2\",\n"
				+ "            \"value\": 5,\n"
				+ "            \"version\": 1,\n"
				+ "            \"children\": {},\n"
				+ "            \"uuid\": x\n"
				+ "          }\n"
				+ "        },\n"
				+ "        \"uuid\": x\n"
				+ "      }\n"
				+ "    },\n"
				+ "    \"uuid\": x\n"
				+ "  },\n"
				+ "  \"nodeValueCloningMode\": null,\n"
				+ "  \"isOrdered\": false,\n"
				+ "  \"nodeComparator\": null\n"
				+ "}";
		assertEquals(expected, treString);
	}
	
}
