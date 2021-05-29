package veve.datastructures.trees;

import static veve.datastructures.trees.GeneralUtils.argsNotNull;
import static veve.datastructures.trees.GeneralUtils.cloneUsingSerialization;
import static veve.datastructures.trees.GeneralUtils.cloneWithCopyConstructor;
import static veve.datastructures.trees.GeneralUtils.safeBiPredicate;
import static veve.datastructures.trees.GeneralUtils.safeConsumer;
import static veve.datastructures.trees.GeneralUtils.safeFunction;
import static veve.datastructures.trees.GeneralUtils.safePredicate;
import static veve.datastructures.trees.GsonInstance.gsonDefault;
import static veve.datastructures.trees.GsonInstance.gsonForNodeToString;
import static veve.datastructures.trees.NTreeConstants.TreeTraversalOrder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import veve.datastructures.trees.NTreeConstants.NodeValueCloningMode;

/**
 * A tree node that has an id, a value and zero or more child nodes. Children of
 * a node cannot have the same id. The id in a {@code NTreeNode} cannot be 
 * {@code null} but the value can be {@code null}. The node is also required to
 * have a reference to a {@link NTree} which is the owner of the node but the node
 * is not necessarily part of the tree who is its owner.
 * <p>
 * A node can also be considered as the root of a subtree or node hierarchy even
 * if it does not not formally form part of an {@code NTree}.
 * <p>
 * All methods from {@code NTreeNode} that modify the node or subtree will update 
 * the indexes of the {@code NTree} that owns this node if the node is formally 
 * part of the trhat tree so the indexes are automatically kept in sync 
 * with the node changes.
 * <p>
 * The methods of this class that use functional interface arguments convert the
 * lambda function passed to another lambda function that prevents NullPointerException
 * from breaking the execution and instead make the lambda function return null
 * or false or not execute if it is a Consumer. If a NullPointerException is 
 * thrown for a Predicate or BiPredicate argument the result of executing the 
 * lambda function would be false. If the parameter is of type Function then the
 * result of the lambda function would be null.
 * <p>
 * Note that no method accepts null values as arguments except {@link NTreeNode#setValue(Object)}
 * <p>
 * The {@link NearestCommonAncestorTool} class can be used to find the nearest
 * common ancestor between two nodes in a tree plus having other methods to get 
 * all nodes between the path between them and the their common ancestor.
 * <p>
 * This class is not thread safe.
 * 
 * @author Eladio Rodriguez Veve
 * @param <K> The type of the nodes id. Must implement {@code Comparable}.
 * @param <V> The type of the {@code value} property of the node.
 */
public class NTreeNode<K extends Comparable<K>, V> implements Iterable<NTreeNode<K,V>> , Comparable<NTreeNode<K,V>> {
	
	K id;
	V value;
	long version = 1;
	@GsonIgnore Map<K,NTreeNode<K,V>> children = new HashMap<K,NTreeNode<K,V>>(); 
	transient NTreeNode<K,V> parent;
	transient NTree<K,V> treeOfBelonging;
	String uuid = UUID.randomUUID().toString();
	
	//==============================================================================================
	//	CONSTRUCTORS, GETTERS AND SETTERS
	//==============================================================================================
	
	NTreeNode(NTree<K,V> treeOfBelonging) {
		this.treeOfBelonging = treeOfBelonging;
	}
	
	/**
	 * Constructor that creates a {@code NTreeNode} with {@code null} value.
	 * 
	 * @param treeOfBelonging the {@link NTree} that owns this node
	 * @param id the id of this node
	 */
	public NTreeNode(NTree<K,V> treeOfBelonging, K id) {
		argsNotNull(treeOfBelonging, id);
		this.treeOfBelonging = treeOfBelonging;
		this.id = id;
	}
	
	/**
	 * Constructor that creates a {@code NTreeNode} with given value. This method
	 * does not accept a {@code null} for the value parameter. Use 
	 * {@link #NTreeNode(NTree, Comparable)} if you want the value of the new node
	 * to be null.
	 * 
	 * @param treeOfBelonging the {@link NTree} that owns this node
	 * @param id the id of this node
	 * @param value the value of this node. Cannot be {@code null}
	 */
	public NTreeNode(NTree<K,V> treeOfBelonging, K id, V value) {
		argsNotNull(treeOfBelonging, id, value);
		this.treeOfBelonging = treeOfBelonging;
		this.id = id;
		this.value = value;
	}
	
	/**
	 * Replaces the id of this node. 
	 * 
	 * @param newId the id that replaces the existing id
	 * @return {@code true} if it replaces the id, otherwise {@code false}.
	 * 			Reasons it does not replace the id are:<br>
	 * 			&#8226 The new id is equal to the existing id.<br>
	 * 			&#8226 The node has a sibling that already has that id
	 */
	public boolean replaceId(K newId) {
		argsNotNull(newId);
		if (this.id.equals(newId)) {
			return false;
		}
		if (isRoot() || this.parent == null) {
			this.id = newId;
			this.treeOfBelonging.putNodesInAllIndexes(this.nodeAndConnectedNodes());
			return true;
		}
		if (this.siblingsMap().containsKey(newId)) {
			return false;
		}
		this.parent.children.remove(this.id);
		this.id = newId;
		this.parent.children.put(this.id, this);
		this.treeOfBelonging.putNodesInAllIndexes(this.nodeAndConnectedNodes());
		return true;
	}
	
	/**
	 * Returns the id of this node.
	 * 
	 * @return the id of this node
	 */
	public K getId() {
		return this.id;
	}
	
	/**
	 * Sets the value property of this node.
	 * 
	 * @param value the value for this node's value property. It can be {@code null}.
	 */
	public NTreeNode<K,V> setValue(V value) {
		this.value = value;
		this.treeOfBelonging.putNodesInAllIndexes(this.nodeAndConnectedNodes());
		return this;
	}
	
	/**
	 * Returns the value of this node's value property.
	 * 
	 * @return the value of this node's value property
	 */
	public V getValue() {
		return this.value;
	}
	
	/**
	 * Returns the {@code NTreeNode} that is the parent of this node. Could be 
	 * {@code null}.
	 * 
	 * @return the {@code NTreeNode} that is the parent of this node
	 */
	public NTreeNode<K,V> getParent() {
		return this.parent;
	}
	
	/**
	 * Returns the string UUID of this node. Each node has a unique UUID which 
	 * is automatically generated when a node is instantiated or cloned. 
	 * Serialization and deserialization to and from JSON does not create a new 
	 * UUID.
	 * 
	 * @return the string UUID unique to this tree
	 */
	public String getUUID() {
		return this.uuid;
	}
	
	//==============================================================================================
	//	NODE MANIPULATION
	//==============================================================================================
	
	//----------------------------------------------------------------------------------------------
	//	REPLACEMENT
	//----------------------------------------------------------------------------------------------
	
	/**
	 * Replaces this node in a node hierarchy or tree with another node without
	 * replacing the node's descendants. The node passed will not be changed and
	 * instead a clone of it will be used.
	 * 
	 * @param other the node to replace this node
	 * @return this node if replaced or {@code null} if it did not replace this node.
	 * Reasons for not replacing this node can be one of the following:<br>
	 * &#8226 The node to replace this node is this node.<br>
	 * &#8226 This node is not a root of a {@link NTree} and it's parent is null.<br>
	 * &#8226 This node has a sibling node that has the same id as the replacement
	 * node.<br>
	 */
	public NTreeNode<K,V> replaceSingleNodeWith(NTreeNode<K,V> other)  {
		argsNotNull(other);
		if (other == this || isBastardNode()) {
			return null;
		}
		if (isRoot()) {
			NTreeNode<K,V> otherClone = other.cloneSingleNode(this.treeOfBelonging);
			this.children.forEach((id,child) -> {
				otherClone.children.put(id, child);
				child.parent = otherClone;
			});
			this.treeOfBelonging.root = otherClone;
			this.treeOfBelonging.removeNodeFromAllIndexes(this);
			this.treeOfBelonging.putNodeInAllIndexes(otherClone);
			this.treeOfBelonging.putNodesInAllIndexes(this.children.values());
			return this.nullRefs();
		}
		if (siblingsMap().containsKey(other.id)) {
			return null;
		}
		NTreeNode<K,V> otherClone = other.cloneSingleNode(this.treeOfBelonging);
		otherClone.parent = this.parent;
		this.children.forEach((id,child) -> {
			otherClone.children.put(id, child);
			child.parent = otherClone;
		});
		this.parent.children.remove(this.id);
		this.parent.children.put(otherClone.id, otherClone);
		this.treeOfBelonging.removeNodeFromAllIndexes(this);
		this.treeOfBelonging.putNodeInAllIndexes(this.parent);
		this.treeOfBelonging.putNodeInAllIndexes(otherClone);
		this.treeOfBelonging.putNodesInAllIndexes(this.children.values());
		return this.nullRefs();
	}
	
