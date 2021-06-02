package veve.datastructures.trees;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class NTreeNodeTest_ChildSettersGettersRemoval {
	
	static final String IDS_INDEX = "idsIndex";
	
	//==============================================================================================
	//	addNewChildren
	//==============================================================================================
	
	@SuppressWarnings("unchecked")
	@Test void test_addNewChildren_node_not_part_of_tree() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.addIndex(IDS_INDEX, node -> node.getId());
		NTreeNode<String,Integer> nodeA = tree.n("A");
		NTreeNode<String,Integer> nodeB = tree.n("B");
		nodeB.parent = tree.n("someParent");
		NTreeNode<String,Integer> nodeC = tree.n("C");
		NTreeNode<String,Integer> nodeC2 = tree.n("C");
		NTreeNode<String,Integer> nodeD = tree.n("D");
		
		nodeA.addNewChildren(nodeB, nodeC, nodeC2, nodeD);
		
		Multiset<NTreeNode<String,Integer>> nodeAexpectedChildren =  HashMultiset.create(Arrays.asList(nodeC, nodeD));
		Multiset<NTreeNode<String,Integer>> nodeAchildren =  HashMultiset.create(nodeA.childrenList());
		assertEquals(nodeAexpectedChildren, nodeAchildren);
		assertEquals(0, tree.indexes.get(IDS_INDEX).indexTable.values().size());
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_addNewChildren_node_is_part_of_tree() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.addIndex(IDS_INDEX, node -> node.getId());
		tree.addNewRoot(tree.n("A"));
		NTreeNode<String,Integer> nodeB = tree.n("B");
		nodeB.parent = tree.n("someParent");
		NTreeNode<String,Integer> nodeC = tree.n("C");
		NTreeNode<String,Integer> nodeC2 = tree.n("C");
		NTreeNode<String,Integer> nodeD = tree.n("D");
		
		tree.root.addNewChildren(nodeB, nodeC, nodeC2, nodeD);
		
		Multiset<NTreeNode<String,Integer>> nodeAexpectedChildren =  HashMultiset.create(Arrays.asList(nodeC, nodeD));
		Multiset<NTreeNode<String,Integer>> nodeAchildren =  HashMultiset.create(tree.root.childrenList());
		Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A","C","D"));
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		assertEquals(nodeAexpectedChildren, nodeAchildren);
		assertEquals(expectedIds, treeIds);
		assertEquals(expectedIds, idsInIndex);
		assertEquals(nodeC.uuid, tree.firstNodeInIndexWithKey(IDS_INDEX, "C").uuid);
	}
	
	//==============================================================================================
	//	setChild
	//==============================================================================================
	
	@SuppressWarnings("unchecked")
	@Test void test_setChild_is_own_child() {
		NTree<String,Integer> tree = NTree.create("tree");
		NTreeNode<String,Integer> a = tree.n("A");
		NTreeNode<String,Integer> b = tree.n("B");
		a.addNewChildren(b);
		
		assertThrows(RuntimeException.class, () -> {
			a.setChild(b);
		});
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_setChild_not_part_of_tree() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.addIndex(IDS_INDEX, node -> node.getId());
		NTreeNode<String,Integer> a = tree.n("A");
		NTreeNode<String,Integer> b = tree.n("B");
		a.addNewChildren(b);
		NTreeNode<String,Integer> b2 = tree.n("B",1);
		
		a.setChild(b2);
		
		assertEquals(1, a.childWithId("B").value);
		assertEquals(0, tree.indexes.get(IDS_INDEX).indexTable.values().size());
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_setChild_replaces() {
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		NTreeNode<String,Integer> newB1 = 
		tree.n("B1",8).c(
			tree.n("X1"),
			tree.n("X2"));
		
		tree.root.setChild(newB1);
		
		Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A1","B1", "B2", "X1", "X2"));
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		assertEquals(expectedIds, treeIds);
		assertEquals(expectedIds, idsInIndex);
		assertEquals(8, tree.root.childWithId("B1").value);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_setChild_adds() {
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		NTreeNode<String,Integer> w1 = 
		tree.n("W1").c(
			tree.n("X1"),
			tree.n("X2"));
		
		tree.root.setChild(w1);
		
		Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A1","B1", "B2", "C1", "C2", "W1", "X1", "X2"));
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		Multiset<String> expectedRootChildrenIds =  HashMultiset.create(Arrays.asList("B1", "B2", "W1"));
		Multiset<String> rootChildrenIds =  HashMultiset.create(tree.root.childrenIds());
		assertEquals(expectedIds, treeIds);
		assertEquals(expectedIds, idsInIndex);
		assertEquals(expectedRootChildrenIds, rootChildrenIds);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_setChild_adds_from_same_tree() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.addIndex(IDS_INDEX, node -> node.getId());
		tree.addNewRoot(
			tree.n("A1").c(
				tree.n("B1").c(
					tree.n("C1").c(
						tree.n("D1"),
						tree.n("D2"))),
				tree.n("B2")));
		
		NTreeNode<String,Integer> b1 = tree.findFirstWithId("B1");
		NTreeNode<String,Integer> b2 = tree.findFirstWithId("B2");
		NTreeNode<String,Integer> c1 = tree.findFirstWithId("C1");
		b2.setChild(c1);
		
		Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A1","B1", "B2", "C1", "C1", "D1", "D1", "D2","D2"));
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		assertTrue(b1.childWithId("C1").equalsSubtree(b2.childWithId("C1")));
		assertNotSame(b1.childWithId("C1"), b2.childWithId("C1"));
		assertEquals(expectedIds, treeIds);
		assertEquals(expectedIds, idsInIndex);
	}
	
	//==============================================================================================
	//	setChilds
	//==============================================================================================
	
	@SuppressWarnings("unchecked")
	@Test void test_setChildren() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.addIndex(IDS_INDEX, node -> node.getId());
		tree.addNewRoot(tree.n("A1"));
		
		NTreeNode<String,Integer> b1 = 
		tree.n("B1").c(
			tree.n("C1"),
			tree.n("C2"));
		
		NTreeNode<String,Integer> b2 = 
		tree.n("B2").c(
			tree.n("D1"),
			tree.n("D2"));
		
		NTreeNode<String,Integer> b2Dup = 
			tree.n("B2").c(
				tree.n("E1"),
				tree.n("E2"));
		
		tree.root.setChildren(b1, b2, b2Dup);
		
		Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A1","B1", "B2", "C1", "C2", "D1", "D2"));
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		Multiset<String> expecteRootChilddIds =  HashMultiset.create(Arrays.asList("B1","B2"));
		Multiset<String> rootChilddIds =  HashMultiset.create(tree.root.childrenIds());
		
		assertTrue(tree.root.childWithId(b1.id).equalsSubtree(b1));
		assertTrue(tree.root.childWithId(b2.id).equalsSubtree(b2));
		assertEquals(expectedIds, treeIds);
		assertEquals(expectedIds, idsInIndex);
		assertEquals(expecteRootChilddIds, rootChilddIds);
	}
	
	//==============================================================================================
	//	setChildIfAbsent
	//==============================================================================================
	
	@SuppressWarnings("unchecked")
	@Test void test_setChildIfAbsent_is_own_child() {
		NTree<String,Integer> tree = NTree.create("tree");
		NTreeNode<String,Integer> a = tree.n("A");
		NTreeNode<String,Integer> b = tree.n("B");
		a.addNewChildren(b);
		
		boolean wasReplaced = a.setChildIfAbsent(b);
		
		assertFalse(wasReplaced);
	}
	
	@Test void test_setChildIfAbsent_not_part_of_tree() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.addIndex(IDS_INDEX, node -> node.getId());
		NTreeNode<String,Integer> a = tree.n("A");
		NTreeNode<String,Integer> b = tree.n("B");
		
		a.setChildIfAbsent(b);
		
		assertEquals(b, a.childWithId("B"));
		assertNotSame(b, a.childWithId("B"));
		assertEquals(0, tree.indexes.get(IDS_INDEX).indexTable.values().size());
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_setChildIfAbsent() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.addIndex(IDS_INDEX, node -> node.getId());
		tree.addNewRoot(tree.n("A"));
		NTreeNode<String,Integer> b = tree.n("B").c(tree.n("C"));
		
		tree.root.setChildIfAbsent(b);
		
		Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A","B","C"));
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		assertEquals(b, tree.root.childWithId("B"));
		assertNotSame(b, tree.root.childWithId("B"));
		assertEquals(expectedIds, treeIds);
		assertEquals(expectedIds, idsInIndex);
	}
	
	//==============================================================================================
	//	setChildsIfAbsent
	//==============================================================================================
	
	@SuppressWarnings("unchecked")
	@Test void test_setChildrenIfAbsent() {
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		NTreeNode<String,Integer> b1 = 
		tree.n("B1").c(
			tree.n("D1"),
			tree.n("D2"));
		
		NTreeNode<String,Integer> x1 = 
		tree.n("X1").c(
			tree.n("Y1"),
			tree.n("Y2"));
		
		tree.root.setChildrenIfAbsent(b1, x1);
		
		Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A1","B1","B2","C1","C2","X1","Y1","Y2"));
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		Multiset<String> expecteRootChilddIds =  HashMultiset.create(Arrays.asList("B1","B2","X1"));
		Multiset<String> rootChilddIds =  HashMultiset.create(tree.root.childrenIds());
		
		assertFalse(tree.root.childWithId(b1.id).equalsSubtree(b1));
		assertTrue(tree.root.childWithId(x1.id).equalsSubtree(x1));
		assertEquals(expectedIds, treeIds);
		assertEquals(expectedIds, idsInIndex);
		assertEquals(expecteRootChilddIds, rootChilddIds);
	}
	
	//==============================================================================================
	//	GETTERS
	//==============================================================================================
	
	@Test void test_firstChildWithValue_no_match_found() {
		NTree<String,Integer> tree = NTree.create("tree");
		
		NTreeNode<String,Integer> node = tree.findFirstWithValue(99);
		
		assertNull(node);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_firstChildWithValue() {
		NTree<String,Integer> t = NTree.create("tree");
		t.addNewRoot(
			t.n("A1").c(
				t.n("B1",1),
				t.n("B2",1),
				t.n("B3"),
				t.n("B4",1),
				t.n("B5",2)
			)
		);
		t.useCustomOrdering((nodeA,nodeB) -> nodeB.compareTo(nodeA));
		
		NTreeNode<String,Integer> node = t.root.firstChildWithValue(1);
		
		NTreeNode<String,Integer> expected = t.n("B4",1);
		assertEquals(expected, node);
	}
	
	@Test void test_childrenList_using_ids() {
		NTree<String,Integer> tree = TestUtil.testTree();
		
		List<NTreeNode<String,Integer>> nodes =  tree.root.childrenList("B1","B2");
		
		Collections.sort(nodes);
		assertEquals(2, nodes.size());
		assertEquals("B1", nodes.get(0).id);
		assertEquals("B2", nodes.get(1).id);
	}
	
	@Test void test_childrenMap_using_ids() {
		NTree<String,Integer> tree = TestUtil.testTree();
		
		Map<String, NTreeNode<String, Integer>> nodes =  tree.root.childrenMap(Arrays.asList("B1","B2"));
		
		assertEquals(2, nodes.size());
		assertEquals("B1", nodes.get("B1").id);
		assertEquals("B2", nodes.get("B2").id);
	}
	
	//==============================================================================================
	//	removeChild
	//==============================================================================================
	
	@Test void test_removeChild_contains_child_with_same_id() {
		NTree<String,Integer> tree = NTree.create("tree");
		NTreeNode<String,Integer> a = tree.n("A");
		
		NTreeNode<String,Integer> removed = a.removeChild("nonExistingId");
		
		assertNull(removed);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_removeChild_is_not_part_of_tree() {
		NTree<String,Integer> tree = NTree.create("tree");
		NTreeNode<String,Integer> a = tree.n("A");
		NTreeNode<String,Integer> b = tree.n("B");
		a.addNewChildren(b);
		
		NTreeNode<String,Integer> removed = a.removeChild(b.id);
		
		assertEquals(b, removed);
		assertEquals(0, a.childrenSize());
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_removeChild() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.addIndex(IDS_INDEX, node -> node.getId());
		tree.addNewRoot(
			tree.n("A1").c(
				tree.n("B1"),
				tree.n("B2")));
		
		NTreeNode<String,Integer> b1 = tree.findFirstWithId("B1");
		NTreeNode<String,Integer> removed = tree.root.removeChild(b1.id);
		
		Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A1","B2"));
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		assertEquals(b1, removed);
		assertEquals(Arrays.asList("B2"), tree.root.childrenIds());
		assertEquals(expectedIds, treeIds);
		assertEquals(expectedIds, idsInIndex);
	}
	
	//==============================================================================================
	//	OTHER CHILD REMOVAL
	//==============================================================================================
	
	@SuppressWarnings("unchecked")
	@Test void test_removeChildren() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.addIndex(IDS_INDEX, node -> node.getId());
		tree.addNewRoot(
			tree.n("A1").c(
				tree.n("B1"),
				tree.n("B2"),
				tree.n("B3").c(
					tree.n("C1"))));
		
		NTreeNode<String,Integer> b1 = tree.findFirstWithId("B1");
		NTreeNode<String,Integer> b3 = tree.findFirstWithId("B3");
		Map<String,NTreeNode<String,Integer>> removed = tree.root.removeChildren(b1.id, b3.id);
		
		Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A1","B2"));
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		Map<String,NTreeNode<String,Integer>> expectedRemoved = new HashMap<>();
		expectedRemoved.put(b1.id, b1);
		expectedRemoved.put(b3.id, b3);
		assertEquals(expectedRemoved, removed);
		assertEquals(expectedIds, treeIds);
		assertEquals(expectedIds, idsInIndex);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_removeAllChildren() {
		NTree<String,Integer> tree = TestUtil.testTree();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		NTreeNode<String,Integer> b1 = tree.findFirstWithId("B1");
		NTreeNode<String,Integer> b2 = tree.findFirstWithId("B2");
		Map<String,NTreeNode<String,Integer>> removed = tree.root.removeAllChildren();
		
		Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A1"));
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		Map<String,NTreeNode<String,Integer>> expectedRemoved = new HashMap<>();
		expectedRemoved.put(b1.id, b1);
		expectedRemoved.put(b2.id, b2);
		assertEquals(expectedRemoved, removed);
		assertEquals(expectedIds, treeIds);
		assertEquals(expectedIds, idsInIndex);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_retainChildren() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.addIndex(IDS_INDEX, node -> node.getId());
		tree.addNewRoot(
			tree.n("A1").c(
				tree.n("B1"),
				tree.n("B2"),
				tree.n("B3"),
				tree.n("B4").c(
					tree.n("C1"))));
		
		NTreeNode<String,Integer> b1 = tree.findFirstWithId("B1");
		NTreeNode<String,Integer> b2 = tree.findFirstWithId("B2");
		NTreeNode<String,Integer> b3 = tree.findFirstWithId("B3");
		NTreeNode<String,Integer> b4 = tree.findFirstWithId("B4");
		Map<String,NTreeNode<String,Integer>> removed = tree.root.retainChildren(b1.id, b3.id);
		
		Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A1","B1","B3"));
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		Map<String,NTreeNode<String,Integer>> expectedRemoved = new HashMap<>();
		expectedRemoved.put(b2.id, b2);
		expectedRemoved.put(b4.id, b4);
		assertEquals(expectedRemoved, removed);
		assertEquals(expectedIds, treeIds);
		assertEquals(expectedIds, idsInIndex);
	}
	
}
