package veve.datastructures.trees;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class NTreeNodeTest_Traversal {
	
	static final String IDS_INDEX = "idsIndex";
	static final String VALUES_INDEX = "valuesIndex";
	
	@SuppressWarnings("unchecked")
	NTree<String,Integer> testTree() {
		NTree<String,Integer> t = NTree.create("tree");
		t.addNewRootSubtree(
			t.n("A1").c(
				t.n("B1",3).c(
					t.n("C1",2),
					t.n("C2",1)
				),
				t.n("B2",2),
				t.n("B3",1)
			)
		);
		return t;
	}
	
	@SuppressWarnings("unchecked")
	NTree<String,Integer> testTree2() {
		NTree<String,Integer> t = NTree.create("tree");
		t.addNewRootSubtree(
			t.n("A1").c(
				t.n("B1",2).c(
					t.n("C1",2),
					t.n("C2",1)
				),
				t.n("B2",1).c(
					t.n("D1",2),
					t.n("D2",1))
			)
		);
		return t;
	}
	
	//==============================================================================================
	//	forEachPreOrder
	//==============================================================================================
	
	@SuppressWarnings("unchecked")
	@Test void test_forEachPreOrder_natural_ordering() {
		NTree<String,Integer> tree = testTree();
		tree.useNaturalOrdering();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		List<String> idsList = new LinkedList<>();
		tree.root.forEachPreOrder(node -> node.replaceId("n" + node.getId()));
		tree.root.forEachPreOrder(node -> idsList.add(node.getId()));
		
		List<String> expectedIdsList = Arrays.asList("nA1","nB1","nC1","nC2","nB2","nB3");
		Multiset<String> expectedIdsListWrapped =  HashMultiset.create(expectedIdsList);
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		
		assertEquals(expectedIdsList, idsList);
		assertEquals(expectedIdsListWrapped, treeIds);
		assertEquals(expectedIdsListWrapped, idsInIndex);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_forEachPreOrder_custom_ordering() {
		NTree<String,Integer> tree = testTree();
		tree.useCustomOrdering((a,b) -> a.getValue() - b.getValue());;
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		List<String> idsList = new LinkedList<>();
		tree.root.forEachPreOrder(node -> node.replaceId("n" + node.getId()));
		tree.root.forEachPreOrder(node -> idsList.add(node.getId()));
		
		List<String> expectedIdsList = Arrays.asList("nA1","nB3","nB2","nB1","nC2","nC1");
		Multiset<String> expectedIdsListWrapped =  HashMultiset.create(expectedIdsList);
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		
		assertEquals(expectedIdsList, idsList);
		assertEquals(expectedIdsListWrapped, treeIds);
		assertEquals(expectedIdsListWrapped, idsInIndex);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_forEachPreOrder_natural_ordering_tree_manipulation() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.addNewRootSubtree(
			tree.n("A1").c(
				tree.n("B1"),
				tree.n("B2")));
		
		tree.useNaturalOrdering();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		tree.root.forEachPreOrder(node -> node.setChildSubtreesIfAbsent(tree.n("X")));
		
		NTree<String,Integer> t = NTree.create("tree");
		t.addNewRootSubtree(
			t.n("A1").c(
				t.n("B1").c(
					t.n("X")),
				t.n("B2").c(
					t.n("X")),
				t.n("X")));
		
		Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A1","B1","B2","X","X","X"));
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		
		assertTrue(tree.equals(t));
		assertEquals(expectedIds, treeIds);
		assertEquals(expectedIds, idsInIndex);
	}
	
	//==============================================================================================
	//	forEachPostOrder
	//==============================================================================================
	
	@SuppressWarnings("unchecked")
	@Test void test_forEachPostOrder_natural_ordering() {
		NTree<String,Integer> tree = testTree();
		tree.useNaturalOrdering();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		List<String> idsList = new LinkedList<>();
		tree.root.forEachPostOrder(node -> node.replaceId("n" + node.getId()));
		tree.root.forEachPostOrder(node -> idsList.add(node.getId()));
		
		List<String> expectedIdsList = Arrays.asList("nC1","nC2","nB1","nB2","nB3","nA1");
		Multiset<String> expectedIdsListWrapped =  HashMultiset.create(expectedIdsList);
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		
		assertEquals(expectedIdsList, idsList);
		assertEquals(expectedIdsListWrapped, treeIds);
		assertEquals(expectedIdsListWrapped, idsInIndex);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_forEachPostOrder_custom_ordering() {
		NTree<String,Integer> tree = testTree();
		tree.useCustomOrdering((a,b) -> a.getValue() - b.getValue());;
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		List<String> idsList = new LinkedList<>();
		tree.root.forEachPostOrder(node -> node.replaceId("n" + node.getId()));
		tree.root.forEachPostOrder(node -> idsList.add(node.getId()));
		
		List<String> expectedIdsList = Arrays.asList("nB3","nB2","nC2","nC1","nB1","nA1");
		Multiset<String> expectedIdsListWrapped =  HashMultiset.create(expectedIdsList);
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		
		assertEquals(expectedIdsList, idsList);
		assertEquals(expectedIdsListWrapped, treeIds);
		assertEquals(expectedIdsListWrapped, idsInIndex);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_forEachPostOrder_natural_ordering_tree_manipulation() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.addNewRootSubtree(
			tree.n("A1").c(
				tree.n("B1"),
				tree.n("B2")));
		
		tree.useNaturalOrdering();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		tree.root.forEachPostOrder(node -> node.setChildSubtreesIfAbsent(tree.n("X")));
		
		NTree<String,Integer> t = NTree.create("tree");
		t.addNewRootSubtree(
			t.n("A1").c(
				t.n("B1").c(
					t.n("X")),
				t.n("B2").c(
					t.n("X")),
				t.n("X")));
		
		Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A1","B1","B2","X","X","X"));
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		
		assertTrue(tree.equals(t));
		assertEquals(expectedIds, treeIds);
		assertEquals(expectedIds, idsInIndex);
	}
	
	//==============================================================================================
	//	forEachOfLevel
	//==============================================================================================
	
	@Test void test_forEachOfLevel_invalid_low_level() {
		NTree<String,Integer> tree = testTree();
		
		Exception exception = assertThrows(RuntimeException.class, () -> {
			tree.forEachOfLevel(0, node -> node.removeSubtree());
		});
		
		assertTrue(exception.getMessage().contains("level cannot be less than 1"));
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_forEachOfLevel_1() {
		NTree<String,Integer> tree = testTree();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		List<String> idsList = new LinkedList<>();
		tree.root.forEachOfLevel(1, node -> node.replaceId("n" + node.getId()));
		tree.root.forEachOfLevel(1, node -> idsList.add(node.getId()));
		
		List<String> expectedIdsList = Arrays.asList("nA1");
		Multiset<String> expectedTreeIds =  HashMultiset.create(Arrays.asList("nA1","B1","B2","B3","C1","C2"));
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		
		assertEquals(expectedIdsList, idsList);
		assertEquals(expectedTreeIds, treeIds);
		assertEquals(expectedTreeIds, idsInIndex);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_forEachOfLevel_2_natural_ordering() {
		NTree<String,Integer> tree = testTree2();
		tree.useNaturalOrdering();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		List<String> idsList = new LinkedList<>();
		tree.root.forEachOfLevel(2, node -> node.replaceId("n" + node.getId()));
		tree.root.forEachOfLevel(2, node -> idsList.add(node.getId()));
		
		List<String> expectedIdsList = Arrays.asList("nB1","nB2");
		Multiset<String> expectedTreeIds =  HashMultiset.create(Arrays.asList("A1","nB1","nB2","C1","C2","D1","D2"));
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		
		assertEquals(expectedIdsList, idsList);
		assertEquals(expectedTreeIds, treeIds);
		assertEquals(expectedTreeIds, idsInIndex);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_forEachOfLevel_3_natural_ordering() {
		NTree<String,Integer> tree = testTree2();
		tree.useNaturalOrdering();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		List<String> idsList = new LinkedList<>();
		tree.root.forEachOfLevel(3, node -> node.replaceId("n" + node.getId()));
		tree.root.forEachOfLevel(3, node -> idsList.add(node.getId()));
		
		List<String> expectedIdsList = Arrays.asList("nC1","nC2","nD1","nD2");
		Multiset<String> expectedTreeIds =  HashMultiset.create(Arrays.asList("A1","B1","B2","nC1","nC2","nD1","nD2"));
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		
		assertEquals(expectedIdsList, idsList);
		assertEquals(expectedTreeIds, treeIds);
		assertEquals(expectedTreeIds, idsInIndex);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_forEachOfLevel_non_existing_upper_level_natural_ordering() {
		NTree<String,Integer> tree = testTree2();
		tree.useNaturalOrdering();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		List<String> idsList = new LinkedList<>();
		tree.root.forEachOfLevel(4, node -> node.replaceId("n" + node.getId()));
		tree.root.forEachOfLevel(4, node -> idsList.add(node.getId()));
		
		Multiset<String> expectedTreeIds =  HashMultiset.create(Arrays.asList("A1","B1","B2","C1","C2","D1","D2"));
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		
		assertEquals(0, idsList.size());
		assertEquals(expectedTreeIds, treeIds);
		assertEquals(expectedTreeIds, idsInIndex);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_forEachOfLevel_3_natural_ordering_tree_manipulation() {
		NTree<String,Integer> tree = testTree2();
		tree.useNaturalOrdering();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		tree.root.forEachOfLevel(3, node -> node.setChildSubtreesIfAbsent(tree.n("X")));
		
		NTree<String,Integer> t = NTree.create("tree");
		t.addNewRootSubtree(
			t.n("A1").c(
				t.n("B1",2).c(
					t.n("C1",2).c(
						t.n("X")),
					t.n("C2",1).c(
						t.n("X"))),
				t.n("B2",1).c(
					t.n("D1",2).c(
						t.n("X")),
					t.n("D2",1).c(
						t.n("X")))));
		
		Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A1","B1","B2","C1","C2","D1","D2","X","X","X","X"));
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		
		assertEquals(tree, t);
		assertEquals(expectedIds, treeIds);
		assertEquals(expectedIds, idsInIndex);
	}
	
	//==============================================================================================
	//	forEachLevelOrder
	//==============================================================================================
	
	@SuppressWarnings("unchecked")
	@Test void test_forEachLevelOrder_natural_ordering() {
		NTree<String,Integer> tree = testTree();
		tree.useNaturalOrdering();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		List<String> idsList = new LinkedList<>();
		tree.root.forEachLevelOrder(node -> node.replaceId("n" + node.getId()));
		tree.root.forEachLevelOrder(node -> idsList.add(node.getId()));
		
		List<String> expectedIdsList = Arrays.asList("nA1","nB1","nB2","nB3","nC1","nC2");
		Multiset<String> expectedIdsListWrapped =  HashMultiset.create(expectedIdsList);
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		
		assertEquals(expectedIdsList, idsList);
		assertEquals(expectedIdsListWrapped, treeIds);
		assertEquals(expectedIdsListWrapped, idsInIndex);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_forEachLevelOrder_custom_ordering() {
		NTree<String,Integer> tree = testTree();
		tree.useCustomOrdering((a,b) -> a.getValue() - b.getValue());;
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		List<String> idsList = new LinkedList<>();
		tree.root.forEachLevelOrder(node -> node.replaceId("n" + node.getId()));
		tree.root.forEachLevelOrder(node -> idsList.add(node.getId()));
		
		List<String> expectedIdsList = Arrays.asList("nA1","nB3","nB2","nB1","nC2","nC1");
		Multiset<String> expectedIdsListWrapped =  HashMultiset.create(expectedIdsList);
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		
		assertEquals(expectedIdsList, idsList);
		assertEquals(expectedIdsListWrapped, treeIds);
		assertEquals(expectedIdsListWrapped, idsInIndex);
	}
	 
	@SuppressWarnings("unchecked")
	@Test void test_forEachLevelOrder_natural_ordering_tree_manipulation() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.addNewRootSubtree(
			tree.n("A1").c(
				tree.n("B1"),
				tree.n("B2")));
		
		tree.useNaturalOrdering();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		tree.root.forEachLevelOrder(node -> node.setChildSubtreesIfAbsent(tree.n("X")));
		
		NTree<String,Integer> t = NTree.create("tree");
		t.addNewRootSubtree(
			t.n("A1").c(
				t.n("B1").c(
					t.n("X")),
				t.n("B2").c(
					t.n("X")),
				t.n("X")));
		
		Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A1","B1","B2","X","X","X"));
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		
		assertTrue(tree.equals(t));
		assertEquals(expectedIds, treeIds);
		assertEquals(expectedIds, idsInIndex);
	}
	
	//==============================================================================================
	//	forEachLevelOrderFromBottom
	//==============================================================================================
	
	@SuppressWarnings("unchecked")
	@Test void test_forEachLevelOrderFromBottom_natural_ordering() {
		NTree<String,Integer> tree = testTree();
		tree.useNaturalOrdering();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		List<String> idsList = new LinkedList<>();
		tree.root.forEachLevelOrderFromBottom(node -> node.replaceId("n" + node.getId()));
		tree.root.forEachLevelOrderFromBottom(node -> idsList.add(node.getId()));
		
		List<String> expectedIdsList = Arrays.asList("nC1","nC2","nB1","nB2","nB3","nA1");
		Multiset<String> expectedIdsListWrapped =  HashMultiset.create(expectedIdsList);
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		
		assertEquals(expectedIdsList, idsList);
		assertEquals(expectedIdsListWrapped, treeIds);
		assertEquals(expectedIdsListWrapped, idsInIndex);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_forEachLevelOrderFromBottom_custom_ordering() {
		NTree<String,Integer> tree = testTree();
		tree.useCustomOrdering((a,b) -> a.getValue() - b.getValue());;
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		List<String> idsList = new LinkedList<>();
		tree.root.forEachLevelOrderFromBottom(node -> node.replaceId("n" + node.getId()));
		tree.root.forEachLevelOrderFromBottom(node -> idsList.add(node.getId()));
		
		List<String> expectedIdsList = Arrays.asList("nC2","nC1","nB3","nB2","nB1","nA1");
		Multiset<String> expectedIdsListWrapped =  HashMultiset.create(expectedIdsList);
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		
		assertEquals(expectedIdsList, idsList);
		assertEquals(expectedIdsListWrapped, treeIds);
		assertEquals(expectedIdsListWrapped, idsInIndex);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_forEachLevelOrderFromBottom_natural_ordering_tree_manipulation() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.addNewRootSubtree(
			tree.n("A1").c(
				tree.n("B1"),
				tree.n("B2")));
		
		tree.useNaturalOrdering();
		tree.addIndex(IDS_INDEX, node -> node.getId());
		
		tree.root.forEachLevelOrderFromBottom(node -> node.setChildSubtreesIfAbsent(tree.n("X")));
		
		NTree<String,Integer> t = NTree.create("tree");
		t.addNewRootSubtree(
			t.n("A1").c(
				t.n("B1").c(
					t.n("X")),
				t.n("B2").c(
					t.n("X")),
				t.n("X")));
		
		Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A1","B1","B2","X","X","X"));
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		
		assertTrue(tree.equals(t));
		assertEquals(expectedIds, treeIds);
		assertEquals(expectedIds, idsInIndex);
	}
	
}