	/**
	 * Replaces this node and its descendants in a node hierarchy or tree with 
	 * another node and its descendants. The node passed will not be changed and
	 * instead a clone of it and its descendants will be used.
	 * 
	 * @param other the node to replace this node with its descendants.
	 * @return this node if replaced or {@code null} if not replaced. Reasons for not
	 * replacing this node can be one of the following:<br>
	 * &#8226 The node to replace this node is this node.<br>
	 * &#8226 This node is not a root of a {@link NTree} and it's parent is null.<br>
	 * &#8226 This node has a sibling node that has the same id as the replacement
	 * node.
	 */
	public NTreeNode<K,V> replaceWith(NTreeNode<K,V> other) {
		argsNotNull(other);
		if (other == this || isBastardNode()) {
			return null;
		}
		if (isRoot()) {
			this.treeOfBelonging.root = other.clone(this.treeOfBelonging);
			this.treeOfBelonging.root.parent = new NTreeNode<K,V>(this.treeOfBelonging);
			this.treeOfBelonging.recreateIndexes();
			this.parent = null;
			return this;
		}
		if (siblingsMap().containsKey(other.id)) {
			return null;
		}
		NTreeNode<K,V> clone = other.clone(this.treeOfBelonging);
		clone.parent = this.parent;
		this.parent.children.remove(this.id);
		this.parent.children.put(clone.id, clone);
		this.treeOfBelonging.removeNodesFromAllIndexes(toList());
		this.treeOfBelonging.putNodesInAllIndexes(clone.toList());
		this.treeOfBelonging.putNodeInAllIndexes(clone.parent);
		return this.nullRefsExceptChildren();
	}
	
	//----------------------------------------------------------------------------------------------
	//	REMOVAL
	//----------------------------------------------------------------------------------------------
	
	/**
	 * Removes this node and its descendants.
	 * 
	 * @return this node if removed or {@code null} if not removed.
	 * If this node's parent is null but this node is not a root of a {@link NTree}
	 * it will not be removed and {@code null} will be returned.<br>
	 */
	public NTreeNode<K,V> remove() {
		if (isBastardNode()) {
			return null;
		}
		if (isRoot()) {
			this.treeOfBelonging.clearTree();
			return this.nullRefsExceptChildren();
		}
		this.treeOfBelonging.removeNodesFromAllIndexes(toList());
		this.treeOfBelonging.putNodeInAllIndexes(this.parent);
		this.parent.children.remove(this.id);
		return this.nullRefsExceptChildren();
	}
	
	/**
	 * Removes this node from a tree or node hierarchy and it's parent adopts
	 * it's children (it's parent grandchildren).
	 * 
	 * @param bipredicate 	a {@code BiPredicate} that if evaluates to {@code true} then siblings
	 * 						of this node that have the same id as the adopted children are
	 * 						replaced by the children with the same id. If it evaluates to
	 * 						{@code false} then the children are removed and the uncle with the
	 * 						same id remains. The first argument of the {@code BiPredicate} is for
	 * 						the node's child and the second for the node's sibling.
	 * @return a list containing this node if it was removed plus any node that was
	 * replaced, if any or {@code null} if it was not removed
	 */
	public List<NTreeNode<K,V>> removeAndParentAdoptsGrandChildren(BiPredicate<NTreeNode<K,V>,NTreeNode<K,V>> bipredicate) {
		argsNotNull(bipredicate);
		if (isRoot() || isBastardNode()) {
			return null;
		}
		List<NTreeNode<K,V>> removed = new LinkedList<>(Arrays.asList(this));
		BiPredicate<NTreeNode<K,V>,NTreeNode<K,V>> safeBiPredicate = safeBiPredicate(bipredicate);
		this.children.forEach((id, child) -> {
			NTreeNode<K,V> childsUncleWithSameId = this.parent.children.get(id);
			// if child of this node has uncle with same id
			if (childsUncleWithSameId != null) {
				// if predicate returns true then replace uncle subtree
				if (safeBiPredicate.test(child, childsUncleWithSameId)) {
					removed.add(childsUncleWithSameId);
					this.parent.children.put(id, child);
					child.parent = this.parent;
					this.treeOfBelonging.removeNodeFromAllIndexes(childsUncleWithSameId);
					this.treeOfBelonging.putNodeInAllIndexes(child);
				}
				else {
					removed.add(child);
					child.parent = null;
					this.treeOfBelonging.removeNodeFromAllIndexes(child);
				}
			}
			else {
				this.parent.children.put(id, child);
				child.parent = this.parent;
				this.treeOfBelonging.putNodeInAllIndexes(child);
			}
		});
		this.parent.children.remove(this.id);
		this.treeOfBelonging.putNodeInAllIndexes(this.parent);
		this.treeOfBelonging.removeNodeFromAllIndexes(this);
		this.nullRefs();
		return removed;
	}
	
	/**
	 * Removes this node from a tree or node hierarchy and it's parent adopts
	 * it's children (it's parent grandchildren). If a child of this node has the
	 * same id as a sibling of this node, the child is removed.
	 * 
	 * @return a list containing the node that was removed plus any node that was
	 * replaced, if any or {@code null} if it was not removed 
	 */
	// A child of this node will not replace uncle node with same id. The child is discarded and uncle remains.
	public List<NTreeNode<K,V>> removeAndParentAdoptsGrandChildren() {
		return removeAndParentAdoptsGrandChildren((child, uncle) -> false);
	}
	
	/**
	 * Removes this node from a tree or node hierarchy and it's parent adopts
	 * it's children (it's parent grandchildren). If a child of this node has the
	 * same id as a sibling of node and this nodereplacesDuplicates argument
	 * is {@code true} the sibling is removed, but if replacesDuplicates argument
	 * is {@code false} the child is removed.
	 * 
	 * @return a list containing the node that was removed plus any node that was
	 * replaced, if any or {@code null} if it was not removed 
	 */
	// A child of this node will replace uncle node with same id if argument is true. The uncle is replaced.
	public List<NTreeNode<K,V>> removeAndParentAdoptsGrandChildren(boolean replacesDuplicates) {
		return removeAndParentAdoptsGrandChildren((child, uncle) -> replacesDuplicates);
	}
	
	//==============================================================================================
	//	CHILDREN
	//==============================================================================================
	
	//----------------------------------------------------------------------------------------------
	//	CHILDREN SETTERS
	//----------------------------------------------------------------------------------------------
	
	//TODO test completely down
	/**
	 * Adds new child nodes to this node. The nodes to be added as childs must
	 * have a parent that is {@code null} or they will not be added. If two  or
	 * more childs have the same id then only the first one will be added.
	 * 
	 * @param children 	a varargs of {@code NTreeNode} to be added as childs of 
	 * 					this node 
	 * @return this node
	 */
	@SuppressWarnings("unchecked")
	public NTreeNode<K,V> addNewChildren(NTreeNode<K,V>... children) {
		argsNotNull((Object) children);
		boolean isPartOfTree = isPartOfTree();
		int numAdded = 0;
		for (NTreeNode<K,V> child : children) {
			if (child.parent == null) {
				child.treeOfBelonging = this.treeOfBelonging;
				child.parent = this;
				NTreeNode<K,V> replaced = this.children.putIfAbsent(child.id, child);
				if (isPartOfTree && replaced == null) {
					numAdded++;
					this.treeOfBelonging.putNodeInAllIndexes(child);
				}
			}
		}
		if (isPartOfTree && numAdded > 0) {
			this.treeOfBelonging.putNodeInAllIndexes(this);
		}
		return this;
	}
	
	/**
	 * Same as {@link #addNewChildren(NTreeNode...)}.
	 */
	@SuppressWarnings("unchecked")
	public NTreeNode<K,V> c(NTreeNode<K,V>... children) {
		argsNotNull((Object) children);
		return addNewChildren(children);
	}
	
	/**
	 * Sets a node as a child of this tree. If this node has a child with the same
	 * id as the node passed then the child will be replaced. The passed node will
	 * be left unchanged and a clone of it and its descendants will be used instead.
	 * 
	 * @param node the node to set as a child of this node
	 * @return {@code null} if the node set as a child did not replace any 
	 * existing child, otherwise returns the replaced child
	 * @throws RuntimeException if trying to set already existing child
	 */
	public NTreeNode<K,V> setChild(NTreeNode<K,V> node) {
		argsNotNull(node);
		for (NTreeNode<K,V> child: this.childrenList()) {
			if (child == node) {
				throw new RuntimeException("Cannot set own child");
			}
		}
		NTreeNode<K,V> childToSet = node.clone(this.treeOfBelonging);
		childToSet.parent = this;
		if (this.children.containsKey(node.id)) {
			NTreeNode<K,V> replaced = this.children.put(node.id, childToSet);
			if (isPartOfTree()) {
				this.treeOfBelonging.removeNodesFromAllIndexes(replaced.toList());
				this.treeOfBelonging.putNodeInAllIndexes(this);
				this.treeOfBelonging.putNodesInAllIndexes(childToSet.toList());
			}
			return replaced.nullRefsExceptChildren();
		}
		this.children.put(node.id, childToSet);
		this.treeOfBelonging.putNodeInAllIndexes(this);
		this.treeOfBelonging.putNodesInAllIndexes(childToSet.toList());
		return null;
	}
	
