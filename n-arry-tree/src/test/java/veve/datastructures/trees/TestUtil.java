package veve.datastructures.trees;

public class TestUtil {
	
	@SuppressWarnings("unchecked")
	static NTree<String,Integer> testTree() {
		NTree<String,Integer> t = NTree.create("t");
		t.addNewRoot(
			t.n("A1", 1).c(
				t.n("B1", 2).c(
					t.n("C1", 4),
					t.n("C2", 5)),
				t.n("B2", 3)));
		return t;
	}
	
	@SuppressWarnings("unchecked")
	static NTree<String,Integer> testTreeWithNullValues() {
		NTree<String,Integer> t = NTree.create("t");
		t.addNewRoot(
			t.n("A1").c(
				t.n("B1", 2).c(
					t.n("C1"),
					t.n("C2", 5)),
				t.n("B2", 3)));
		return t;
	}
	
}
