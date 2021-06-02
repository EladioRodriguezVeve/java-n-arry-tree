package veve.datastructures.trees;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.gson.reflect.TypeToken;

import veve.datastructures.trees.NTreeConstants.TreeTraversalOrder;

public class NTreeNodeTest_DerivedPropsAndConvinience {
	
	@Test void test_height() {
		NTree<String,Integer> tree = TestUtil.testTree();
		assertEquals(3, tree.root.height());
	}
	
	@Test void test_subtreeSize() {
		NTree<String,Integer> tree = TestUtil.testTree();
		assertEquals(5, tree.root.size());
	}
	
	@Test void test_levelFromRoot() {
		NTree<String,Integer> tree = TestUtil.testTree();
		NTreeNode<String,Integer> a1 = tree.root;
		NTreeNode<String,Integer> b1 = tree.findFirstWithId("B1");
		NTreeNode<String,Integer> c1 = tree.findFirstWithId("C1");
		assertEquals(1,a1.levelFromRoot());
		assertEquals(2,b1.levelFromRoot());
		assertEquals(3,c1.levelFromRoot());
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_levelRelativeToAncestor() {
		NTree<String,Integer> t = NTree.create("tree");
		t.addNewRoot(
			t.n("A1").c(
				t.n("B1"),
				t.n("B2").c(
					t.n("C1"),
					t.n("C2").c(
						t.n("D1"),
						t.n("D2").c(
							t.n("E1"),
							t.n("E2"))))));
		
		NTreeNode<String,Integer> a1 = t.root;
		NTreeNode<String,Integer> b1 = t.findFirstWithId("B1");
		NTreeNode<String,Integer> b2 = t.findFirstWithId("B2");
		NTreeNode<String,Integer> c2 = t.findFirstWithId("C2");
		NTreeNode<String,Integer> d2 = t.findFirstWithId("D2");
		NTreeNode<String,Integer> e2 = t.findFirstWithId("E2");
		assertEquals(1, e2.levelRelativeToAncestor(e2));
		assertEquals(2, e2.levelRelativeToAncestor(d2));
		assertEquals(3, e2.levelRelativeToAncestor(c2));
		assertEquals(4, e2.levelRelativeToAncestor(b2));
		assertEquals(5, e2.levelRelativeToAncestor(a1));
		assertEquals(-1, e2.levelRelativeToAncestor(b1));
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_toMapOfLists() {
		NTree<String,Integer> t = NTree.create("tree");
		t.addNewRoot(
			t.n("A1").c(
				t.n("B1").c(
					t.n("A1"),
					t.n("B2")),
				t.n("B2").c(
					t.n("C1"),
					t.n("C2"))));
		
		Map<String,List<NTreeNode<String,Integer>>> mol = t.root.toMapOfLists();
		
		Map<String,List<NTreeNode<String,Integer>>> expected = new HashMap<>();
		expected.computeIfAbsent("A1", node -> new LinkedList<NTreeNode<String,Integer>>()).add(t.n("A1"));
		expected.computeIfAbsent("A1", node -> new LinkedList<NTreeNode<String,Integer>>()).add(t.n("A1"));
		expected.computeIfAbsent("B1", node -> new LinkedList<NTreeNode<String,Integer>>()).add(t.n("B1"));
		expected.computeIfAbsent("B2", node -> new LinkedList<NTreeNode<String,Integer>>()).add(t.n("B2"));
		expected.computeIfAbsent("B2", node -> new LinkedList<NTreeNode<String,Integer>>()).add(t.n("B2"));
		expected.computeIfAbsent("C1", node -> new LinkedList<NTreeNode<String,Integer>>()).add(t.n("C1"));
		expected.computeIfAbsent("C2", node -> new LinkedList<NTreeNode<String,Integer>>()).add(t.n("C2"));
		assertEquals(expected, mol);
	}
	
	@Test void test_toList() {
		NTree<String,Integer> t = TestUtil.testTree();
		
		Multiset<NTreeNode<String,Integer>> list =  HashMultiset.create(t.root.toList());
		
		LinkedList<NTreeNode<String,Integer>> expected = new LinkedList<>(Arrays.asList(
			t.n("A1",1),
			t.n("B1",2),
			t.n("B2",3),
			t.n("C1",4),
			t.n("C2",5)
		));
		Multiset<NTreeNode<String,Integer>> expectedList =  HashMultiset.create(expected);
		assertEquals(expectedList, list);
	}
	
	@SuppressWarnings("unchecked")
	@Test void test_nodeAndConnectedNodes() {
		NTree<String,Integer> t = TestUtil.testTree();
		NTreeNode<String,Integer> b1 = t.findFirstWithId("B1");
		NTreeNode<String,Integer> node = t.n("X").c(t.n("Y").c(t.n("Z")));
		NTreeNode<String,Integer> y = node.findFirstWithId("Y");
		
		Multiset<NTreeNode<String,Integer>> rootConnected = HashMultiset.create(t.root.nodeAndConnectedNodes());
		Multiset<NTreeNode<String,Integer>> b1Connected = HashMultiset.create(b1.nodeAndConnectedNodes());
		Multiset<NTreeNode<String,Integer>> nodeConnected = HashMultiset.create(node.nodeAndConnectedNodes());
		Multiset<NTreeNode<String,Integer>> yConnected = HashMultiset.create(y.nodeAndConnectedNodes());
		
		Multiset<NTreeNode<String,Integer>> expRootConnected = HashMultiset.create(Arrays.asList(t.n("A1",1),t.n("B1",2),t.n("B2",3)));
		Multiset<NTreeNode<String,Integer>> expB1Connected = HashMultiset.create(Arrays.asList(t.n("B1",2),t.n("A1",1),t.n("C1",4),t.n("C2",5)));
		Multiset<NTreeNode<String,Integer>> expNodeConnected = HashMultiset.create(Arrays.asList(t.n("X"),t.n("Y")));
		Multiset<NTreeNode<String,Integer>> expYConnected = HashMultiset.create(Arrays.asList(t.n("Y"),t.n("X"),t.n("Z")));
		assertEquals(expRootConnected, rootConnected);
		assertEquals(expB1Connected, b1Connected);
		assertEquals(nodeConnected, expNodeConnected);
		assertEquals(yConnected, expYConnected);
	}
	
	@Test void test_farthestAncestor() {
		NTree<String,Integer> t = TestUtil.testTree();
		NTreeNode<String,Integer> c1 = t.findFirstWithId("C1");
		
		NTreeNode<String,Integer> ancestor = c1.farthestAncestor();
		assertSame(t.root, ancestor);
		assertNull(t.root.farthestAncestor());
	}
	
	@Test void test_hasAncestor() {
		NTree<String,Integer> t = TestUtil.testTree();
		NTreeNode<String,Integer> b1 = t.findFirstWithId("B1");
		NTreeNode<String,Integer> c1 = t.findFirstWithId("C1");
		
		assertTrue(c1.hasAncestor(t.root));
		assertFalse(t.root.hasAncestor(t.root));
		assertFalse(t.root.hasAncestor(t.n("X")));
		assertFalse(b1.hasAncestor(c1));
	}
	
	@Test void test_nodesUpToAncestor() {
		NTree<String,Integer> t = TestUtil.testTree();
		NTreeNode<String,Integer> a1 = t.findFirstWithId("A1");
		NTreeNode<String,Integer> b1 = t.findFirstWithId("B1");
		NTreeNode<String,Integer> c1 = t.findFirstWithId("C1");
		
		List<NTreeNode<String,Integer>> fromC1toA1 = c1.nodesUpToAncestor(t.root);
		
		List<NTreeNode<String,Integer>> expected = Arrays.asList(c1,b1,a1);
		assertEquals(expected, fromC1toA1);
	}

	@Test void test_nodesInLevel_less_than_1() {
		NTree<String,Integer> t = TestUtil.testTree();
		
		Exception exception = assertThrows(RuntimeException.class, () -> {
			t.root.nodesInLevel(0);
		});
		
		assertTrue(exception.getMessage().contains("level cannot be less than 1"));
	}
	
	@Test void test_nodesInLevel() {
		NTree<String,Integer> t = TestUtil.testTree();
		
		Multiset<NTreeNode<String,Integer>> nodesL1 = HashMultiset.create(t.root.nodesInLevel(1));
		Multiset<NTreeNode<String,Integer>> nodesL2 = HashMultiset.create(t.root.nodesInLevel(2));
		Multiset<NTreeNode<String,Integer>> nodesL3 = HashMultiset.create(t.root.nodesInLevel(3));
		List<NTreeNode<String,Integer>> nodesL4 = t.root.nodesInLevel(4);
		
		Multiset<NTreeNode<String,Integer>> expectedL1 = HashMultiset.create(Arrays.asList(t.n("A1",1)));
		Multiset<NTreeNode<String,Integer>> expectedL2 = HashMultiset.create(Arrays.asList(t.n("B1",2),t.n("B2",3)));
		Multiset<NTreeNode<String,Integer>> expectedL3 = HashMultiset.create(Arrays.asList(t.n("C1",4),t.n("C2",5)));
		assertEquals(expectedL1, nodesL1);
		assertEquals(expectedL2, nodesL2);
		assertEquals(expectedL3, nodesL3);
		assertEquals(0, nodesL4.size());
	}
	
	@Test void test_mapToList() {
		NTree<String,Integer> t = TestUtil.testTree();
		t.useNaturalOrdering();
		
		List<String> ids = t.root.mapToList(TreeTraversalOrder.LEVEL_ORDER, node -> node.id);
		
		List<String> expected = Arrays.asList("A1","B1","B2","C1","C2");
		assertEquals(expected, ids);
	}
	
	@Test void test_findAll() {
		NTree<String,Integer> t = TestUtil.testTree();
		t.useNaturalOrdering();
		
		List<NTreeNode<String,Integer>> ids = t.root.findAll(TreeTraversalOrder.LEVEL_ORDER, node -> node.value % 2 != 0);
		
		List<NTreeNode<String,Integer>> expected = Arrays.asList(t.n("A1",1),t.n("B2",3),t.n("C2",5));
		assertEquals(expected, ids);
	}
	
	//TODO Complete with assertions
	@SuppressWarnings("unchecked")
	@Test void test_treeStringGraph() {
		NTree<String,Integer> t = NTree.create("tree");
		t.addNewRoot(
			t.n("A1").c(
				t.n("B1").c(
					t.n("C1").c(
						t.n("D1",1)
					)
				),
				t.n("B2").c(
					t.n("C2").c(
						t.n("D2",2)
					),
					t.n("C3",3)
				),
				t.n("B3").c(
					t.n("C4").c(
						t.n("D3").c(
							t.n("E1",4)
						)
					),
					t.n("C5",5)
				),
				t.n("B4")
			)
		);
		
		NTree<String,ClassForTesting<Integer>> t2 = NTree.create("tree");
		t2.addNewRoot(
			t2.n("A1").c(
				t2.n("B1", new ClassForTesting<Integer>(2)).c(
					t2.n("C1").c(
						t2.n("D1")
					)
				)
			)
		);
		
		String graph1T1 = t.root.treeGraph(3, 2, node -> node.getValue());
		String expected1T1 = 
				  "A1: null\n"
				+ "│\n"
				+ "└──B1: null\n"
				+ "│  │\n"
				+ "│  └──C1: null\n"
				+ "│     │\n"
				+ "│     └──D1: 1\n"
				+ "│\n"
				+ "└──B2: null\n"
				+ "│  │\n"
				+ "│  └──C2: null\n"
				+ "│  │  │\n"
				+ "│  │  └──D2: 2\n"
				+ "│  │\n"
				+ "│  └──C3: 3\n"
				+ "│\n"
				+ "└──B3: null\n"
				+ "│  │\n"
				+ "│  └──C4: null\n"
				+ "│  │  │\n"
				+ "│  │  └──D3: null\n"
				+ "│  │     │\n"
				+ "│  │     └──E1: 4\n"
				+ "│  │\n"
				+ "│  └──C5: 5\n"
				+ "│\n"
				+ "└──B4: null\n"
				+ "\n"
				+ "";
		assertEquals(expected1T1, graph1T1);
		
		String graph2T1 = t.root.treeGraph(null, null, null);
		String expected2T1 = 
				  "A1\n"
				+ "│\n"
				+ "└───B1\n"
				+ "│   │\n"
				+ "│   └───C1\n"
				+ "│       │\n"
				+ "│       └───D1\n"
				+ "│\n"
				+ "└───B2\n"
				+ "│   │\n"
				+ "│   └───C2\n"
				+ "│   │   │\n"
				+ "│   │   └───D2\n"
				+ "│   │\n"
				+ "│   └───C3\n"
				+ "│\n"
				+ "└───B3\n"
				+ "│   │\n"
				+ "│   └───C4\n"
				+ "│   │   │\n"
				+ "│   │   └───D3\n"
				+ "│   │       │\n"
				+ "│   │       └───E1\n"
				+ "│   │\n"
				+ "│   └───C5\n"
				+ "│\n"
				+ "└───B4\n"
				+ "\n"
				+ "";
		assertEquals(expected2T1, graph2T1);
		
		String graph1T2 = t2.root.treeGraph(3, 2, node -> node.getValue());
		String expected1T2 = 
				  "A1: null\n"
				+ "│\n"
				+ "└──B1: Value: 2\n"
				+ "   │\n"
				+ "   └──C1: null\n"
				+ "      │\n"
				+ "      └──D1: null\n"
				+ "\n"
				+ "";
		assertEquals(expected1T2, graph1T2);
	}
	
	@Test void test_toJson_and_fromJson() {
		NTree<String,Integer> t = TestUtil.testTree();
		Type nodeValueType = new TypeToken<Integer>(){}.getType();
		
		String json = t.root.toJson();
		NTreeNode<String, Integer> clone = NTreeNode.fromJson(json, t, nodeValueType);
		
		assertEquals(t.root, clone);
		assertNotSame(t.root, clone);
	}
	
}