	/**
	 * Sets a collection of nodes as a childs of this tree. If this node has a 
	 * child with the same id as one of the node passed then the child will be
	 * replaced. The passed nodes will be left unchanged and a clone of them and
	 * their descendants will be used instead.
	 * 
	 * @param nodes the nodes to set as a children of this node
	 * @return a {@code Map} of nodes that where replaced, where the keys are
	 * their ids
	 */
	public Map<K,NTreeNode<K,V>> setChildren(Collection<NTreeNode<K,V>> nodes) {
		argsNotNull(nodes);
		Map<K,NTreeNode<K,V>> replaced = new HashMap<>();
		Set<NTreeNode<K,V>> nodesSet = new HashSet<>(nodes);
		nodesSet.forEach(node -> {
			NTreeNode<K,V> replacedNode = setChild(node);
			if (replacedNode != null) {
				replaced.put(node.id, node);
			}
		});
		return replaced;
	}
	
	/**
	 * Same as {@link #setChildren(Collection)} but accepts a varargs argument
	 * instead of a {@code Collection} of nodes.
	 * 
	 * @param nodes a varargs of {@code NTreeNode} to set as children of this node
	 * @return a {@code Map} of nodes that where replaced, where the keys are
	 * their ids
	 */
	@SuppressWarnings("unchecked")
	public Map<K,NTreeNode<K,V>> setChildren(NTreeNode<K,V>... nodes) {
		return setChildren(Arrays.asList(nodes));
	}
	
	/**
	 * Similar to {@link #setChild(NTreeNode)} but will not replace an 
	 * existing child. 
	 * 
	 * @param node the node to set as a child of this node
	 * @return {@code true} if the child was set or {@code false} otherwise. 
	 * {@code false} means that this node had already a child with the same id
	 */
	public boolean setChildIfAbsent(NTreeNode<K,V> node) {
		argsNotNull(node);
		if (this.children.containsKey(node.id)) {
			return false;
		}
		else {
			NTreeNode<K,V> childToSet = node.clone(this.treeOfBelonging);
			childToSet.parent = this;
			this.children.put(childToSet.id, childToSet);
			if (isPartOfTree()) {
				this.treeOfBelonging.putNodeInAllIndexes(this);
				this.treeOfBelonging.putNodesInAllIndexes(childToSet.toList());
			}
		}
		return true;
	}
	
	/**
	 * Similar to {@link #setChildren(Collection)} but will not replace any 
	 * existing childs.
	 * 
	 * @param nodes a {@code Collection} of nodes to set as a children of this node
	 * @return {@code true} if it added all nodes passed as children
	 */
	public boolean setChildrenIfAbsent(Collection<NTreeNode<K,V>> nodes) {
		argsNotNull(nodes);
		List<Integer> duplicates = new LinkedList<>();
		Set<NTreeNode<K,V>> nodesSet = new HashSet<>(nodes);
		nodesSet.forEach(node -> {
			boolean wasAdded = setChildIfAbsent(node);
			if (!wasAdded) {
				duplicates.add(0);
			}
		});
		if (duplicates.size() > 0) {
			return false;
		}
		return true;
	}
	
	/**
	 * Same as {@link #setChildrenIfAbsent(Collection)} but accepts a 
	 * varargs argument instead of a {@code Collection} of nodes.
	 * 
	 * @param nodes a varargs of {@code NTreeNode} to set as children of this node
	 * @return {@code true} if it added all nodes passed as children
	 */
	@SuppressWarnings("unchecked")
	public boolean setChildrenIfAbsent(NTreeNode<K,V>...  nodes) {
		argsNotNull((Object) nodes);
		return setChildrenIfAbsent(Arrays.asList(nodes));
	}
	
	//----------------------------------------------------------------------------------------------
	//	CHILDREN GETTERS
	//----------------------------------------------------------------------------------------------
	
	/**
	 * Returns the child node that matches the passed id.
	 * 
	 * @param id the id of the child to return
	 * @return the child node that matches the provided id or {@code null} if this
	 * node does not have a child with that id
	 */
	public NTreeNode<K,V> childWithId(K id) {
		argsNotNull(id);
		return this.children.get(id);
	}
	
	/**
	 * Returns the first child it finds that a value that is equal to the one provided.
	 * 
	 * @param value the value of the child node to return
	 * @return the first child node that it finds that has a value equal to the
	 * one provided or {@code null} if no child has that value.
	 */
	//TODO test
	public NTreeNode<K,V> firstChildWithValue(V value) {
		argsNotNull(value);
		List<NTreeNode<K,V>> nodesWithValue = this.childrenList().stream().filter(
				safePredicate(node -> node.value.equals(value))
			).collect(Collectors.toList());
		return nodesWithValue.size() > 0 ? nodesWithValue.get(0) : null;
	}
	
	/**
	 * Returns a {@code List} of this node's children.
	 * 
	 * @return a {@code List} of this node's children
	 */
	public List<NTreeNode<K,V>> childrenList() {
		return new LinkedList<NTreeNode<K,V>>(this.children.values());
	}
	
	/**
	 * Returns a {@code List} of this node's children for which the provided 
	 * {@code Predicate} evaluates to {@code true}.
	 * 
	 * @param predicate the {@code Predicate} that is used to filter which childs
	 * 					to return
	 * @return a {@code List} of this node's children for which the provided 
	 * {@code Predicate} evaluates to {@code true}
	 */
	public List<NTreeNode<K,V>> childrenList(Predicate<NTreeNode<K,V>> predicate) {
		argsNotNull(predicate);
		return childrenList().stream().filter(safePredicate(predicate)).collect(Collectors.toList());
	}
	
	/**
	 * Returns a {@code List} of this node's children whose ids are included in
	 * the passed {@code Collection} of ids.
	 * 
	 * @param a {@code Collection} of ids used to filter which childs to return
	 * @return a {@code List} of this node's children whose ids are included in
	 * the passed {@code Collection} of ids
	 */
	public List<NTreeNode<K,V>> childrenList(Collection<K> ids) {
		argsNotNull(ids);
		Set<K> idsSet = new HashSet<>(ids);
		return childrenList(node -> idsSet.contains(node.id));
	}
	
	/**
	 * Similar to {@link #childrenList(Collection)} but has a varargs parameter
	 * instead of a {@code Collection} of nodes
	 * 
	 * @param ids the varargs of ids
	 * @return a {@code List} of this node's children whose ids are included in
	 * the passed varargs ids parameter
	 */
	@SuppressWarnings("unchecked")
	public List<NTreeNode<K,V>> childrenList(K... ids) {
		argsNotNull((Object) ids);
		return childrenList(Arrays.asList(ids));
	}
	
	/**
	 * Returns a {@code Map} of this node's children. The Map keys are the children's ids.
	 * 
	 * @return a {@code Map} of this node's children. The Map keys are the children's ids.
	 */
	public Map<K,NTreeNode<K,V>> childrenMap() {
		return new HashMap<K,NTreeNode<K,V>>(this.children);
	}
	
	/**
	 * Returns a {@code Map} of this node's children for which the provided 
	 * {@code Predicates} evaluates to {@code true}. The Map keys are the 
	 * children's ids.
	 * 
	 * @return a {@code Map} of this node's children for which the provided 
	 * {@code Predicates} evaluates to {@code true}. The Map keys are the 
	 * children's ids.
	 */
	public Map<K,NTreeNode<K,V>> childrenMap(Predicate<NTreeNode<K,V>> predicate) {
		argsNotNull(predicate);
		return childrenList().stream().filter(safePredicate(predicate)).collect(Collectors.toMap(node -> node.id, node -> node));
	}
	
	/**
	 * Returns a {@code Map} of this node's children that have ids that are in 
	 * the provided {@code Collection} of ids. The Map keys are the children's ids.
	 * 
	 * @return a {@code Map} of this node's children that have ids that are in 
	 * the provided {@code Collection} of ids. The Map keys are the children's ids.
	 */
	public Map<K,NTreeNode<K,V>> childrenMap(Collection<K> ids) {
		argsNotNull(ids);
		Set<K> idsSet = new HashSet<>(ids);
		return childrenMap(node -> idsSet.contains(node.id));
	}
	
	/**
	 * Similar to {@link #childrenMap(Collection)} but instead of having a 
	 * {@code Collection} as parameter it uses a varargs parameter.
	 * 
	 * @return a {@code Map} of this node's children that have ids that are in 
	 * the provided varargs of ids. The Map keys are the children's ids.
	 */
	@SuppressWarnings("unchecked")
	public Map<K,NTreeNode<K,V>> childrenMap(K... ids) {
		argsNotNull((Object) ids);
		return childrenMap(Arrays.asList(ids));
	}
	
	//----------------------------------------------------------------------------------------------
	//	CHILDREN REMOVAL
	//----------------------------------------------------------------------------------------------
	
