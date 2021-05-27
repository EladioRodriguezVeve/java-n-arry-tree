package veve.datastructures.trees;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class TreeNodeIndexTest {
	
	static final String IDS_INDEX = "idsIndex";
	
	@SuppressWarnings("unchecked")
	@Test void test_computeIndex() {
		NTree<String,Integer> tree = TestUtil.testTree();
		TreeNodeIndex<String, Integer, String> idsIndex = new TreeNodeIndex<>(IDS_INDEX, tree, node -> node.getId());
		tree.indexes.put(IDS_INDEX, idsIndex);
		
		idsIndex.computeIndex();
		
		Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A1","B1","B2","C1","C2"));
		Multiset<String> keysInIdsIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		assertEquals(expectedIds, keysInIdsIndex);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_put() {
		NTree<String,Integer> tree = NTree.create("tree");
		TreeNodeIndex<String, Integer, String> idsIndex = new TreeNodeIndex<>(IDS_INDEX, tree, node -> node.getId());
		tree.indexes.put(IDS_INDEX, idsIndex);
		
		idsIndex.put(tree.n("A"));
		idsIndex.put(tree.n("B"));
		idsIndex.put(tree.n("C"));
		
		Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A","B","C"));
		Multiset<String> keysInIdsIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		assertEquals(expectedIds,keysInIdsIndex);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_remove() {
		NTree<String,Integer> tree = TestUtil.testTree();
		TreeNodeIndex<String, Integer, String> idsIndex = new TreeNodeIndex<>(IDS_INDEX, tree, node -> node.getId());
		tree.indexes.put(IDS_INDEX, idsIndex);
		idsIndex.computeIndex();
		
		idsIndex.remove(tree.findFirstWithId("B1"));
		
		Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A1","B2","C1","C2"));
		Multiset<String> keysInIdsIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		assertEquals(expectedIds,keysInIdsIndex);
	}
	
	@Test void test_clear() {
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		TreeNodeIndex<String, Integer, String> idsIndex = new TreeNodeIndex<>(IDS_INDEX, tree, node -> node.getId());
		tree.indexes.put(IDS_INDEX, idsIndex);
		idsIndex.computeIndex();
		
		idsIndex.clear();
		
		assertEquals(0, idsIndex.indexTable.size());
	}
	
	@Test void test_getNodes() {
		NTree<String,Integer> tree = TestUtil.testTree();
		TreeNodeIndex<String, Integer, Boolean> oddValuesIndex = new TreeNodeIndex<>("oddValuesIndex", tree, node -> node.getValue() % 2 != 0);
		tree.indexes.put("oddValuesIndex", oddValuesIndex);
		oddValuesIndex.computeIndex();
		
		List<NTreeNode<String,Integer>> oddNodes = oddValuesIndex.getNodes(true);
		
		NTreeNode<String,Integer> a1 = tree.findFirstWithValue(1);
		NTreeNode<String,Integer> b2 = tree.findFirstWithValue(3);
		NTreeNode<String,Integer> c2 = tree.findFirstWithValue(5);
		Multiset<NTreeNode<String, Integer>> expectedNodes =  HashMultiset.create(Arrays.asList(a1,b2,c2));
		Multiset<NTreeNode<String, Integer>> oddNdesInIndex =  HashMultiset.create(oddNodes);
		assertEquals(expectedNodes, oddNdesInIndex);
	}
	
	@Test void test_keysList() {
		NTree<String,Integer> tree = TestUtil.testTree();
		TreeNodeIndex<String, Integer, String> idsIndex = new TreeNodeIndex<>(IDS_INDEX, tree, node -> node.getId());
		tree.indexes.put(IDS_INDEX, idsIndex);
		idsIndex.computeIndex();
		
		List<String> keys = idsIndex.keysList();
		
		Multiset<String> expectedKeys =  HashMultiset.create(Arrays.asList("A1","B1","B2","C1","C2"));
		Multiset<String> keysInIndex =  HashMultiset.create(keys);
		assertEquals(expectedKeys, keysInIndex);
	}
	
}
