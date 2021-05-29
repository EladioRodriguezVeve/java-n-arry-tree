package veve.datastructures.trees;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.gson.reflect.TypeToken;

import veve.datastructures.trees.NTreeConstants.NodeValueCloningMode;

public class NTreeNodeTest_OtherChildMethodsSiblingAndCloning {
	
	static final String IDS_INDEX = "idsIndex";
	
	@SuppressWarnings("unchecked")
	NTree<String,Integer> getTestTree() {
		NTree<String,Integer> tree = NTree.create("tree");
		return tree.addNewRoot(
			tree.n("A1").c(
				tree.n("B1",1),
				tree.n("B2",2),
				tree.n("B3",3),
				tree.n("B4",4)));
	}
	
	@Test void test_mapChildrenToList() {
		NTree<String,Integer> tree = getTestTree();
		
		List<String> ids = tree.root.mapChildrenToList(child -> child.getId());
		
		Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("B1","B2","B3","B4"));
		Multiset<String> childrenIds =  HashMultiset.create(ids);
		assertEquals(expectedIds, childrenIds);
	}
	
	@Test void test_mapChildrenToMap() {
		NTree<String,Integer> tree = getTestTree();
		
		Map<String,Integer> ids = tree.root.mapChildrenToMap(child -> child.getValue());
		
		Map<String,Integer> expectedIds =  new HashMap<>();
		expectedIds.put("B1",1);
		expectedIds.put("B2",2);
		expectedIds.put("B3",3);
		expectedIds.put("B4",4);
		assertEquals(expectedIds, ids);
	}
	
	@Test void test_getMapOfChildrenIdValues() {
		NTree<String,Integer> tree = getTestTree();
		
		Map<String,Integer> ids = tree.root.childrenIdsValuesMap();
		
		Map<String,Integer> expectedIds =  new HashMap<>();
		expectedIds.put("B1",1);
		expectedIds.put("B2",2);
		expectedIds.put("B3",3);
		expectedIds.put("B4",4);
		assertEquals(expectedIds, ids);
	}
	
	@Test void test_getMapOfSiblings_is_root() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.addNewRoot(tree.n("A1"));
		
		assertNull(tree.root.siblingsMap());
	}
	
	@Test void test_getMapOfSiblings() {
		NTree<String,Integer> tree = getTestTree();
		
		Map<String,NTreeNode<String,Integer>> siblingsMap = tree.root.childWithId("B1").siblingsMap();
		
		Map<String,NTreeNode<String,Integer>> expected = new HashMap<>();
		expected.put("B2", tree.n("B2",2));
		expected.put("B3", tree.n("B3",3));
		expected.put("B4", tree.n("B4",4));
		assertEquals(expected, siblingsMap);
	}
	
	//==============================================================================================
	//	CLONING
	//==============================================================================================
	
	@Test void test_clone_uses_serialization_for_value_by_default() {
		NTree<String,Integer> tree = NTree.create("tree");
		NTree<String,Integer> tree2 = NTree.create("tree2");
		NTreeNode<String,Integer> original = tree.n("A1", 1);
		
		NTreeNode<String,Integer> clone = original.cloneSingleNode(tree2);
		
		assertNull(tree.getNodeValueCloningMode());
		assertEquals(clone, original);
		assertEquals(1, clone.value);
		assertSame(tree2, clone.treeOfBelonging);
		assertNotSame(clone, original);
	}
	
	@Test void test_clone_uses_copy_constructor_for_value() {
		NTree<String,CloningTestClass<String>> tree = NTree.create("tree");
		tree.nodeValueCloningUsesCopyConstructor();
		NTree<String,CloningTestClass<String>> tree2 = NTree.create("tree2");
		NTreeNode<String,CloningTestClass<String>> original = tree.n("A1", new CloningTestClass<String>("Value"));
		
		NTreeNode<String,CloningTestClass<String>> clone = original.cloneSingleNode(tree2);
		
		assertEquals(NodeValueCloningMode.BY_COPY_CONSTRUCTOR, tree.getNodeValueCloningMode());
		assertEquals(clone, original);
		assertEquals("Value", clone.value.value);
		assertEquals(original.version, clone.version);
		assertSame(tree2, clone.treeOfBelonging);
		assertNotSame(clone, original);
	}
	
	@Test void test_clone_uses_copy_constructor_for_value_and_value_null() {
		NTree<String,CloningTestClass<String>> tree = NTree.create("tree");
		tree.nodeValueCloningUsesCopyConstructor();
		NTree<String,CloningTestClass<String>> tree2 = NTree.create("tree2");
		NTreeNode<String,CloningTestClass<String>> original = tree.n("A1");
		
		NTreeNode<String,CloningTestClass<String>> clone = original.cloneSingleNode(tree2);
		
		assertEquals(NodeValueCloningMode.BY_COPY_CONSTRUCTOR, tree.getNodeValueCloningMode());
		assertEquals(clone, original);
		assertNull(clone.value);
		assertSame(tree2, clone.treeOfBelonging);
		assertNotSame(clone, original);
	}
	
	@Test void test_clone_uses_serialization_for_value_by_configuration() {
		NTree<String,Integer> tree = NTree.create("tree");
		tree.nodeValueCloningUsesSerialization();
		NTree<String,Integer> tree2 = NTree.create("tree2");
		NTreeNode<String,Integer> original = tree.n("A1", 1);
		
		NTreeNode<String,Integer> clone = original.cloneSingleNode(tree2);
		
		assertEquals(NodeValueCloningMode.BY_SERIALIZATION, tree.getNodeValueCloningMode());
		assertNull(tree.getNodeValueType());
		assertEquals(clone, original);
		assertEquals(1, clone.value);
		assertSame(tree2, clone.treeOfBelonging);
		assertNotSame(clone, original);
	}
	
	@Test void test_clone_uses_serialization_with_type_for_value_by_configuration() {
		NTree<String,CloningTestClass<String>> tree = NTree.create("tree");
		Type valueType = new TypeToken<CloningTestClass<String>>(){}.getType();
		tree.nodeValueCloningUsesSerialization(valueType);
		NTree<String,CloningTestClass<String>> tree2 = NTree.create("tree2");
		NTreeNode<String,CloningTestClass<String>> original = tree.n("A1",  new CloningTestClass<>("Value"));
		
		NTreeNode<String,CloningTestClass<String>> clone = original.cloneSingleNode(tree2);
		
		assertEquals(NodeValueCloningMode.BY_SERIALIZATION, tree.getNodeValueCloningMode());
		assertEquals(valueType, tree.getNodeValueType());
		assertEquals(clone, original);
		assertEquals("Value", clone.value.value);
		assertSame(tree2, clone.treeOfBelonging);
		assertNotSame(clone, original);
	}
	
	@Test void test_cloneSubtree() {
		NTree<String,Integer> tree = TestUtil.testTree();
		NTree<String,Integer> tree2 = NTree.create("tree2");
		
		NTreeNode<String,Integer> clone = tree.root.clone(tree2);
		
		Multiset<String> expectedIds =  HashMultiset.create(Arrays.asList("A1","B1","B2","C1","C2"));
		Multiset<String> ids =  HashMultiset.create(clone.mapToList(node -> node.getId()));
		assertTrue(tree.root.equalsSubtree(clone));
		assertSame(tree2, clone.treeOfBelonging);
		assertNotSame(clone, tree.root);
		assertEquals(expectedIds, ids);
	}
	
}