	/**
	 * Removes a child of this node that has the provided id.
	 * 
	 * @param id the id of the child node to remove
	 * @return the removed node or {@code null} if this node has no child with 
	 * the provided id
	 */
	public NTreeNode<K,V> removeChild(K id) {
		argsNotNull(id);
		if (this.children.containsKey(id)) {
			if (isPartOfTree()) {
				this.treeOfBelonging.removeNodesFromAllIndexes(this.children.get(id).toList());
				this.treeOfBelonging.putNodeInAllIndexes(this);
			}
			return this.children.remove(id).nullRefsExceptChildren();
		}
		return null;
	}
	
	/**
	 * Removes the child nodes of this node for which the provided {@code Predicate}
	 * evaluates to {@code true}.
	 * 
	 * @param predicate the {@code Predicate} which if true will make a child node
	 * 					of this node be removed
	 * @return a {@code Map} of the child nodes removed. The keys of the map are
	 * the ids of the child nodes.
	 */
	public Map<K,NTreeNode<K,V>> removeChildren(Predicate<NTreeNode<K,V>> predicate) {
		argsNotNull(predicate);
		Map<K,NTreeNode<K,V>> removed = new HashMap<>();
		List<NTreeNode<K,V>> nodesToRemove = childrenList(predicate);
		nodesToRemove.forEach(node -> {
			NTreeNode<K,V> removedNode = removeChild(node.id);
			if (removedNode != null) {
				removed.put(node.id, node);
			}
		});
		return removed;
	}
	
	/**
	 * Removes the child nodes of this node that whose id are in the passed 
	 * {@code Collection} of ids.
	 * 
	 * @param ids a {@code Collection} of the ids of the child nodes to remove
	 * @return a {@code Map} of the child nodes removed. The keys of the map are
	 * the ids of the child nodes.
	 */
	public Map<K,NTreeNode<K,V>> removeChildren(Collection<K> ids) {
		argsNotNull(ids);
		Set<K> idsSet = new HashSet<>(ids);
		return removeChildren(node -> idsSet.contains(node.id));
	}
	
	/**
	 * Similar to {@link #removeChildren(Collection)} but uses a varargs
	 * parameter instead of a {@code Collection} to pass the child ids.
	 * 
	 * @param ids a vararg of the ids of the child nodes to remove
	 * @return a {@code Map} of the child nodes removed. The keys of the map are
	 * the ids of the child nodes.
	 */
	@SuppressWarnings("unchecked")
	public Map<K,NTreeNode<K,V>> removeChildren(K... ids) {
		argsNotNull((Object) ids);
		return removeChildren(Arrays.asList(ids));
	}
	
	/**
	 * Removes all of this node's child nodes.
	 * 
	 * @return a {@code Map} of the child nodes removed. The keys of the map are
	 * the ids of the child nodes.
	 */
	public Map<K, NTreeNode<K, V>> removeAllChildren() {
		return removeChildren(childrenIds());
	}
	
	/**
	 * Removes all of this node's children for which the provided {@code Predicate}
	 * evaluates to {@code false} and retaining those childs for which it evaluates to
	 * {@code true}.
	 * 
	 * @param predicate the {@code Predicate} which when evaluates to {@code false}
	 * 					makes the child be removed.
	 * @return a {@code Map} of the child nodes removed. The keys of the map are
	 * the ids of the child nodes.
	 */
	public Map<K,NTreeNode<K,V>> retainChild(Predicate<NTreeNode<K,V>> predicate) {
		argsNotNull(predicate);
		Map<K,NTreeNode<K,V>> nodesToRemove = childrenList().stream()
				.filter(Predicate.not(safePredicate(predicate))).collect(Collectors.toMap(node -> node.id, node -> node));
		nodesToRemove.forEach((id, node) -> removeChild(node.id));
		return nodesToRemove;
	}
	
	/**
	 * Removes all of this node's children whose id is not in the provided 
	 * {@code Collection} of ids and retains those whose id is in the collection.
	 * 
	 * @param ids a {@code Collection} of ids of the childs to be removed.
	 * @return a {@code Map} of the child nodes removed. The keys of the map are
	 * the ids of the child nodes.
	 */
	public Map<K, NTreeNode<K, V>> retainChildren(Collection<K> ids) {
		argsNotNull(ids);
		Set<K> idsSet = new HashSet<>(ids);
		return retainChild(node -> idsSet.contains(node.id));
	}
	
	/**
	 * Similar to {@link #retainChildren(Collection)} but has a varargs parameter
	 * to pass the child ids.
	 * 
	 * @param ids a varargs of ids of the child nodes to retain
	 * @return a {@code Map} of the child nodes removed. The keys of the map are
	 * the ids of the child nodes.
	 */
	@SuppressWarnings("unchecked")
	public Map<K, NTreeNode<K, V>> retainChildren(K... ids) {
		argsNotNull((Object) ids);
		return retainChildren(Arrays.asList(ids));
	}
	
	//----------------------------------------------------------------------------------------------
	//	CHILDREN CONVENIENCE
	//----------------------------------------------------------------------------------------------
	
	/**
	 * Returns the number of children this node has.
	 * 
	 * @return the number of children this node has
	 */
	public int childrenSize() {
		return this.children.size();
	}
	
	/**
	 * Returns a {@code List} of the ids of this node's children.
	 * 
	 * @return a {@code List} of the ids of this node's children
	 */
	public List<K> childrenIds() {
		return mapChildrenToList(child -> child.id);
	}
	
	/**
	 * Returns a {@code List} of the values of this node's children.
	 * 
	 * @return a {@code List} of the values of this node's children
	 */
	public List<V> childrenValues() {
		return mapChildrenToList(child -> child.value);
	}
	
	/**
	 * Returns a {@code List} of the results of applying the provided {@code Function}
	 * to each of this node's children.
	 * 
	 * @param <R> the return type of the passed function. Java can infer it.
	 * @param function 	the {@code Function} whose result of being applied to 
	 * 					each child node is an item in the returned {@code List}
	 * @return a {@code List} of the results of applying the mapping {@code Function}
	 * to each of this node's children
	 */
	public <R> List<R> mapChildrenToList(Function<NTreeNode<K,V>,R> function) {
		argsNotNull(function);
		return childrenList().stream().map(safeFunction(function)).collect(Collectors.toList());
	}
	
	/**
	 * Returns a {@code Map} of the results of applying the provided {@code Function}
	 * to each of this node's children. The map keys are the children ids.
	 * 
	 * @param <R> the return type of the passed function. Java can infer it.
	 * @param function 	the {@code Function} whose result of being applied to 
	 * 					each child node is a value of an entry in the returned {@code Map}
	 * @return a {@code Map} of the results of applying the mapping {@code Function}
	 * to each of this node's children. The keys of the map are the child ids and
	 * the values are the result of the mapping function.
	 */
	public <R> Map<K,R> mapChildrenToMap(Function<NTreeNode<K,V>,R> function) {
		argsNotNull(function);
		return childrenList().stream().collect(Collectors.toMap(node -> node.id, node -> safeFunction(function).apply(node)));
	}
	
	/**
	 * Returns a {@code Map} of where the keys are the ids of this node's children 
	 * and the values are their values.
	 * 
	 * @return a {@code Map} of where the keys are the ids of this node's children 
	 * and the values are their values
	 */
	public Map<K,V> childrenIdsValuesMap() {
		return childrenList().stream().collect(Collectors.toMap(node -> node.id, node -> node.value));
	}
	
	//==============================================================================================
	//	SIBLINGS
	//==============================================================================================
	
