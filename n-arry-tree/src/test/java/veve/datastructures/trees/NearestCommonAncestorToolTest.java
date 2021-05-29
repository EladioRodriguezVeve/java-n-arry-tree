package veve.datastructures.trees;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class NearestCommonAncestorToolTest {
	
	@SuppressWarnings("unchecked")
	NTree<String,Integer> testTree1() {
		NTree<String,Integer> t = NTree.create("tree");
		t.addNewRoot(
			t.n("A1").c(
				t.n("B1"),
				t.n("B2"))
		);
		return t;
	}
	
	@SuppressWarnings("unchecked")
	NTree<String,Integer> testTree2() {
		NTree<String,Integer> t = NTree.create("tree");
		t.addNewRoot(
			t.n("A1").c(
				t.n("B1"),
				t.n("B2").c(
					t.n("C1")))
		);
		return t;
	}
	
	@SuppressWarnings("unchecked")
	NTree<String,Integer> testTree3() {
		NTree<String,Integer> t = NTree.create("tree");
		t.addNewRoot(
			t.n("A1").c(
				t.n("B1").c(
					t.n("C1"),
					t.n("C2")))
		);
		return t;
	}
	
	@SuppressWarnings("unchecked")
	NTree<String,Integer> testTree4() {
		NTree<String,Integer> t = NTree.create("tree");
		t.addNewRoot(
			t.n("A1").c(
				t.n("B1").c(
					t.n("C1"),
					t.n("C2").c(
						t.n("D1"))))
		);
		return t;
	}

	@Test void different_hierarchies() {
		NTree<String,Integer> t1 = testTree1();
		NTree<String,Integer> t2 = testTree1();
		assertFalse(NearestCommonAncestorTool.forNodes(t1.root, t2.root).hasCommonAncestor());
	}
	
	@Test void same_node() {
		NTree<String,Integer> tree = testTree1();
		assertFalse(NearestCommonAncestorTool.forNodes(tree.root, tree.root).hasCommonAncestor());
	}
	
	@Test void test_commonAncestor() {
		NTree<String,Integer> t1 = testTree1();
		NTreeNode<String,Integer> t1B1 = t1.findFirstWithId("B1");
		NTreeNode<String,Integer> t1B2 = t1.findFirstWithId("B2");
		assertEquals(t1.root, NearestCommonAncestorTool.forNodes(t1B1,t1B2).commonAncestor());
		assertNull(NearestCommonAncestorTool.forNodes(t1.root,t1B2).commonAncestor());
		
		NTree<String,Integer> t2 = testTree2();
		NTreeNode<String,Integer> t2B1 = t2.findFirstWithId("B1");
		NTreeNode<String,Integer> t2B2 = t2.findFirstWithId("B2");
		NTreeNode<String,Integer> t2C1 = t2.findFirstWithId("C1");
		assertEquals(t2.root, NearestCommonAncestorTool.forNodes(t2B1,t2C1).commonAncestor());
		assertEquals(t2.root, NearestCommonAncestorTool.forNodes(t2C1,t2B1).commonAncestor());
		assertEquals(t2.root, NearestCommonAncestorTool.forNodes(t2C1,t2B2).commonAncestor());
		assertEquals(t2.root, NearestCommonAncestorTool.forNodes(t2B2,t2C1).commonAncestor());
		
		NTree<String,Integer> t3 = testTree3();
		NTreeNode<String,Integer> t3B1 = t3.findFirstWithId("B1");
		NTreeNode<String,Integer> t3C1 = t3.findFirstWithId("C1");
		NTreeNode<String,Integer> t3C2 = t3.findFirstWithId("C2");
		assertEquals(t3B1, NearestCommonAncestorTool.forNodes(t3C1,t3C2).commonAncestor());
		assertEquals(t3B1, NearestCommonAncestorTool.forNodes(t3C2,t3C1).commonAncestor());
		
		NTree<String,Integer> t4 = testTree4();
		NTreeNode<String,Integer> t4B1 = t4.findFirstWithId("B1");
		NTreeNode<String,Integer> t4C1 = t4.findFirstWithId("C1");
		NTreeNode<String,Integer> t4D1 = t4.findFirstWithId("D1");
		assertEquals(t4B1, NearestCommonAncestorTool.forNodes(t4C1,t4D1).commonAncestor());
		assertEquals(t4B1, NearestCommonAncestorTool.forNodes(t4D1,t4C1).commonAncestor());
	}
	
	@Test void test_nodesFromAtoAncestor() {
		NTree<String,Integer> t = testTree4();
		NTreeNode<String,Integer> a1 = t.findFirstWithId("A1");
		NTreeNode<String,Integer> b1 = t.findFirstWithId("B1");
		NTreeNode<String,Integer> c2 = t.findFirstWithId("C2");
		NTreeNode<String,Integer> d1 = t.findFirstWithId("D1");
		
		List<NTreeNode<String,Integer>> fromAtoCommonAncestor = NearestCommonAncestorTool.forNodes(d1,b1).nodesFromAtoAncestor();
		
		List<NTreeNode<String,Integer>> expected = new LinkedList<>(Arrays.asList(d1,c2,b1,a1));
		assertEquals(expected, fromAtoCommonAncestor);
	}
	
	@Test void test_nodesFromBtoAncestor() {
		NTree<String,Integer> t = testTree4();
		NTreeNode<String,Integer> a1 = t.findFirstWithId("A1");
		NTreeNode<String,Integer> b1 = t.findFirstWithId("B1");
		NTreeNode<String,Integer> d1 = t.findFirstWithId("D1");
		
		List<NTreeNode<String,Integer>> nodesFromBtoAncestor = NearestCommonAncestorTool.forNodes(d1,b1).nodesFromBtoAncestor();
		
		List<NTreeNode<String,Integer>> expected = new LinkedList<>(Arrays.asList(b1,a1));
		assertEquals(nodesFromBtoAncestor, expected);
	}
	
	@Test void test_nodesFromAncestorToA() {
		NTree<String,Integer> t = testTree4();
		NTreeNode<String,Integer> a1 = t.findFirstWithId("A1");
		NTreeNode<String,Integer> b1 = t.findFirstWithId("B1");
		NTreeNode<String,Integer> c2 = t.findFirstWithId("C2");
		NTreeNode<String,Integer> d1 = t.findFirstWithId("D1");
		
		List<NTreeNode<String,Integer>> nodesFromAncestorToA = NearestCommonAncestorTool.forNodes(d1,b1).nodesFromAncestorToA();
		
		List<NTreeNode<String,Integer>> expected = new LinkedList<>(Arrays.asList(a1,b1,c2,d1));
		assertEquals(expected, nodesFromAncestorToA);
	}
	
	@Test void test_nodesFromAncestorToB() {
		NTree<String,Integer> t = testTree4();
		NTreeNode<String,Integer> a1 = t.findFirstWithId("A1");
		NTreeNode<String,Integer> b1 = t.findFirstWithId("B1");
		NTreeNode<String,Integer> d1 = t.findFirstWithId("D1");
		
		List<NTreeNode<String,Integer>> nodesFromAncestorToB = NearestCommonAncestorTool.forNodes(d1,b1).nodesFromAncestorToB();
		
		List<NTreeNode<String,Integer>> expected = new LinkedList<>(Arrays.asList(a1,b1));
		assertEquals(expected, nodesFromAncestorToB);
	}
	
	@Test void test_nodesFromAtoB() {
		NTree<String,Integer> t1 = testTree1();
		NTreeNode<String,Integer> t1A1 = t1.findFirstWithId("A1");
		NTreeNode<String,Integer> t1B1 = t1.findFirstWithId("B1");
		NTreeNode<String,Integer> t1B2 = t1.findFirstWithId("B2");
		List<NTreeNode<String,Integer>> t1FromAtoB = NearestCommonAncestorTool.forNodes(t1B1,t1B2).nodesFromAtoB();
		List<NTreeNode<String,Integer>> expectedT1FromAtoB = Arrays.asList(t1B1,t1A1,t1B2);
		assertEquals(expectedT1FromAtoB, t1FromAtoB);
		
		NTree<String,Integer> t2 = testTree2();
		NTreeNode<String,Integer> t2A1 = t2.findFirstWithId("A1");
		NTreeNode<String,Integer> t2B1 = t2.findFirstWithId("B1");
		NTreeNode<String,Integer> t2B2 = t2.findFirstWithId("B2");
		NTreeNode<String,Integer> t2C1 = t2.findFirstWithId("C1");
		List<NTreeNode<String,Integer>> t2FromAtoB = NearestCommonAncestorTool.forNodes(t2B1,t2C1).nodesFromAtoB();
		List<NTreeNode<String,Integer>> expectedT2FromAtoB = Arrays.asList(t2B1,t2A1,t2B2,t2C1);
		assertEquals(expectedT2FromAtoB, t2FromAtoB);
		
		NTree<String,Integer> t3 = testTree3();
		NTreeNode<String,Integer> t3B1 = t3.findFirstWithId("B1");
		NTreeNode<String,Integer> t3C1 = t3.findFirstWithId("C1");
		NTreeNode<String,Integer> t3C2 = t3.findFirstWithId("C2");
		List<NTreeNode<String,Integer>> t3FromAtoB = NearestCommonAncestorTool.forNodes(t3C1,t3C2).nodesFromAtoB();
		List<NTreeNode<String,Integer>> expectedT3FromAtoB = Arrays.asList(t3C1,t3B1,t3C2);
		assertEquals(expectedT3FromAtoB, t3FromAtoB);
		
		NTree<String,Integer> t4 = testTree4();
		NTreeNode<String,Integer> t4B1 = t4.findFirstWithId("B1");
		NTreeNode<String,Integer> t4C1 = t4.findFirstWithId("C1");
		NTreeNode<String,Integer> t4C2 = t4.findFirstWithId("C2");
		NTreeNode<String,Integer> t4D1 = t4.findFirstWithId("D1");
		List<NTreeNode<String,Integer>> t4FromAtoB = NearestCommonAncestorTool.forNodes(t4D1,t4C1).nodesFromAtoB();
		List<NTreeNode<String,Integer>> expectedT4FromAtoB = Arrays.asList(t4D1,t4C2,t4B1,t4C1);
		assertEquals(expectedT4FromAtoB, t4FromAtoB);
	}
	
	@Test void test_nodesFromBtoA() {
		NTree<String,Integer> t1 = testTree1();
		NTreeNode<String,Integer> t1A1 = t1.findFirstWithId("A1");
		NTreeNode<String,Integer> t1B1 = t1.findFirstWithId("B1");
		NTreeNode<String,Integer> t1B2 = t1.findFirstWithId("B2");
		List<NTreeNode<String,Integer>> t1FromAtoB = NearestCommonAncestorTool.forNodes(t1B1,t1B2).nodesFromBtoA();
		List<NTreeNode<String,Integer>> expectedT1FromAtoB = Arrays.asList(t1B2,t1A1,t1B1);
		assertEquals(expectedT1FromAtoB, t1FromAtoB);
		
		NTree<String,Integer> t2 = testTree2();
		NTreeNode<String,Integer> t2A1 = t2.findFirstWithId("A1");
		NTreeNode<String,Integer> t2B1 = t2.findFirstWithId("B1");
		NTreeNode<String,Integer> t2B2 = t2.findFirstWithId("B2");
		NTreeNode<String,Integer> t2C1 = t2.findFirstWithId("C1");
		List<NTreeNode<String,Integer>> t2FromAtoB = NearestCommonAncestorTool.forNodes(t2B1,t2C1).nodesFromBtoA();
		List<NTreeNode<String,Integer>> expectedT2FromAtoB = Arrays.asList(t2C1,t2B2,t2A1,t2B1);
		assertEquals(expectedT2FromAtoB, t2FromAtoB);
		
		NTree<String,Integer> t3 = testTree3();
		NTreeNode<String,Integer> t3B1 = t3.findFirstWithId("B1");
		NTreeNode<String,Integer> t3C1 = t3.findFirstWithId("C1");
		NTreeNode<String,Integer> t3C2 = t3.findFirstWithId("C2");
		List<NTreeNode<String,Integer>> t3FromAtoB = NearestCommonAncestorTool.forNodes(t3C1,t3C2).nodesFromBtoA();
		List<NTreeNode<String,Integer>> expectedT3FromAtoB = Arrays.asList(t3C2,t3B1,t3C1);
		assertEquals(expectedT3FromAtoB, t3FromAtoB);
		
		NTree<String,Integer> t4 = testTree4();
		NTreeNode<String,Integer> t4B1 = t4.findFirstWithId("B1");
		NTreeNode<String,Integer> t4C1 = t4.findFirstWithId("C1");
		NTreeNode<String,Integer> t4C2 = t4.findFirstWithId("C2");
		NTreeNode<String,Integer> t4D1 = t4.findFirstWithId("D1");
		List<NTreeNode<String,Integer>> t4FromAtoB = NearestCommonAncestorTool.forNodes(t4D1,t4C1).nodesFromBtoA();
		List<NTreeNode<String,Integer>> expectedT4FromAtoB = Arrays.asList(t4C1,t4B1,t4C2,t4D1);
		assertEquals(expectedT4FromAtoB, t4FromAtoB);
	}
	
}
