package veve.datastructures.trees;

public class TestUtil {
	
//	static final String IDS_INDEX = "idsIndex";
//	static final String PARENT_IDS_INDEX = "parentIdsIndex";
	
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
	
//	static NTree<String,Integer> testTreeWithIndexes() {
//		
//	}
	
}