	/**
	 * Returns a {@code Map} of this node's siblings. The keys of the map are the
	 * siblings ids. If this node is a root or it's parent is {@code null} then 
	 * {@code null} is returned.
	 * 
	 * @return a {@code Map} of this node's siblings. The keys of the map are the
	 * siblings ids. If this node is a root or it's parent is {@code null} then 
	 * {@code null} is returned.
	 */
	public Map<K,NTreeNode<K,V>> siblingsMap() {
		if (this.parent == null) {
			return null;
		}
		K id = this.id;
		return this.parent.children.entrySet().stream()
			.filter(entry -> !entry.getKey().equals(id))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
	
	/**
	 * Returns a {@code List} of this node's siblings.
	 * 
	 * @return a {@code List} of this node's siblings.
	 */
	public List<NTreeNode<K,V>> siblingsList() {
		return new LinkedList<NTreeNode<K,V>>(siblingsMap().values());
	}
	
	//==============================================================================================
	//	CLONING
	//==============================================================================================
	
	NTreeNode<K,V> cloneSingleNode(NTreeNode<K,V> node, NTree<K,V> treeOfBelonging) {
		argsNotNull(node, treeOfBelonging);
		V clonedValue = null;
		if (this.treeOfBelonging.getNodeValueCloningMode() == null) {
			clonedValue = cloneUsingSerialization(node.value);
		}
		else if (this.treeOfBelonging.getNodeValueCloningMode() == NodeValueCloningMode.BY_COPY_CONSTRUCTOR) {
			clonedValue = cloneWithCopyConstructor(node.value);
		}
		else if (this.treeOfBelonging.getNodeValueCloningMode() == NodeValueCloningMode.BY_SERIALIZATION && this.treeOfBelonging.getNodeValueType() == null) {
			clonedValue = cloneUsingSerialization(node.value);
		}
		else if (this.treeOfBelonging.getNodeValueCloningMode() == NodeValueCloningMode.BY_SERIALIZATION && this.treeOfBelonging.getNodeValueType() != null) {
			clonedValue = cloneUsingSerialization(node.value, node.treeOfBelonging.getNodeValueType());
		}
		if (clonedValue == null) {
			return new NTreeNode<K,V>(treeOfBelonging, node.id);
		}
		NTreeNode<K,V> clone = new NTreeNode<K,V>(treeOfBelonging, node.id, clonedValue);
		clone.version = node.version;
		return clone;
	}
	
	/**
	 * Returns a clone of this node and sets its treeOfBelonging to the one provided.
	 * By default the node's value id copied using simple serialization. This can
	 * be changed if the tree is configured with {@link NTree#nodeValueCloningUsesCopyConstructor()}
	 * or {@link NTree#nodeValueCloningUsesSerialization(Type)}.<br>
	 * The returned clone does not have any parent or children.
	 * 
	 * @param treeOfBelonging the tree that owns the returned clone
	 * @return a clone of this node without any parent or children
	 */
	public NTreeNode<K,V> cloneSingleNode(NTree<K,V> treeOfBelonging) {
		argsNotNull(treeOfBelonging);
		return cloneSingleNode(this, treeOfBelonging);
	}
	
	/**
	 * Returns a clone of this node and sets its treeOfBelonging to the one this
	 * node has. By default the node's value id copied using simple serialization. 
	 * This can be changed if the tree is configured with 
	 * {@link NTree#nodeValueCloningUsesCopyConstructor()}
	 * or {@link NTree#nodeValueCloningUsesSerialization(Type)}.<br>
	 * The returned clone does not have any parent or children.
	 * 
	 * @param treeOfBelonging the tree that owns the returned clone
	 * @return a clone of this node without any parent or children
	 */
	public NTreeNode<K,V> cloneSingleNode() {
		return cloneSingleNode(this, this.treeOfBelonging);
	}
	
	void _clone(NTreeNode<K,V> node, NTreeNode<K,V> parent, NTree<K,V> treeOfBelonging) {
		NTreeNode<K,V> clonedNode = node.cloneSingleNode(treeOfBelonging);
		clonedNode.parent = parent;
		clonedNode.treeOfBelonging = treeOfBelonging;
		parent.children.put(clonedNode.id, clonedNode);
		node.children.forEach((id, child) -> _clone(child, clonedNode, treeOfBelonging));
	}
	
	/**
	 * Returns a clone of this node and its descendant. The parent of the 
	 * returned clone is {@code null}. The treeOfBelonging of the returned clone
	 * and its descendant will be set to the provided treeOfBelonging. The parent
	 * of the returned clone will be {@code null}.
	 * 
	 * @param treeOfBelonging the tree that owns the returned clone and its descendants
	 * @return a Returns a clone of this node and its descendant.
	 */
	public NTreeNode<K,V> clone(NTree<K,V> treeOfBelonging) {
		argsNotNull(treeOfBelonging);
		NTreeNode<K,V> tempParent = new NTreeNode<>(treeOfBelonging, this.id);
		_clone(this, tempParent, treeOfBelonging);
		NTreeNode<K,V> clone = tempParent.children.get(this.id);
		clone.parent = null;
		return clone;
	}
	
	/**
	 * Returns a clone of this node and its descendant. The parent of the 
	 * returned clone is {@code null}. The treeOfBelonging of the returned clone
	 * and its descendant will be the same as this node's treeOfBelonging. The 
	 * parent of the returned clone will be {@code null}.
	 * 
	 * @return a Returns a clone of this node and its descendant.
	 */
	public NTreeNode<K,V> clone() {
		return clone(this.treeOfBelonging);
	}
	
	//==============================================================================================
	//	TRAVERSAL
	//==============================================================================================
	
	void _preOrderAction(NTreeNode<K,V> node, Consumer<NTreeNode<K,V>> action) {
		action.accept(node);
		List<NTreeNode<K,V>> orderedNodeList = null;
		if (this.treeOfBelonging.isUnordered()) {
			node.children.forEach((id,child) -> _preOrderAction(child, action));
		}
		else if (this.treeOfBelonging.isNaturalOrdered()) {
			orderedNodeList = new LinkedList<>(node.children.values());
			Collections.sort(orderedNodeList);
			orderedNodeList.forEach(child -> _preOrderAction(child, action));
		}
		else if (this.treeOfBelonging.isCustomOrdered()) {
			orderedNodeList = new LinkedList<>(node.children.values());
			orderedNodeList.sort(this.treeOfBelonging.nodeComparator);
			orderedNodeList.forEach(child -> _preOrderAction(child, action));
		}
	}
	
	void _preOrderAction(Consumer<NTreeNode<K,V>> action) {
		_preOrderAction(this, action);
	}
	
	/**
	 * Does an action for this node and each of its descendants traversing the 
	 * nodes in a preorder manner.
	 * 
	 * @param action 	the {@code Consumer} that will execute an action for this 
	 * 					node and each of its descendants
	 */
	public void forEachPreOrder(Consumer<NTreeNode<K,V>> action) {
		argsNotNull(action);
		List<NTreeNode<K,V>> nodeList = new LinkedList<>();
		_preOrderAction(node -> nodeList.add(node));
		nodeList.forEach(safeConsumer(action));
		this.treeOfBelonging.recreateIndexes(); 
	}
	
	void _postorderAction(NTreeNode<K,V> node, Consumer<NTreeNode<K,V>> action) {
		List<NTreeNode<K,V>> orderedNodeList = null;
		if (this.treeOfBelonging.isUnordered()) {
			node.children.forEach((id,child) -> _postorderAction(child, action));
		}
		else if (this.treeOfBelonging.isNaturalOrdered()) {
			orderedNodeList = new LinkedList<>(node.children.values());
			Collections.sort(orderedNodeList);
			orderedNodeList.forEach(child -> _postorderAction(child, action));
		}
		else if (this.treeOfBelonging.isCustomOrdered()) {
			orderedNodeList = new LinkedList<>(node.children.values());
			orderedNodeList.sort(this.treeOfBelonging.nodeComparator);
			orderedNodeList.forEach(child -> _postorderAction(child, action));
		}
		action.accept(node);
	}
	
	void _postOrderAction(Consumer<NTreeNode<K,V>> action) {
		_postorderAction(this, action);
	}
	
	/**
	 * Performs an action for this node and each of its descendants traversing the 
	 * nodes in a postorder manner.
	 * 
	 * @param action 	the {@code Consumer} that will execute an action for this 
	 * 					node and each of its descendants
	 */
	public void forEachPostOrder(Consumer<NTreeNode<K,V>> action) {
		List<NTreeNode<K,V>> nodeList = new LinkedList<>();
		_postOrderAction(node -> nodeList.add(node));
		nodeList.forEach(safeConsumer(action));
		this.treeOfBelonging.recreateIndexes();
	}
	
	void _specificLevelOrderAction(NTreeNode<K,V> node, int level, Consumer<NTreeNode<K,V>> action) {
		if (level == 1) {
			action.accept(node);
		}
		else if (level > 1) {
			List<NTreeNode<K,V>> orderedNodeList = null;
			if (this.treeOfBelonging.isUnordered()) {
				node.children.forEach((id, child) -> _specificLevelOrderAction(child, level-1, action));
			}
			else if (this.treeOfBelonging.isNaturalOrdered()) {
				orderedNodeList = new LinkedList<>(node.children.values());
				Collections.sort(orderedNodeList);
				orderedNodeList.forEach(child -> _specificLevelOrderAction(child, level-1, action));
			}
			else if (this.treeOfBelonging.isCustomOrdered()) {
				orderedNodeList = new LinkedList<>(node.children.values());
				orderedNodeList.sort(this.treeOfBelonging.nodeComparator);
				orderedNodeList.forEach(child -> _specificLevelOrderAction(child, level-1, action));
			}
		}
	}
	
	// For specific level only
	void _specificLevelOrderAction(int level, Consumer<NTreeNode<K,V>> action) {
		argsNotNull(action);
		_specificLevelOrderAction(this, level, action);
	}
	
	/**
	 * Performs an action on each node of the specified level relative to this node.
	 * 
	 * @param level the level relative to this node. This node is level 1, its
	 * 				children are level 2 and so on.
	 * @param action 	the {@code Consumer} that will execute an action for the 
	 * 					nodes at the specified level relative to this node
	 * @throws RuntimeException if the provided level is less than 1
	 */
	public void forEachOfLevel(int level, Consumer<NTreeNode<K,V>> action) {
		argsNotNull(action);
		if (level < 1) {
			throw new RuntimeException("level cannot be less than 1");
		}
		List<NTreeNode<K,V>> nodeList = new LinkedList<>();
		_specificLevelOrderAction(level, node -> nodeList.add(node));
		nodeList.forEach(safeConsumer(action));
		this.treeOfBelonging.recreateIndexes();
	}
	
	void _levelOrderAction(NTreeNode<K,V> node, Consumer<NTreeNode<K,V>> action) {
		Integer height = node.height();
		for (int level = 1;level <= height; level++) {
			_specificLevelOrderAction(node, level, action);
		}
	}
	
	//For all levels
	void _levelOrderAction(Consumer<NTreeNode<K,V>> action) {
		argsNotNull(action);
		_levelOrderAction(this, action);
	}
	
	/**
	 * Performs an action for this node and each of its descendants traversing the 
	 * nodes in a levelorder manner, meaning when traversing the nodes visit the 
	 * node in level 1 and then the nodes in level 2 and so on.
	 * 
	 * @param action 	the {@code Consumer} that will execute an action for this 
	 * 					node and each of its descendants
	 */
	public void forEachLevelOrder(Consumer<NTreeNode<K,V>> action) {
		argsNotNull(action);
		List<NTreeNode<K,V>> nodeList = new LinkedList<>();
		_levelOrderAction(node -> nodeList.add(node));
		nodeList.forEach(safeConsumer(action));
		this.treeOfBelonging.recreateIndexes();
	}
	
	void _levelOrderActionFromBottom(NTreeNode<K,V> node, Consumer<NTreeNode<K,V>> action) {
		Integer height = node.height();
		for (int level = height;level >= 1; level--) {
			_specificLevelOrderAction(node, level, action);
		}
	}
	
	//For all levels
	void _levelOrderActionFromBottom(Consumer<NTreeNode<K,V>> action) {
		argsNotNull(action);
		_levelOrderActionFromBottom(this, action);
	}
	
	/**
	 * Performs an action for this node and each of its descendants traversing the 
	 * nodes in a levelorder manner but starting from the deepest level and then
	 * traversing up the levels, meaning when traversing the nodes visit the 
	 * nodes in the deepest level N and then the nodes in level N-1 and so on.
	 * 
	 * @param action 	the {@code Consumer} that will execute an action for this
	 * 					node and each of its its descendants
	 */
	//For all levels
	public void forEachLevelOrderFromBottom(Consumer<NTreeNode<K,V>> action) {
		argsNotNull(action);
		List<NTreeNode<K,V>> nodeList = new LinkedList<>();
		_levelOrderActionFromBottom(node -> nodeList.add(node));
		nodeList.forEach(safeConsumer(action));
		this.treeOfBelonging.recreateIndexes();
	}
	
	/**
	 * Performs an action for this node and each of its descendants traversing the 
	 * nodes in the specified traversal order.
	 * 
	 * @param traversal the {@link TreeTraversalOrder} enum that determines the
	 * 					traversal order.
	 * @param action 	the {@code Consumer} that will execute an action for this
	 * 					node and its each of its descendants
	 */
	public void forEachNode(TreeTraversalOrder traversal, Consumer<NTreeNode<K,V>> action) {
		argsNotNull(traversal, action);
		Consumer<NTreeNode<K,V>> safeConsumer = safeConsumer(action);
		if (traversal == TreeTraversalOrder.PRE_ORDER) {
			forEachPreOrder(safeConsumer);
		}
		else if (traversal == TreeTraversalOrder.POST_ORDER) {
			forEachPostOrder(safeConsumer);
		}
		else if (traversal == TreeTraversalOrder.LEVEL_ORDER) {
			forEachLevelOrder(safeConsumer);
		}
		else if (traversal == TreeTraversalOrder.LEVEL_ORDER_FROM_BOTTOM) {
			forEachLevelOrderFromBottom(safeConsumer);
		}
		else {
			throw new IllegalArgumentException("Unrecognized TreeTraversalType passed to onEachNode(TreeTraversalType traversal, Consumer<NTreeNode<K,V>> action)");
		}
	}
	
	void forEachNodeUnsafeAndDoNotUpdateIndex(TreeTraversalOrder traversal, Consumer<NTreeNode<K,V>> action) {
		argsNotNull(traversal, action);
		if (traversal == TreeTraversalOrder.PRE_ORDER) {
			_preOrderAction(action);
		}
		else if (traversal == TreeTraversalOrder.POST_ORDER) {
			_postOrderAction(action);
		}
		else if (traversal == TreeTraversalOrder.LEVEL_ORDER) {
			_levelOrderAction(action);
		}
		else if (traversal == TreeTraversalOrder.LEVEL_ORDER_FROM_BOTTOM) {
			_levelOrderActionFromBottom(action);
		}
		else {
			throw new IllegalArgumentException();
		}
	}
	
	//==============================================================================================
	//	DERIVED PROPERTIES
	//==============================================================================================

	Integer _height(NTreeNode<K,V> node, int totalHeight) {
		if (node.children.isEmpty()) {
			return totalHeight + 1;
		}
		SortedSet<Integer> heights = new TreeSet<>();
		node.children.forEach((id,child) -> heights.add(_height(child, totalHeight)));
		return heights.last() + 1;
	}
	
	/**
	 * Returns the height of this subtree meaning the number of nodes from this 
	 * node down the path to the deepest node.
	 * 
	 * @return the height of this subtree
	 */
	public Integer height() {
		return _height(this, 0);
	}
	
	/**
	 * Returns the number of nodes in this subtree, which includes this node and
	 * all of its descendants.
	 * 
	 * @return the number of nodes in this subtree, which includes this node and
	 * all of its descendants
	 */
	public int size() {
		List<Integer> list = new ArrayList<>();
		_preOrderAction(node -> list.add(0));
		return list.size();
	}
	
	/**
	 * Returns {@code true} if this node is the root of it's treeOfBelonging.
	 * 
	 * @return {@code true} if this node is the root of it's treeOfBelonging
	 */
	public boolean isRoot() {
		return this == this.treeOfBelonging.root;
	}
	
	/**
	 * Returns {@code true} if this node's parent is {@code null} and it's not
	 * the root of it's treeOfBelonging.
	 * 
	 * @return {@code true} if this node's parent is {@code null} and it's not
	 * the root of it's treeOfBelonging
	 */
	public boolean isBastardNode() {
		return !isRoot() && this.parent == null;
	}
	
	/**
	 * Returns the level of this node relative to the root of the tree or node
	 * hierarchy it belongs to.
	 * 
	 * @return the level of this node relative to the root of the tree or node
	 * hierarchy it belongs to
	 */
	public int levelFromRoot() {
		int level = 0;
		NTreeNode<K,V> curr = this;
		while(curr != null) {
			level++;
			curr = curr.parent;
		}
		return level;
	}
	
	/**
	 * Returns the level of this node relative to the provided ancestor or -1 if
	 * this node does not have that ancestor.
	 * 
	 * @return the level of this node relative to the provided ancestor or -1 if
	 * this node does not have that ancestor
	 */
	public int levelRelativeToAncestor(NTreeNode<K,V> ancestor) {
		argsNotNull(ancestor);
		int level = 1;
		NTreeNode<K,V> curr = this;
		while(curr != ancestor) {
			if (curr.parent == null && curr != ancestor) {
				return -1;
			}
			curr = curr.parent;
			level++;
		}
		return level;
	}
	
	/**
	 * Returns {@code true} if this node is part of a {@link NTree}. All {@code NTreeNode}s
	 * have a {@code  NTree} that is their owner but the node is not necessarily
	 * a node of that tree. To be part of a tree a node must be the root node of a
	 * {@code NTree} or a descendant of the root. Root meaning here the {@code NTree.root}
	 * property of a {@code NTree}.
	 * 
	 * @return {@code true} if this node is part of a {@link NTree}
	 */
	boolean isPartOfTree() {
		NTreeNode<K,V> origin = this;
		while(origin.parent != null) {
			origin = origin.parent;
		}
		if (origin.isRoot()) {
			return true;
		}
		return false;
	}
	
	//==============================================================================================
	//	CONVENIENCE
	//==============================================================================================
	
	/**
	 * Returns a {@code Map} of {@code List}s of this node and its descendants.
	 * The map keys are the node ids and the values are lists of nodes that have
	 * the same id.
	 * 
	 * @return a {@code Map} of {@code List}s of this node and its descendants.
	 * The map keys are the node ids and the values are lists of nodes that have
	 * the same id.
	 */
	public Map<K,List<NTreeNode<K,V>>> toMapOfLists() {
		Map<K,List<NTreeNode<K,V>>> nodesMap = new HashMap<>();
		forEachNodeUnsafeAndDoNotUpdateIndex(TreeTraversalOrder.PRE_ORDER, node -> nodesMap.computeIfAbsent(node.id, n -> new LinkedList<NTreeNode<K,V>>()).add(node));
		return nodesMap;
	}
	
	/**
	 * Returns a {@code List} of this node and its descendants.
	 * 
	 * @param traversal the {@link TreeTraversalOrder} enum that determines the
	 * 					traversal order used to traverse the nodes and create the list
	 * @return a {@code List} of this node and its descendants
	 */
	public List<NTreeNode<K,V>> toList(TreeTraversalOrder traversal) {
		argsNotNull(traversal);
		LinkedList<NTreeNode<K,V>> nodeList = new LinkedList<>();
		forEachNodeUnsafeAndDoNotUpdateIndex(traversal, node -> nodeList.add(node));
		return nodeList;
	}
	
	/**
	 * Similar to {@link #toList(TreeTraversalOrder)} but has a predefined traversal
	 * order of preorder.
	 * 
	 * @return a {@code List} of this node and its descendants. 
	 */
	public List<NTreeNode<K,V>> toList() {
		return toList(TreeTraversalOrder.PRE_ORDER);
	}
	
	/**
	 * Returns a {@code List} of nodes that include this node, its parent if it
	 * not null and its children.
	 * 
	 * @return a {@code List} of nodes that include this node, its parent if it
	 * not null and its children
	 * 
	 */
	public List<NTreeNode<K,V>> nodeAndConnectedNodes() {
		List<NTreeNode<K,V>> nodeList = new ArrayList<>();
		nodeList.add(this);
		if (this.parent != null) {
			nodeList.add(this.parent);
		}
		nodeList.addAll(this.children.values());
		return nodeList;
	}
	
	/**
	 * Returns the farthest ancestor node of this node or {@code null} if this
	 * node's parent is {@code null}.
	 * 
	 * @return the farthest ancestor node of this node or {@code null} if this
	 * node's parent is {@code null}
	 */
	public NTreeNode<K,V> farthestAncestor() {
		if (this.parent == null) {
			return null;
		}
		NTreeNode<K,V> ancestor = this.parent;
		NTreeNode<K,V> ancestorParent = ancestor.parent;
		while(ancestorParent != null) {
			ancestor = ancestor.parent;
			ancestorParent = ancestorParent.parent;
		}
		return ancestor;
	}
	
	/**
	 * Returns {@code true} if the passed node is an ancestor of this node.
	 * 
	 * @param ancestor the node to check if it is an ancestor of this node
	 * @return {@code true} if the passed node is an ancestor of this node
	 */
	public boolean hasAncestor(NTreeNode<K,V> ancestor) {
		argsNotNull(ancestor);
		if (this.parent == null) {
			return false;
		}
		NTreeNode<K,V> currAncestor = this.parent;
		while(currAncestor != null) {
			if (currAncestor == ancestor) {
				return true;
			}
			currAncestor = currAncestor.parent;
		}
		return false;
	}
	
	/**
	 * Returns a {@code List} of this node and the nodes leading up to the
	 * ancestor plus the ancestor itself or {@code null} if the passed node is not
	 * an ancestor of this node.
	 * 
	 * @param ancestor the node that is an ancestor of this node
	 * @return a {@code List} of this node and the nodes leading up to the
	 * ancestor plus the ancestor itself or {@code null} if the passed node is not
	 * an ancestor of this node
	 */
	public List<NTreeNode<K,V>> nodesUpToAncestor(NTreeNode<K,V> ancestor) {
		argsNotNull(ancestor);
		if (!hasAncestor(ancestor)) {
			return null;
		}
		List<NTreeNode<K,V>> nodes = new LinkedList<>(Arrays.asList(this));
		NTreeNode<K,V> currAncestor = this.parent;
		while(currAncestor != ancestor.parent) {
			nodes.add(currAncestor);
			currAncestor = currAncestor.parent;
		}
		return nodes;
	}
	
	/**
	 * Returns the nodes in the level relative to this node. This node would be
	 * level 1, its children level 2 and so on.
	 * 
	 * @param level the level relative to this node to get the nodes from
	 * @return a {@code List} of the nodes at the specified level relative to this
	 * node
	 * @throws RuntimeException if the passed level is less than 1
	 */
	public List<NTreeNode<K,V>> nodesInLevel(int level) {
		if (level < 1) {
			throw new RuntimeException("level cannot be less than 1");
		}
		List<NTreeNode<K,V>> nodeList = new LinkedList<>();
		_specificLevelOrderAction(level, node -> nodeList.add(node));
		return nodeList;
	}
	
	/**
	 * Returns a {@code Stream} of nodes that includes this node and its descendants.
	 * 
	 * @return a {@code Stream} of nodes that includes this node and its descendants
	 */
	public Stream<NTreeNode<K,V>> stream() {
		return toList().stream();
	}
	
	/**
	 * Returns a {@code Stream} of nodes that includes this node and its descendants
	 * ordered based on the traversal argument.
	 * 
	 * @param traversal the {@link TreeTraversalOrder} enum that determines the
	 * 					order of the nodes in the stream
	 * @return a {@code Stream} of nodes that includes this node and its descendants
	 */
	public Stream<NTreeNode<K,V>> stream(TreeTraversalOrder traversal) {
		return toList(traversal).stream();
	}
	
	/**
	 * Returns a {@code Map} of the results of applying the provided function to 
	 * this node and its descendants. The list is ordered based on the preorder
	 * traversal of this node and its descendants. The keys of the map are the
	 * nodes ids and the values are the result of the passed function for each node.
	 * 
	 * @param <R> the return type of the passed function. Java can infer it.
	 * @param function 	the {@code Function} that is applied to this node and its 
	 * 					descendants and whose results are the values of the
	 * 					returned map
	 * @return a {@code List} 
	 */
	public <R> List<R> mapToList(Function<NTreeNode<K,V>,R> function) {
		argsNotNull(function);
		return this.stream().map(safeFunction(function)).collect(Collectors.toList());
	}
	
	/**
	 * Returns a {@code Map} of the results of applying the provided function to 
	 * this node and its descendants. The list is ordered based on the traversal 
	 * argument which determines the traversal of this node and its descendants. 
	 * The keys of the map are the nodes ids and the values are the result of
	 * the passed function for each node.
	 * 
	 * @param <R> the return type of the passed function. Java can infer it.
	 * @param function 	the {@code Function} that is applied to this node and its 
	 * 					descendants and whose results are the values of the
	 * 					returned map
	 * @return a {@code List} 
	 */
	public <R> List<R> mapToList(TreeTraversalOrder traversal, Function<NTreeNode<K,V>,R> function) {
		argsNotNull(traversal, function);
		return this.stream(traversal).map(safeFunction(function)).collect(Collectors.toList());
	}
	
	/**
	 * Returns all the nodes from the group comprising this node and its descendants
	 * for which the provided {@code Predicate} evaluates to true. The order of
	 * the list is determined by a preorder traversal of this node and its descendants.
	 * 
	 * @param predicate the {@code Predicate} used to filter this node and its
	 * descendants
	 * @return a {@code List} of nodes for which the passed predicate evaluates to true
	 */
	public List<NTreeNode<K,V>> findAll(Predicate<NTreeNode<K,V>> predicate) {
		argsNotNull(predicate);
		return this.stream().filter(safePredicate(predicate)).collect(Collectors.toList());
	}
	
	/**
	 * Returns all the nodes from the group comprising this node and its descendants
	 * for which the provided {@code Predicate} evaluates to true. The order of
	 * the list is determined by a the passed traversal.
	 * 
	 * @param traversal the {@link TreeTraversalOrder} that determines the order
	 * 					of the nodes in the returned list
	 * @param predicate the {@code Predicate} used to filter this node and its
	 * 					descendants
	 * @return a {@code List} of nodes for which the passed predicate evaluates to true
	 */
	public List<NTreeNode<K,V>> findAll(TreeTraversalOrder traversal, Predicate<NTreeNode<K,V>> predicate) {
		argsNotNull(traversal, predicate);
		return this.stream(traversal).filter(safePredicate(predicate)).collect(Collectors.toList());
	}
	
	void _findFirst(NTreeNode<K,V> node, List<NTreeNode<K,V>> list, Predicate<NTreeNode<K,V>> predicate) {
		if (predicate.test(node)) {
			list.add(node);
		}
		else {
			if (this.treeOfBelonging.isUnordered()) {
				Iterator<Map.Entry<K, NTreeNode<K,V>>> iterator = node.children.entrySet().iterator();
				while (iterator.hasNext() && list.size() == 0) {
					Map.Entry<K, NTreeNode<K,V>> entry = iterator.next();
					_findFirst(entry.getValue(), list, predicate);
				}
			} else if (this.treeOfBelonging.isNaturalOrdered()) {
				Comparator<Entry<K,NTreeNode<K,V>>> comparator = new Comparator<>() {
					@Override
					public int compare(Entry<K,NTreeNode<K,V>> a, Entry<K,NTreeNode<K,V>> b) {
						return a.getValue().compareTo(b.getValue());
					}
				};
				List<Entry<K,NTreeNode<K,V>>>  entryList = new LinkedList<>(node.children.entrySet());
				entryList.sort(comparator);
				Iterator<Entry<K,NTreeNode<K,V>>> iterator = entryList.iterator();
				while (iterator.hasNext() && list.size() == 0) {
					Map.Entry<K, NTreeNode<K,V>> entry = iterator.next();
					_findFirst(entry.getValue(), list, predicate);
				}
			} else if (this.treeOfBelonging.isCustomOrdered()) {
				NTree<K,V> treeOfBelonging = this.treeOfBelonging;
				Comparator<Entry<K,NTreeNode<K,V>>> comparator = new Comparator<>() {
					@Override
					public int compare(Entry<K,NTreeNode<K,V>> a, Entry<K,NTreeNode<K,V>> b) {
						return treeOfBelonging.nodeComparator.compare(a.getValue(), (b.getValue()));
					}
				};
				List<Entry<K,NTreeNode<K,V>>>  entryList = new LinkedList<>(node.children.entrySet());
				entryList.sort(comparator);
				Iterator<Entry<K,NTreeNode<K,V>>> iterator = entryList.iterator();
				while (iterator.hasNext() && list.size() == 0) {
					Map.Entry<K, NTreeNode<K,V>> entry = iterator.next();
					_findFirst(entry.getValue(), list, predicate);
				}
			}
		}
	}
	
	/**
	 * Returns the first node from the group of nodes comprised of this node and
	 * its descendants for which the passed {@code Predicate} returns true or 
	 * {@code null} if for no node the predicate is true. The search is done in
	 * a preorder manner.
	 * 
	 * @param predicate the {@code Predicate} that if true then returns the node
	 * 					for which it was evaluated
	 * @return the found node or {@code null} if not match was found
	 */
	public NTreeNode<K,V> findFirst(Predicate<NTreeNode<K,V>> predicate) {
		argsNotNull(predicate);
		Predicate<NTreeNode<K,V>> safePredicate = safePredicate(predicate);
		if (safePredicate.test(this)) {
			return this;
		}
		List<NTreeNode<K,V>> list = new ArrayList<>();
		_findFirst(this, list, safePredicate);
		if (list.size() == 0) {
			return null;
		}
		return list.get(0);
	}
	
	/**
	 * Returns the first node from the group comprising this node and its
	 * descendants that has the provided id.
	 * 
	 * @param id the id of the node to find
	 * @return the found node or {@code null} if not found
	 */
	public NTreeNode<K,V> findFirstWithId(K id) {
		argsNotNull(id);
		return findFirst(node -> node.id.equals(id));
	}
	
	/**
	 * Returns the first node from the group comprising this node and its
	 * descendants that has the provided value.
	 * 
	 * @param value the value of the node to find
	 * @return the found node or {@code null} if not found
	 */
	public NTreeNode<K,V> findFirstWithValue(V value) {
		argsNotNull(value);
		return findFirst(node -> node.value.equals(value));
	}
	
	void _equalsSubtree(NTreeNode<K,V> nodeA, NTreeNode<K,V> nodeB, MutableBoolean equal) {
		Map<K,NTreeNode<K,V>> nodeAChildren = nodeA.childrenMap();
		Map<K,NTreeNode<K,V>> nodeBChildren = nodeB.childrenMap();
		if (!nodeAChildren.entrySet().equals(nodeBChildren.entrySet())) {
			equal.setFalse();
			return;
		}
		for (Map.Entry<K,NTreeNode<K,V>> aEntry : nodeAChildren.entrySet()) {
			NTreeNode<K,V> bChild = nodeBChildren.get(aEntry.getKey());
			_equalsSubtree(aEntry.getValue(), bChild, equal);
		}
	}

	/**
	 * Returns {@code true} if this subtree is equal to the provided subtree.
	 * Two subtrees are equal if their node hierarchy is the same and the
	 * corresponding nodes in each hierarchy are equal.
	 * 
	 * @return {@code true} if this subtree is equal to the provided subtree
	 */
	public boolean equalsSubtree(NTreeNode<K,V> other) {
		argsNotNull(other);
		MutableBoolean equal = new MutableBoolean(true);
		if (!this.equals(other)) {
			return false;
		}
		_equalsSubtree(this, other, equal);
		return equal.getValue();
	}
	
	NTreeNode<K,V> nullRefs() {
		this.treeOfBelonging = null;
		this.parent = null;
		this.children = null;
		return this;
	}
	
	NTreeNode<K,V> nullRefsExceptChildren() {
		this.treeOfBelonging = null;
		this.parent = null;
		return this;
	}
	
	int subtreeHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id.hashCode();
		result = prime * result + toList().hashCode();
		return result;
	}
	
