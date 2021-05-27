package veve.datastructures.trees;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class NTreeNodeTest_Basics {
	
	static final String IDS_INDEX = "idsIndex";
	static final String PARENT_IDS_INDEX = "parentIdsIndex";
	
	
	//==============================================================================================
	//	replaceId
	//==============================================================================================
	
    @Test void test_replaceId_same_id() {
    	NTree<String,Integer> tree = NTree.create("tree");
    	NTreeNode<String,Integer> node = tree.createNode("A");
    	
    	boolean replaced = node.replaceId("A");
    	
    	assertFalse(replaced);
    	assertEquals("A", node.id);
    }
    
	@Test void test_replaceId_not_root_and_parent_is_null() {
    	NTree<String,Integer> tree = NTree.create("tree");
    	NTreeNode<String,Integer> nodeA = tree.createNode("A");
    	
    	boolean replaced = nodeA.replaceId("X1");

    	assertTrue(replaced);
    	assertEquals("X1", nodeA.id);
    }
    
    @SuppressWarnings("unchecked")
	@Test void test_replaceId_is_root() {
    	NTree<String,Integer> tree = TestUtil.testTree();
    	tree.addIndex(IDS_INDEX, node -> node.getId());
    	tree.addIndex(PARENT_IDS_INDEX, node -> node.getParent().getId());
    	
    	boolean wasReplaced = tree.getRoot().replaceId("X1");
    	
    	Multiset<String> expIds =  HashMultiset.create(Arrays.asList("X1","B1","B2","C1","C2"));
    	Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIdsIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		NTreeNode<String,Integer> x1InIdsIndex = tree.getFirstNodeInIndex(IDS_INDEX, "X1");
		List<NTreeNode<String,Integer>> x1NodesInParentIdsIndex = tree.getNodesInIndex(PARENT_IDS_INDEX, "X1");
    	assertTrue(wasReplaced);
    	assertEquals("X1", tree.getRoot().getId());
    	assertEquals(expIds, treeIds);
    	assertEquals(idsInIdsIndex, treeIds);
    	assertSame(x1InIdsIndex, tree.getRoot());
    	x1NodesInParentIdsIndex.forEach(node -> {
    		if(node.getParent() != tree.getRoot())
    			fail();
    	});
    }
    
	@Test void test_replaceId_sibling_has_id_already() {
    	NTree<String,Integer> tree = TestUtil.testTree();
    	
    	NTreeNode<String,Integer> b1 = tree.findFirstWithId("B1");
    	boolean replaced = b1.replaceId("B2");
    	
    	assertFalse(replaced);
    	assertEquals("B1", b1.id);
    }
	
    @SuppressWarnings("unchecked")
	@Test void test_replaceId_non_root() {
    	final String HAS_CHILD_WITH_ID_X1_INDEX = "hasChildWithIdX1Index";
    	NTree<String,Integer> tree = TestUtil.testTree();
    	tree.addIndex(IDS_INDEX, node -> node.getId());
    	tree.addIndex(PARENT_IDS_INDEX, node -> node.getParent().getId());
    	tree.addIndex(HAS_CHILD_WITH_ID_X1_INDEX, node -> node.getChildrenIds().contains("X1"));
    	
    	NTreeNode<String,Integer> b1 = tree.findFirst(node -> node.id.equals("B1"));
    	boolean wasReplaced = b1.replaceId("X1");
    	
    	NTreeNode<String,Integer> x1 = tree.findFirstWithId("X1");
    	Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A1","X1","B2","C1","C2"));
    	Multiset<String> treeIds =  HashMultiset.create(tree.mapToList(node -> node.getId()));
		Multiset<String> idsInIndex =  HashMultiset.create(tree.indexes.get(IDS_INDEX).keysList());
		
		NTreeNode<String,Integer> x1InIdsIndex = tree.getFirstNodeInIndex(IDS_INDEX, "X1");
		List<NTreeNode<String,Integer>> x1NodesInParentIdsIndex = tree.getNodesInIndex(PARENT_IDS_INDEX, "X1");
		NTreeNode<String,Integer> nodeInHasChildWithIdX1Index = tree.getFirstNodeInIndex(HAS_CHILD_WITH_ID_X1_INDEX, true);
		
    	assertTrue(wasReplaced);
    	assertEquals("X1", b1.id);
    	assertEquals(expectedIds, treeIds);
    	assertEquals(idsInIndex, treeIds);
    	
    	assertSame(x1InIdsIndex,  x1);
    	x1NodesInParentIdsIndex.forEach(node -> {
    		if (node.getParent() != x1)
    			fail();
    	});
    	assertSame(nodeInHasChildWithIdX1Index, x1.getParent());
    }
    
	//==============================================================================================
	//	setValue
	//==============================================================================================
    
    @SuppressWarnings("unchecked")
	@Test void test_setValue() {
    	final String VALUES_INDEX = "valuesIndex";
    	final String PARENT_VALUES_INDEX = "PARENT_VALUES_INDEX";
    	final String HAS_CHILD_WITH_VALUE_8_INDEX = "HAS_CHILD_WITH_VALUE_8_INDEX";
    	NTree<String,Integer> tree = TestUtil.testTree();
    	tree.addIndex(VALUES_INDEX, node -> node.getValue());
    	tree.addIndex(PARENT_VALUES_INDEX, node -> node.getParent().getValue());
    	tree.addIndex(HAS_CHILD_WITH_VALUE_8_INDEX, node -> node.getChildrenValues().contains(8));
    	
    	NTreeNode<String,Integer> b1 = tree.findFirstWithValue(2);
    	NTreeNode<String,Integer> b1After = b1.setValue(8);
    	
    	Multiset<Integer> expectedValues =  HashMultiset.create(Arrays.asList(1,8,3,4,5));
    	Multiset<Integer> treeValues =  HashMultiset.create(tree.mapToList(node -> node.getValue()));
		Multiset<Integer> valuesInIndex =  HashMultiset.create(tree.indexes.get(VALUES_INDEX).keysList());
		
		NTreeNode<String,Integer> b1InValuesIndex = tree.getFirstNodeInIndex(VALUES_INDEX, 8);
		List<NTreeNode<String,Integer>> value8NodesInParentIdsIndex = tree.getNodesInIndex(PARENT_VALUES_INDEX, 8);
		NTreeNode<String,Integer> nodeInHasChildWithIdX1Index = tree.getFirstNodeInIndex(HAS_CHILD_WITH_VALUE_8_INDEX, true);
		
		assertSame(b1After, b1);
    	assertEquals(8, b1.getValue());
    	assertEquals(expectedValues, treeValues);
    	assertEquals(expectedValues, valuesInIndex);
    	
    	assertSame(b1InValuesIndex, b1);
    	value8NodesInParentIdsIndex.forEach(node -> {
    		if (node.getParent().getValue() != 8)
    			fail();
    	});
    	assertSame(nodeInHasChildWithIdX1Index, b1.getParent());
    }
    
	//==============================================================================================
	//	getParent
	//==============================================================================================
    
    @Test void test_getParent_is_root() {
    	NTree<String,Integer> tree = TestUtil.testTree();
    	assertNull(tree.root.getParent());
    }
    
    @Test void test_getParent_is_null() {
    	NTree<String,Integer> tree = NTree.create("tree");
    	NTreeNode<String,Integer> node = tree.createNode("A");
    	
    	NTreeNode<String,Integer> parent = node.getParent();
    	
    	assertNull(parent);
    }
    
    @Test void test_getParent() {
    	NTree<String,Integer> tree = TestUtil.testTree();
    	NTreeNode<String,Integer> b1 = tree.findFirst(node -> node.id.equals("B1"));
    	
    	NTreeNode<String,Integer> parent = b1.getParent();
    	
    	assertEquals(tree.root, parent);
    	assertSame(tree.root, parent);
    }
    
	//==============================================================================================
	//	OVERRIDEN
	//==============================================================================================
    
    @Test void test_iterator() {
    	NTree<String,Integer> tree = TestUtil.testTree();
    	List<NTreeNode<String,Integer>> nodeList = tree.toList();
    	int index = 0;
    	
    	Iterator<NTreeNode<String,Integer>> iterator = tree.iterator();
    	
    	while(iterator.hasNext()) {
    		NTreeNode<String,Integer> node = iterator.next();
    		if (node != nodeList.get(index))
    			fail();
    		index++;
    	}
    }
    
    @Test void test_compareTo_value_implements_comparable() {
    	NTree<String,Integer> tree = NTree.create("tree");
    	NTreeNode<String,Integer> nodeA_null = tree.n("A");
    	NTreeNode<String,Integer> nodeA_1 = tree.n("A",1);
    	NTreeNode<String,Integer> nodeA_2 = tree.n("A",2);
    	NTreeNode<String,Integer> nodeB = tree.n("B");
    	
    	assertEquals(1, nodeA_null.compareTo(null));
    	assertEquals(0, nodeA_null.compareTo(nodeA_null));
    	assertEquals(-1, nodeA_null.compareTo(nodeA_1));
    	assertEquals(1, nodeA_1.compareTo(nodeA_null));
    	assertEquals(0, nodeA_1.compareTo(nodeA_1));
    	assertEquals(-1, nodeA_1.compareTo(nodeA_2));
    	assertEquals(1, nodeA_2.compareTo(nodeA_1));
    	assertTrue(nodeB.compareTo(nodeA_null) > 0);
    	assertTrue(nodeB.compareTo(nodeA_2) > 0);
    	assertTrue(nodeA_null.compareTo(nodeB) < 0);
    	assertTrue(nodeA_2.compareTo(nodeB) < 0);
    }
    
    @Test void test_compareTo_value_does_not_implement_comparable() {
    	NTree<String,NonComparableValue> tree = NTree.create("tree");
    	NTreeNode<String,NonComparableValue> nodeA_1 = tree.n("A", new NonComparableValue(1));
    	NTreeNode<String,NonComparableValue> nodeA_2 = tree.n("A", new NonComparableValue(2));
    	NTreeNode<String,NonComparableValue> nodeB = tree.n("B", new NonComparableValue(2));
    	
    	assertEquals(0, nodeA_1.compareTo(nodeA_2));
    	assertEquals(-1, nodeA_1.compareTo(nodeB));
    	assertEquals(1, nodeB.compareTo(nodeA_1));
    }
    
    @Test void test_equals() {
    	NTree<String,Integer> tree = NTree.create("tree");
    	NTreeNode<String,Integer> nodeA1_null = tree.n("A");
    	NTreeNode<String,Integer> nodeA2_null = tree.n("A");
    	nodeA2_null.version = 2;
    	NTreeNode<String,Integer> nodeA_1 = tree.n("A",1);
    	NTreeNode<String,Integer> nodeB = tree.n("B");
    	
    	assertEquals(nodeA1_null, nodeA2_null);
    	assertNotEquals(nodeA1_null, nodeA_1);
    	assertNotEquals(nodeA1_null, nodeB);
    }
    
    @Test void test_toString_id_is_not_number() {
    	NTree<String,Integer> tree = TestUtil.testTree();
    	NTreeNode<String,Integer> b1 = tree.findFirstWithId("B1");
    	
    	String b1String = b1.toString();
    	b1String = b1String.replaceFirst("  \"uuid\": \"[0-9a-f-]{1,}\"", "  \"uuid\": x");
    	
    	String expected = "{\n"
    			+ "  \"id\": \"B1\",\n"
    			+ "  \"value\": 2,\n"
    			+ "  \"version\": 1,\n"
    			+ "  \"uuid\": x,\n"
    			+ "  \"treeOfBelonging_id\": \"t\",\n"
    			+ "  \"parent_id\": \"A1\",\n"
    			+ "  \"children_ids\": [\n"
    			+ "    \"C1\",\n"
    			+ "    \"C2\"\n"
    			+ "  ]\n"
    			+ "}";
    	System.out.println(expected);
    	assertEquals(expected, b1String);
    }
    
    @SuppressWarnings("unchecked")
	@Test void test_toString_id_is_number() {
    	NTree<Integer,String> tree = NTree.create(0);
    	tree.addNewRootSubtree(
    		tree.n(1,"A1").c(
    			tree.n(2,"B1").c(
    				tree.n(3,"C1"),
    				tree.n(4,"D1"))));
    	NTreeNode<Integer,String> b1 = tree.findFirstWithId(2);
    	
    	String b1String = b1.toString();
    	b1String = b1String.replaceFirst("  \"uuid\": \"[0-9a-f-]{1,}\"", "  \"uuid\": x");
    	
    	String expected = "{\n"
    			+ "  \"id\": 2,\n"
    			+ "  \"value\": \"B1\",\n"
    			+ "  \"version\": 1,\n"
    			+ "  \"uuid\": x,\n"
    			+ "  \"treeOfBelonging_id\": 0,\n"
    			+ "  \"parent_id\": 1,\n"
    			+ "  \"children_ids\": [\n"
    			+ "    3,\n"
    			+ "    4\n"
    			+ "  ]\n"
    			+ "}";
    	assertEquals(expected, b1String);
    }
}
