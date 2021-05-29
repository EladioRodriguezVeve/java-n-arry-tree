package veve.datastructures.trees;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class NTreeNodeTest_NodeEdits {
	
	static final String IDS_INDEX = "idsIndex";
	static final String PARENT_IDS_INDEX = "parentIdsIndex";
	
	//==============================================================================================
	//	replace
	//==============================================================================================
	
	@Test void test_replaceSingleNodeWith_same_node() {
    	NTree<String,Integer> tree = NTree.create("tree");
    	NTreeNode<String,Integer> node = tree.createNode("node");
    	
    	assertNull(node.replaceSingleNodeWith(node));
    }
    
    @Test void test_replaceSingleNodeWith_is_bastard_node() {
    	NTree<String,Integer> tree = NTree.create("tree");
    	NTreeNode<String,Integer> nodeA = tree.createNode("A");
    	NTreeNode<String,Integer> nodeB = tree.createNode("B");
    	
    	assertNull(nodeA.replaceSingleNodeWith(nodeB));
    }
    
    @SuppressWarnings("unchecked")
	@Test void test_replaceSingleNodeWith_is_root() {
    	NTree<String,Integer> tA = TestUtil.testTree();
    	tA.addIndex(IDS_INDEX, node -> node.getId());
    	tA.addIndex(PARENT_IDS_INDEX, node -> node.getParent().getId());
    	
    	NTree<String,Integer> tB = NTree.create("tB");
    	NTreeNode<String,Integer> replacement = tB.n("X");
    	NTreeNode<String,Integer> replacedRoot = tA.getRoot();
    	
    	NTreeNode<String,Integer> x = tA.root.replaceSingleNodeWith(replacement);
    	
    	Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("X","B1","B2","C1","C2"));
    	Multiset<String> treeIds =  HashMultiset.create(tA.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tA.indexes.get(IDS_INDEX).keysList());
		
		NTreeNode<String,Integer> xInIdsIndex = tA.firstNodeInIndexWithKey(IDS_INDEX, "X");
		List<NTreeNode<String,Integer>> childsInParentIdsIndex = tA.nodesInIndexWithKey(PARENT_IDS_INDEX, "X");
		
		assertNotSame(tA.getRoot(), replacement);
		assertEquals(expectedIds, treeIds);
		assertEquals(x, replacedRoot);
		assertEquals(expectedIds, idsInIndex);
		assertSame(tA.getRoot(), xInIdsIndex);
		childsInParentIdsIndex.forEach(node -> {
			if (node.getParent() != tA.getRoot())
				fail();
		});
    }
    
    @SuppressWarnings("unchecked")
	@Test void test_replaceSingleNodeWith_sibling_has_id_already() {
    	NTree<String,Integer> tA = TestUtil.testTree();
    	tA.addIndex(IDS_INDEX, node -> node.getId());
    	NTree<String,Integer> tB = NTree.create("tB");
    	NTreeNode<String,Integer> replacement = tB.n("B2");
    	
    	NTreeNode<String,Integer> tAb1 = tA.findFirstWithId("B1");
    	NTreeNode<String,Integer> oldNode = tAb1.replaceSingleNodeWith(replacement);
    	
    	Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A1","B1","B2","C1","C2"));
    	Multiset<String> treeIds =  HashMultiset.create(tA.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tA.indexes.get(IDS_INDEX).keysList());
		assertNull(oldNode);
		assertEquals(expectedIds,treeIds);
		assertEquals(expectedIds,idsInIndex);
    }
    
    @SuppressWarnings("unchecked")
	@Test void test_replaceSingleNodeWith() {
    	final String HAS_CHILD_WITH_ID_X_INDEX = "HAS_CHILD_WITH_ID_X_INDEX";
    	NTree<String,Integer> tA = TestUtil.testTree();
    	tA.addIndex(IDS_INDEX, node -> node.getId());
    	tA.addIndex(PARENT_IDS_INDEX, node -> node.getParent().getId());
    	tA.addIndex(HAS_CHILD_WITH_ID_X_INDEX, node -> node.childrenIds().contains("X"));
    	NTree<String,Integer> tB = NTree.create("tB");
    	NTreeNode<String,Integer> replacement = tB.n("X");
    	
    	NTreeNode<String,Integer> tAb1 = tA.findFirstWithId("B1");
    	NTreeNode<String,Integer> replaced = tAb1.replaceSingleNodeWith(replacement);
    	
    	Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A1","X","B2","C1","C2"));
    	Multiset<String> treeIds =  HashMultiset.create(tA.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tA.indexes.get(IDS_INDEX).keysList());
		NTreeNode<String,Integer> x = tA.findFirstWithId("X");
		NTreeNode<String,Integer> xInIndex = tA.firstNodeInIndexWithKey(IDS_INDEX, "X");
		
		List<NTreeNode<String,Integer>> nodesInParentIdsIndex = tA.nodesInIndexWithKey(PARENT_IDS_INDEX, "X");
		NTreeNode<String,Integer> nodeInHasChildWithIdXIndex = tA.firstNodeInIndexWithKey(HAS_CHILD_WITH_ID_X_INDEX, true);
		
		assertNotSame(tAb1, replacement);
		assertEquals(expectedIds,treeIds);
		assertEquals(expectedIds,idsInIndex);
		assertSame(replaced, tAb1);
		assertSame(x, xInIndex);
		nodesInParentIdsIndex.forEach(node -> {
			if (node.getParent() != x)
				fail();
		});
		assertSame(x, nodeInHasChildWithIdXIndex.childWithId("X"));
    }
    
	//==============================================================================================
	//	replaceSubtree
	//==============================================================================================
    
    @Test void test_replaceWith_same_node() {
    	NTree<String,Integer> tree = TestUtil.testTree();
    	
    	assertNull(tree.root.replaceWith(tree.root));
    }
    
    @SuppressWarnings("unchecked")
	@Test void test_replaceWith_is_bastard_node() {
    	NTree<String,Integer> tree = NTree.create("tree");
    	NTreeNode<String,Integer> nodeA = tree.createNode("A").c(tree.n("B"));
    	NTreeNode<String,Integer> nodeB = tree.createNode("B");
    	
    	assertNull(nodeA.replaceWith(nodeB));
    }
    
    @SuppressWarnings("unchecked")
	@Test void test_replaceWith_has_sibling_with_same_id() {
    	NTree<String,Integer> tA = TestUtil.testTree();
    	tA.addIndex(IDS_INDEX, node -> node.getId());
    	NTree<String,Integer> tB = NTree.create("tB");
    	NTreeNode<String,Integer> replacement = tB.n("B2");
    	
    	NTreeNode<String,Integer> tAb1 = tA.findFirstWithId("B1");
    	NTreeNode<String,Integer> oldNode = tAb1.replaceWith(replacement);
    	
    	Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A1","B1","B2","C1","C2"));
    	Multiset<String> treeIds =  HashMultiset.create(tA.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tA.indexes.get(IDS_INDEX).keysList());
		assertNull(oldNode);
		assertEquals(expectedIds,treeIds);
		assertEquals(expectedIds,idsInIndex);
    }
    
    @SuppressWarnings("unchecked")
	@Test void test_replaceWith() {
    	final String HAS_CHILD_WITH_ID_X_INDEX = "HAS_CHILD_WITH_ID_X_INDEX";
    	NTree<String,Integer> tA = TestUtil.testTree();
    	tA.addIndex(IDS_INDEX, node -> node.getId());
    	tA.addIndex(PARENT_IDS_INDEX, node -> node.getParent().getId());
    	tA.addIndex(HAS_CHILD_WITH_ID_X_INDEX, node -> node.childrenIds().contains("X1"));
    	NTree<String,Integer> tB = NTree.create("treeB");
    	tB.addNewRoot(tB.n("X1").c(tB.n("Y1"),tB.n("Y2")));
    	
    	NTreeNode<String,Integer> tAb1 = tA.findFirstWithId("B1");
    	NTreeNode<String,Integer> replaced = tAb1.replaceWith(tB.root); //
    	
    	Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A1","X1","Y1","Y2","B2"));
    	Multiset<String> treeIds =  HashMultiset.create(tA.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tA.indexes.get(IDS_INDEX).keysList());
		
		NTreeNode<String,Integer> x1 = tA.findFirstWithId("X1");
		List<NTreeNode<String,Integer>> childsOdX1InParentIdsIndex = tA.nodesInIndexWithKey(PARENT_IDS_INDEX, "X1");
		NTreeNode<String,Integer> nodeInHasChildWithIdXIndex = tA.firstNodeInIndexWithKey(HAS_CHILD_WITH_ID_X_INDEX, true);
		
		assertNotSame(tAb1, tB.root);
		assertEquals(expectedIds,treeIds);
		assertEquals(expectedIds,idsInIndex);
		
		assertSame(replaced, tAb1);
		childsOdX1InParentIdsIndex.forEach(node -> {
			if (node.getParent() != x1)
				fail();
		});
		assertSame(tA.getRoot(), nodeInHasChildWithIdXIndex);
    }
    
	//==============================================================================================
	//	removeSubtree
	//==============================================================================================
    
    @Test void test_remove_is_not_root_and_parent_is_null() {
    	NTree<String,Integer> tree = NTree.create("tree");
    	tree.addIndex(IDS_INDEX, node -> node.getId());
    	NTreeNode<String,Integer> node = tree.n("A");
    	
    	NTreeNode<String,Integer> removed = node.remove();
    	
    	assertNull(removed);
    	assertNull(tree.root);
    	assertEquals(0, tree.indexes.get(IDS_INDEX).indexTable.size());
    }
    
    @Test void test_remove_is_root() {
    	NTree<String,Integer> tree = TestUtil.testTree();
    	tree.addIndex(IDS_INDEX, node -> node.getId());
    	
    	NTreeNode<String,Integer> removed = tree.root.remove();
    	
    	assertNotNull(removed);
    	assertNull(tree.root);
    	assertEquals(0, tree.indexes.get(IDS_INDEX).indexTable.size());
    }
    
    @SuppressWarnings("unchecked")
	@Test void test_remove() {
    	final String HAS_CHILD_WITH_ID_B1_INDEX = "HAS_CHILD_WITH_ID_B1_INDEX";
    	NTree<String,Integer> tree = TestUtil.testTree();
    	tree.addIndex(IDS_INDEX, node -> node.getId());
    	tree.addIndex(PARENT_IDS_INDEX, node -> node.getParent().getId());
    	tree.addIndex(HAS_CHILD_WITH_ID_B1_INDEX, node -> node.childrenIds().contains("B1"));
    	
    	NTreeNode<String,Integer> tAb1 = tree.findFirstWithId("B1");
    	NTreeNode<String,Integer> removed = tAb1.remove();
    	
    	Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A1","B2"));
    	Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		
		List<String> idsInParentIdsIndex =  tree.indexes.get(PARENT_IDS_INDEX).keysList();
		
		assertSame(removed, tAb1);
		assertEquals(expectedIds, idsInIndex);
		assertEquals(expectedIds, treeIds);
		assertEquals(Arrays.asList("A1"), idsInParentIdsIndex);
		assertNull(tree.firstNodeInIndexWithKey(HAS_CHILD_WITH_ID_B1_INDEX, "B1"));
    }
    
	//==============================================================================================
	//	removeAndParentAdoptsGrandChildren
	//==============================================================================================
    
    @Test void test_removeAndParentAdoptsGrandChildren_is_bastard_node() {
    	NTree<String,Integer> tree = NTree.create("tree");
    	NTreeNode<String,Integer> node = tree.n("node");
    	
    	assertNull(node.removeAndParentAdoptsGrandChildren());
    }
    
    @Test void test_removeAndParentAdoptsGrandChildren_is_root() {
    	NTree<String,Integer> tree = TestUtil.testTree();
    	assertNull(tree.root.removeAndParentAdoptsGrandChildren());
    }
    
    @SuppressWarnings("unchecked")
	@Test void test_removeAndParentAdoptsGrandChildren_replaces_uncle() {
    	final String HAS_CHILD_WITH_ID_B2_C1_INDEX = "HAS_CHILD_WITH_ID_B2_C1_INDEX";
    	NTree<String,Integer> tree = NTree.create("tree");
    	tree.addIndex(IDS_INDEX, node -> node.getId());
    	tree.addIndex(PARENT_IDS_INDEX, node -> node.getParent().getId());
    	tree.addIndex(HAS_CHILD_WITH_ID_B2_C1_INDEX, node -> node.childrenIds().containsAll(Arrays.asList("B2","C1")));
    	tree.addNewRoot(
    		tree.n("A1").c(
    			tree.n("B1").c(
    				tree.n("B2",2),
    				tree.n("C1")),
    			tree.n("B2")));
    	
    	NTreeNode<String,Integer> b1 = tree.getRoot().childWithId("B1");
    	NTreeNode<String,Integer> b2ToBeRemoved = tree.getRoot().childWithId("B2");
    	List<NTreeNode<String,Integer>> removed = b1.removeAndParentAdoptsGrandChildren(true);
    	
    	Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A1","B2", "C1"));
    	Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId())); 
		Multiset<String> idsInIdsIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		assertEquals(expectedIds,treeIds);
		assertEquals(expectedIds,idsInIdsIndex);
		assertEquals(Arrays.asList(b1, b2ToBeRemoved), removed);
		
		List<NTreeNode<String,Integer>> leftB2s = tree.findAll(node -> node.getId().equals("B2"));
		assertEquals(1, leftB2s.size());
		assertEquals(2, leftB2s.get(0).getValue());
		
		Multiset<NTreeNode<String,Integer>> nodesInParentIdsIndexWithA1Parent = HashMultiset.create(tree.nodesInIndexWithKey(PARENT_IDS_INDEX, "A1"));
		Multiset<NTreeNode<String,Integer>> nodesInTreeWithA1Parent = HashMultiset.create(tree.findAll(node -> node.getParent().getId().equals("A1")));
		assertEquals(nodesInTreeWithA1Parent, nodesInParentIdsIndexWithA1Parent);
		
		NTreeNode<String,Integer> a1InHasChildIndex = tree.firstNodeInIndexWithKey(HAS_CHILD_WITH_ID_B2_C1_INDEX, true);
		assertSame(tree.getRoot(), a1InHasChildIndex);
    }
    
    @SuppressWarnings("unchecked")
	@Test void removeAndParentAdoptsGrandChildren_replace_child() {
    	final String HAS_CHILD_WITH_ID_B2_C1_INDEX = "HAS_CHILD_WITH_ID_B2_C1_INDEX";
    	NTree<String,Integer> tree = NTree.create("tree");
    	tree.addIndex(IDS_INDEX, node -> node.getId());
    	tree.addIndex(PARENT_IDS_INDEX, node -> node.getParent().getId());
    	tree.addIndex(HAS_CHILD_WITH_ID_B2_C1_INDEX, node -> node.childrenIds().containsAll(Arrays.asList("B2","C1")));
    	tree.addNewRoot(
    		tree.n("A1").c(
    			tree.n("B1").c(
    				tree.n("B2", 1),
    				tree.n("C1")),
    			tree.n("B2", 2)));
    	
    	NTreeNode<String,Integer> b1 = tree.getRoot().childWithId("B1");
    	NTreeNode<String,Integer> b2ToBeRemoved = tree.getRoot().childWithId("B1").childWithId("B2");
    	
    	List<NTreeNode<String,Integer>> removed = b1.removeAndParentAdoptsGrandChildren(false);
    	
    	Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A1","B2", "C1"));
    	Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		assertEquals(expectedIds,treeIds);
		assertEquals(expectedIds,idsInIndex);
		assertEquals(Arrays.asList(b1, b2ToBeRemoved), removed);
		
		List<NTreeNode<String,Integer>> leftB2s = tree.findAll(node -> node.getId().equals("B2"));
		assertEquals(1, leftB2s.size());
		assertEquals(2, leftB2s.get(0).getValue());
		
		Multiset<NTreeNode<String,Integer>> nodesInParentIdsIndexWithA1Parent = HashMultiset.create(tree.nodesInIndexWithKey(PARENT_IDS_INDEX, "A1"));
		Multiset<NTreeNode<String,Integer>> nodesInTreeWithA1Parent = HashMultiset.create(tree.findAll(node -> node.getParent().getId().equals("A1")));
		assertEquals(nodesInTreeWithA1Parent, nodesInParentIdsIndexWithA1Parent);
		
		NTreeNode<String,Integer> a1InHasChildIndex = tree.firstNodeInIndexWithKey(HAS_CHILD_WITH_ID_B2_C1_INDEX, true);
		assertSame(tree.getRoot(), a1InHasChildIndex);
    }
    
    @SuppressWarnings("unchecked")
    @Test void test_removeAndParentAdoptsGrandChildren_no_duplicates_with_uncles() {
    	final String HAS_CHILD_WITH_ID_INDEX = "HAS_CHILD_WITH_ID_INDEX";
	  	NTree<String,Integer> tree = NTree.create("tree");
	  	tree.addIndex(IDS_INDEX, node -> node.getId());
	  	tree.addIndex(PARENT_IDS_INDEX, node -> node.getParent().getId());
	  	tree.addIndex(HAS_CHILD_WITH_ID_INDEX, node -> node.childrenIds().containsAll(Arrays.asList("B2","C1","C2")));
	  	tree.addNewRoot(
	  		tree.n("A1").c(
	  			tree.n("B1").c(
	  				tree.n("C1", 1),
	  				tree.n("C2")),
	  			tree.n("B2", 2)));
	  	
	  	NTreeNode<String,Integer> b1 = tree.getRoot().childWithId("B1");
	  	
	  	List<NTreeNode<String,Integer>> removed = b1.removeAndParentAdoptsGrandChildren(true);
	  	
	  	Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A1","B2", "C1", "C2"));
		Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		Multiset<String> expectedA1ChildrenIds =  HashMultiset.create(Arrays.asList("B2", "C1", "C2"));
		Multiset<String> a1ChildrenIds =  HashMultiset.create(tree.getRoot().childrenIds());
		assertEquals(expectedIds,treeIds);
		assertEquals(expectedIds,idsInIndex);
		assertEquals(expectedA1ChildrenIds,a1ChildrenIds);
		assertEquals(Arrays.asList(b1), removed);
		
		Multiset<NTreeNode<String,Integer>> nodesInParentIdsIndexWithA1Parent = HashMultiset.create(tree.nodesInIndexWithKey(PARENT_IDS_INDEX, "A1"));
		Multiset<NTreeNode<String,Integer>> nodesInTreeWithA1Parent = HashMultiset.create(tree.findAll(node -> node.getParent().getId().equals("A1")));
		assertEquals(nodesInTreeWithA1Parent, nodesInParentIdsIndexWithA1Parent);
		
		NTreeNode<String,Integer> a1InHasChildIndex = tree.firstNodeInIndexWithKey(HAS_CHILD_WITH_ID_INDEX, true);
		assertSame(tree.getRoot(), a1InHasChildIndex);
	}
    
}