	//==============================================================================================
	//	SERIALIZATION
	//==============================================================================================
	
	/**
	 * Returns the JSON string representation of this node. This does not include
	 * its parent and treeOfBelonging.
	 * 
	 * @return the JSON string representation of this node. This does not include
	 * its parent and treeOfBelonging.
	 */
	public String toJson() {
		return gsonDefault.toJson(this);
	}
	
	/**
	 * Returns an instance of a {@code NTreeNode} from a JSON string.
	 * 
	 * @param <K> the type of the node id
	 * @param <V> the type of the node value
	 * @param json the JSON string representation of an {@code NTreeNode}
	 * @param treeOfBelonging the {@code NTree} that is to be the owner of the
	 * 							creaed node
	 * @param nodeValueType the type of the node values
	 * @return an {@code NTreeNode}
	 */
	public static <K extends Comparable<K>,V> NTreeNode<K,V> fromJson(String json, NTree<K,V> treeOfBelonging, Type nodeValueType) {
		argsNotNull(json, treeOfBelonging, nodeValueType);
		Type kClass = treeOfBelonging.getId().getClass();
		NTreeNode<K, V> deserializedNode = gsonDefault.fromJson(json, TypeToken.getParameterized(NTreeNode.class, kClass, nodeValueType).getType());
		reassignMissingReferences(deserializedNode, treeOfBelonging);
		return deserializedNode;
	}
	
	static <K extends Comparable<K>,V> void reassignMissingReferences(NTreeNode<K,V> node, NTree<K,V> treeOfBelonging) {
		node.children.forEach((id, child) -> {
			child.treeOfBelonging = treeOfBelonging;
			child.parent = node;
			reassignMissingReferences(child, treeOfBelonging);
		});
	}
	
	//==============================================================================================
	//	OVERRIDEN
	//==============================================================================================
	
	/**
	 * Returns an {@code Iterator} to iterates over the nodes of this node and its
	 * descendants in a preorder manner.
	 * 
	 * @return an {code Iterator} that can be used to iterate over the this node
	 * and its descendants in a preorder manner.
	 */
	@Override
	public Iterator<NTreeNode<K,V>> iterator() {
		return toList().iterator();
	}
	
	/**
	 * Returns an integer which based on weather it is negative, zero or positive
	 * determines if this node is less than, equal to or greater than the other 
	 * node.<br>
	 * Null values are considered less than to non null values and comparison
	 * between two nulls returns 0.<br>
	 * If {@code this.id.compareTo(other.id)} returns 0 (equal) and the class of
	 * the value property implements {@code Comparable} then the value properties
	 * are also compared.
	 * 
	 * @return an integer which based on weather it is negative, zero or positive
	 * determines if this node is less than, equal to or greater than the other 
	 * node.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public int compareTo(NTreeNode<K,V> other) {
		if (other == null) {
			return 1;
		} 
		int idComparison = this.id.compareTo(other.id);
		if (idComparison != 0) {
			return idComparison;
		}
		if (this.value == null && other.value != null) {
			return -1;
		}
		if (this.value == null && other.value == null) {
				return 0;
		}
		if (this.value != null && other.value == null) {
			return 1;
		}
		if (this.value instanceof Comparable) {
			return ((Comparable)this.value).compareTo(other.value);
		}
		return idComparison;
	}
	
	/**
	 * Returns the hashcode for this node. The hashcode is calculated using the
	 * id and value of this node.
	 * 
	 * @return an {@code int} that is the hashcode for this node
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id.hashCode();
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/**
	 * Returns {@code true} if this node is equal to the other node passed.
	 * Two nodes are considered equal if their ids are equal and their values are
	 * also equal.
	 * 
	 * @return {@code true} if this node's id is equal to the other node's id and
	 * this node's value is equal to the other node's value.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NTreeNode<K,V> other = (NTreeNode<K,V>) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	/**
	 * Returns a {@code String} representation of this node. The parent field is
	 * replaced by parent_id with a string representation of of the parent id. 
	 * The the children field is replaced by children_ids with a list representation
	 * of the children ids.
	 * 
	 * @return the {@code String} representation of this node
	 */
	@Override
	public String toString() {
		String json = gsonForNodeToString.toJson(this);
		JsonObject jsonObj = gsonForNodeToString.fromJson(json, JsonObject.class);
		if (this.id instanceof Number) {
			jsonObj.addProperty("treeOfBelonging_id", (Number) this.treeOfBelonging.getId());
			jsonObj.addProperty("parent_id", (Number) this.parent.id);
			jsonObj.add("children_ids", new JsonArray());
			JsonArray childrenJsonArray = jsonObj.get("children_ids").getAsJsonArray();
			this.children.forEach((id, child) -> childrenJsonArray.add((Number)id));
		}
		else {
			jsonObj.addProperty("treeOfBelonging_id", this.treeOfBelonging.getId().toString());
			jsonObj.addProperty("parent_id", this.parent.id.toString());
			jsonObj.add("children_ids", new JsonArray());
			JsonArray childrenJsonArray = jsonObj.get("children_ids").getAsJsonArray();
			this.children.forEach((id, child) -> childrenJsonArray.add(id.toString()));
		}
		return gsonForNodeToString.toJson(jsonObj);
	}
	
}
