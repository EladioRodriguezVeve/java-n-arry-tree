
package veve.datastructures.trees;

import static veve.datastructures.trees.GeneralUtils.argsNotNull;
import static veve.datastructures.trees.GeneralUtils.safeCompareBiFunction;
import static veve.datastructures.trees.GeneralUtils.safeFunction;
import static veve.datastructures.trees.GsonInstance.gsonDefault;
import static veve.datastructures.trees.GsonInstance.gsonForTreeToString;
import static veve.datastructures.trees.NTreeConstants.NodeValueCloningMode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.reflect.TypeToken;

import veve.datastructures.trees.NTreeConstants.TreeTraversalOrder;

/**
 * A mutable tree composed of nodes where each node can have zero or any number 
 * of children. Each node  has an id property which is required, a value property 
 * which can be null and zero or more child nodes. Ids between  children of the 
 * same node must be unique between themselves. There can be nodes with the same 
 * ids in the tree as long as they are not siblings between themselves.
 * <p>
 * Nodes are instances of {@link NTreeNode} class. Each node can be considered 
 * a subtree having children which are subtrees themselves. Tree manipulation can
 * be done through the API of {@link NTree} or using the NTreeNode API for each 
 * node.
 * <p>
 * This tree is not ordered. Traversal order when it comes to determining
 * which child node to traverse first is not deterministic by default. This can 
 * be changed so when traversing the childs of a node it does so following by the
 * natural ordering of the child nodes (based on their ids and values) or using 
 * a custom order determined by the result of a BiFunction that can be set with
 * {@link #useCustomOrdering(BiFunction)}
 * <p>
 * Indexes can be added to this tree to be able to find and access its nodes
 * without traversing the tree. Adding one or more indexes increases the time to
 * add, remove or change nodes in the tree but has the benefit of faster access 
 * to finding nodes in the tree when getting the nodes from an index. Each index
 * creates a table of key node entries where each key is the result of a lambda
 * function passed to {@link #addIndex(String, Function)} when creating the index. 
 * Be aware that if there is a NullPointerException thrown by the lambda function
 * then the node will not be included in the entry since the index does not support
 * null keys.
 * <p>
 * When creating an index the node properties to be used in the lambda function
 * should not access properties from other nodes that are not directly connected
 * to a node. For example:<br>
 * valid:<br>
 * <code>
 * addIndex("someIndex", node -&gt node.getId())<br>
 * addIndex("someIndex", node -&gt node.getParent().getChildrenSize())<br>
 * addIndex("someIndex", node -&gt node.getChildById("A").getValue()).)<br>
 * </code>
 * invalid:<br>
 * <code>
 * addIndex("someIndex", node -&gt node.getParent().getParent().getValue())<br>
 * addIndex("someIndex", node -&gt node.getChildById("A").node.getChildById("B").getValue())
 * </code>
 * <p>
 * All methods from {@code NTree} or {@code NTreeNode} that modify the tree will
 * update the indexes also so the indexes are automatically kept in sync with the
 * tree changes.
 * <p>
 * The methods of this class that use functional interface arguments convert the
 * lambda function passed to another lambda function that prevents NullPointerException
 * from breaking the execution and instead make the lambda function return null
 * or false or not execute if it is a Consumer. If a NullPointerException is 
 * thrown for a Predicate or BiPredicate argument the result of executing the 
 * lambda function would be false. If the parameter is of type Function then the
 * result of the lambda function would be null.
 * <p>
 * Note that no method accepts null values as arguments except 
 * {@link #isClone(NTree, Collection)}} for the {@code Collection} parameter.
 * <p>
 * The {@link NearestCommonAncestorTool} class can be used to find the nearest
 * common ancestor between two nodes in a tree plus having other methods to get 
 * all nodes between the path between them and the their common ancestor.
 * <p>
 * This class is not thread safe.
 * 
 * @author Eladio Rodriguez Veve
 * @param <K> The type of the tree and nodes ids. Must implement {@code Comparable}.
 * @param <V> The type of the {@code value} property of the nodes.
 */
public class NTree<K extends Comparable<K>,V> implements Iterable<NTreeNode<K,V>> {
	
	String uuid = UUID.randomUUID().toString();
	K id;
	long version = 1;
	NTreeNode<K,V> root;
	transient Type nodeValueType;
	NodeValueCloningMode nodeValueCloningMode;
	@SuppressWarnings("rawtypes")
	transient Map<String, TreeNodeIndex> indexes = new HashMap<>();
	boolean isOrdered = false;
	NodeComparator<K,V> nodeComparator;
	
	//==============================================================================================
	//	STATIC FACTORY
	//==============================================================================================
	
	/**
	 * Returns a new NTree with an id for the tree.
	 * 
	 * @param 	treeId the id of the tree
	 * @return 	a new NTree that has the provided id
	 */
	public static <K extends Comparable<K>,V> NTree<K,V> create(K treeId) {
		argsNotNull(treeId);
		return new NTree<K,V>(treeId);
	}
	
	//==============================================================================================
	//	CONSTRUCTOR, GETTERS AND SETTERS
	//==============================================================================================
	
	/**
	 * Constructor for NTree with an id for the tree.
	 * 
	 * @param treeId the id of this tree
	 */
	public NTree(K treeId) {
		argsNotNull(treeId);
		this.id = treeId;
	}
	
	/**
	 * Replaces the id of the tree.
	 * 
	 * @param id the new id for this tree
	 */
	public void replaceId(K id) {
		this.id = id;
	}
	
	/**
	 * Returns the id of this tree.
	 * 
	 * @return the id of this tree
	 */
	public K getId() {
		return this.id;
	}
	
	/**
	 * Returns the string UUID of this tree. Each tree has a unique UUID which 
	 * is automatically generated when a tree is instantiated or cloned. 
	 * Serialization and deserialization to and from JSON does not create a new 
	 * UUID.
	 * 
	 * @return the string UUID unique to this tree
	 */
	public String getUUID() {
		return this.uuid;
	}
	
	/**
	 * Returns the root of this tree which is the first node in the tree's node
	 * hierarchy. It could be {@code null}.
	 * 
	 * @return the root node of this tree. Could be {@code null}.
	 */
	public NTreeNode<K,V> getRoot() {
		return this.root;
	}
	
	/**
	 * Returns the Type instance used for serializing and deserializing to and from 
	 * JSON the {@code value} property of the nodes. This property is set using
	 * {@link #nodeValueCloningUsesSerialization(Type)}
	 * 
	 * @return the Type instance used for serializing and deserializing to and from 
	 * JSON the {@code value} property of the nodes.
	 */
	public Type getNodeValueType() {
		return this.nodeValueType;
	}
	
	/**
	 * @return the {@link NodeValueCloningMode} used to determine the way that 
	 * the the node {@code value} field of each node is cloned. They can be 
	 * cloned using copy constructors or serialization.
	 */
	public NodeValueCloningMode getNodeValueCloningMode() {
		return this.nodeValueCloningMode;
	}
	
	/**
	 * Increments by one the version of this tree.
	 */
	public void incrementVersion() {
		this.version++;
	}
	
	/**
	 * Returns the version number of this tree. All instances of this tree start
	 * with a version value of 1.
	 * 
	 * @return the version of this tree
	 */
	public long getVersion() {
		return this.version;
	}
	
	//==============================================================================================
	//	CONFIGURATION
	//==============================================================================================
	
	/**
	 * Configures this tree to use the copy constructor of the class of the value
	 * property of the nodes when cloning nodes. The class of the value property 
	 * of the nodes must be public and its copy constructor must be public.
	 */
	public void nodeValueCloningUsesCopyConstructor() {
		this.nodeValueCloningMode = NodeValueCloningMode.BY_COPY_CONSTRUCTOR;
	}
	
	/**
	 * Configures this tree to use JSON serialization and deserialization for the
	 * value property of the nodes when cloning nodes. This is the default 
	 * configuration so use this method if another configuration method was used
	 * and you want to revert back to using simple serialization.
	 */
	public void nodeValueCloningUsesSerialization() {
		this.nodeValueCloningMode = NodeValueCloningMode.BY_SERIALIZATION;
		this.nodeValueType = null;
	}
	
	/**
	 * Configures this tree to use JSON serialization and deserialization for the
	 * value property of the nodes when cloning nodes. This method also stores the
	 * Type instance used for serializing and deserializing more complex types 
	 * of the {@code value} property of the nodes.<br>
	 * Example of how to create a Type instance for 
	 * <code>SomeGenericClass&ltAnotherClass&gt</code>:
	 * <p>
	 * <code>Type nodeValurType = 
	 * new TypeToken&ltSomeGenericClass&ltAnotherClass&gt&gt(){}.getType();</code>
	 * 
	 * @param nodeValueType the type of the node value property
	 */
	public void nodeValueCloningUsesSerialization(Type nodeValueType) {
		argsNotNull(nodeValueType);
		this.nodeValueCloningMode = NodeValueCloningMode.BY_SERIALIZATION;
		this.nodeValueType = nodeValueType;
	}
	
	/**
	 * Configures this tree so when traversing the tree the order in which the
	 * children of each node is visited is not ordered. This is the default.
	 */
	public void dontUseOrdering() {
		this.isOrdered = false;
		this.nodeComparator = null;
	}
	
	/**
	 * Configures this tree so when traversing the tree the order in which the
	 * children of each node is visited corresponds with their natural ordering.
	 * The natural ordering between sibling nodes is determined by their id.
	 */
	public void useNaturalOrdering() {
		this.isOrdered = true;
		this.nodeComparator = null;
	}
	
	/**
	 * Configures this tree so when traversing the tree the order in which the
	 * children of each node is visited is determined by the result of the 
	 * BiFunction whose return value is an integer. The result of the BiFunction
	 * should correspond with the result of Comparator.compare.
	 * 
	 * @param compareBiFunction the BiFUnction whose result determines the 
	 * 							traversal order between children of a node
	 */
	public void useCustomOrdering(BiFunction<NTreeNode<K,V>,NTreeNode<K,V>,Integer> compareBiFunction) {
		this.isOrdered = true;
		this.nodeComparator = new NodeComparator<K,V>(safeCompareBiFunction(compareBiFunction));
	}
	
	/**
	 * Returns {@code true} if the traversal order between a node's children is 
	 * unordered. This is the default. It is the most performant way to traverse
	 * the children of nodes.
	 * 
	 * @return {@code true} if the traversal order between a node's children is unordered.
	 */
	public boolean isUnordered() {
		return !this.isOrdered && this.nodeComparator == null;
	}
	
	/**
	 * Returns {@code true} if the traversal order between a node's children is 
	 * based on the natural ordering between them. The natural order of nodes is
	 * determined by the {@link NTreeNode#compareTo(NTreeNode)} method.
	 * Use {@link #useNaturalOrdering()} to set natural ordering for visiting
	 * the node's children.
	 * 
	 * @return {@code true} if the traversal order between a node's children is 
	 * based on the natural ordering between them.
	 */
	public boolean isNaturalOrdered() {
		return this.isOrdered && this.nodeComparator == null;
	}
	
	/**
	 * Returns {@code true} if the traversal order between a node's children is
	 * based on the BiFunction passed to {@link #useCustomOrdering(BiFunction)}
	 * 
	 * @return {@code true} if the traversal order between a node's children is
	 * based on the BiFunction passed to {@link #useCustomOrdering(BiFunction)}
	 */
	public boolean isCustomOrdered() {
		return this.isOrdered && this.nodeComparator != null;
	}
	
	/**
	 *  Returns {@code true} if the traversal order between a node's children is
	 *  based on natural ordering of the nodes or the provided BiFUnction
	 * 	passed to {@link #useCustomOrdering(BiFunction)}
	 * 
	 * @return {@code true} if the traversal order between a node's children is 
	 * based on natural ordering of the nodes or the provided BiFUnction
	 * passed to {@link #useCustomOrdering(BiFunction)}
	 */
	public boolean isOrdered() {
		return this.isOrdered;
	}
	
	//==============================================================================================
	//	TREE MANIPULATION
	//==============================================================================================
	
	/**
	 * Adds a {@link NTreeNode} as the root of this tree if this tree has no root
	 * node set yet. The node's treeOfBelonging must be this tree and the node's
	 * parent must be null.
	 * 
	 * @param 	newRoot the new NTreeNode to be the root of this tree.
	 * @return 	{@code true} if it added the new root node or {@code false} if 
	 * this tree already has a root node or 
	 * @throws 	RuntimeException if the passed node's treeOfBelonging is not this
	 * 			tree or the node's parent is not null
	 */
	public boolean addNewRoot(NTreeNode<K,V> newRoot) {
		argsNotNull(newRoot);
		if (this.root != null) {
			return false;
		}
		if (newRoot.treeOfBelonging != this || newRoot.parent != null) {
			return false;
		}
		this.root = newRoot;
		recreateIndexes();
		return true;
	}
	
	/**
	 * Sets the root of this tree. If the tree already has a root, it will replace
	 * the old root with all its descendants with the new root and its descendants.
	 * The node passed and its descendanst will not be changed and instead a 
	 * clone of it and its descendants will be used.
	 * 
	 * @param node the node whose clone and cloned descendants will replace the 
	 * 				existing root with its descendants.
	 * @return the replaced root or {@code null}. Null is returned for one of the 
	 * 			following reasons:<br>
	 * 			&#8226 There was no root, so the replaced node was null.<br>
	 * 			&#8226 The passed node to this method is this tree's existing root.<br>
	 */
	public NTreeNode<K,V> setRoot(NTreeNode<K,V> node) {
		argsNotNull(node);
		if (this.root == null) {
			this.root = node.clone(this);
			recreateIndexes();
			return null;
		}
		return this.root.replaceWith(node);
	}
	
	/**
	 * Sets the root of this tree without replacing the descendants of the
	 * current root. The node passed will not be changed and instead a clone of 
	 * it will be used.
	 *
	 *@param 	node the node whose clone will replace the existing root.
	 *@return 	the replaced root or {@code null}. Null is returned for one of the 
	 * 			following reasons:<br>
	 * 			&#8226 There was no root, so the replaced node was null.<br>
	 * 			&#8226 The passed node to this method is this tree's existing root.<br>
	 */
	public NTreeNode<K,V> setRootSingleNode(NTreeNode<K,V> node) {
		argsNotNull(node);
		if (this.root == null) {
			this.root = node.cloneSingleNode(this);
			recreateIndexes();
			return null;
		}
		return this.root.replaceSingleNodeWith(node);
	}
	
	/**
	 * Sets the root of this tree to null.
	 */
	public void clearTree() {
		clearAllIndexes();
		this.root = null;
	}
	
	//==============================================================================================
	//	DERIVED PROPERTIES
	//==============================================================================================
	
	/**
	 * Returns the number of nodes in this tree.
	 * 
	 * @return the number of nodes in this tree
	 */
	public int size() {
		if (this.root == null) {
			return 0;
		}
		return this.root.size();
	}
	
	/**
	 * Returns the height of this tree meaning the number of nodes from the root 
	 * node down the path to the deepest node.
	 * 
	 * @return the height of this tree
	 */
	public int height() {
		if (this.root == null) {
			return 0;
		}
		return this.root.height();
	}
	
	//==============================================================================================
	//	INDEX
	//==============================================================================================
	
	/**
	 * Adds an index to the tree. An index is used to quickly access a node based
	 * on an associated key without needing to traverse the tree to find the node.<br>
	 * The passed keyGeneratingFunction will be used to generate the keys for 
	 * the nodes in the index.
	 * 
	 * @param 	<R> the type of the index keys
	 * @param 	indexName the name of the index to be created
	 * @param 	keyGeneratingFunction the function used to generate the index keys
	 * @return {@code true} if there is no other existing index with the same name
	 * 			and it adds the index or false if there is another index with the
	 * 			same name and it does not add the index.
	 */
	public <R> boolean addIndex(String indexName, Function<NTreeNode<K,V>, R> keyGeneratingFunction) {
		argsNotNull(indexName, keyGeneratingFunction);
		TreeNodeIndex<K,V,R> index = new TreeNodeIndex<>(indexName, this, safeFunction(keyGeneratingFunction));
		if (this.indexes.putIfAbsent(indexName, index) == null) {
			index.computeIndex();
			return true;
		}
		return false;
	}
	
	/**
	 * Removes an existing index of this tree if it exists.
	 * 
	 * @param indexName the name of the index to remove
	 */
	public void removeIndex(String indexName) {
		argsNotNull(indexName);
		this.indexes.remove(indexName);
	}
	
	/**
	 * Removes all indexes in this tree.
	 */
	public void removeAllIndexes() {
		this.indexes.clear();
	}
	
	/**
	 * Recreates all existing indexes of this tree.
	 */
	public void recreateIndexes() {
		this.indexes.forEach((indexName, index) -> index.computeIndex());
	}
	
	/**
	 * Returns a list of the indexes names of this tree.
	 * 
	 * @return a list of the indexes names of this tree
	 */
	public List<String> getIndexNames() {
		List<String> list = new LinkedList<>();
		this.indexes.forEach((indexName, index) -> list.add(indexName));
		return list;
	}
	
	@SuppressWarnings("unchecked")
	void removeNodeFromAllIndexes(NTreeNode<K,V> node) {
		this.indexes.forEach((indexName,index) -> index.remove(node));
	}
	
	void removeNodesFromAllIndexes(Collection<NTreeNode<K,V>> nodes) {
		nodes.forEach(nodeItem -> removeNodeFromAllIndexes(nodeItem));
	}
	
	void clearAllIndexes() {
		this.indexes.forEach((indexName,index) -> index.clear());
	}
	
	@SuppressWarnings("unchecked")
	void putNodeInAllIndexes(NTreeNode<K,V> node) {
		this.indexes.forEach((indexName,index) -> index.put(node));
	}
	
	void putNodesInAllIndexes(Collection<NTreeNode<K,V>> nodes) {
		nodes.forEach(nodeItem -> putNodeInAllIndexes(nodeItem));
	}
	
	/**
	 * Returns a list of the nodes in an index that are mapped to a key in that index.
	 * 
	 * @param <R> the type of the index keys
	 * @param indexName the name of the index to get the nodes from
	 * @param key the key in the index that maps to zero or more nodes in that index
	 * @return a list of nodes that are mapped to a key in the named index or 
	 * 			{@code null} if the index does not exist
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <R> List<NTreeNode<K,V>> nodesInIndexWithKey(String indexName, R key) {
		argsNotNull(indexName, key);
		TreeNodeIndex index = this.indexes.get(indexName);
		if (index == null) {
			return null;
		}
		return index.getNodes(key);
	}
	
	/**
	 * Gets the first node from the list of nodes mapped to an index key in the index.
	 * 
	 * @param <R> the type of the index keys
	 * @param indexName the name of the index to get the nodes from
	 * @param key the key of the index mapped to zero or more nodes in that index
	 * @return the first node in the index mapped to the provided key or {@code null}
	 * 			if the index does not exists or there is no node in the index 
	 * 			mapped to the key
	 */
	public <R> NTreeNode<K,V> firstNodeInIndexWithKey(String indexName, R key) {
		argsNotNull(indexName, key);
		List<NTreeNode<K,V>> nodes = nodesInIndexWithKey(indexName,key);
		if (nodes == null || nodes.size() < 1) {
			return null;
		}
		return nodes.get(0);
	}
	
	//==============================================================================================
	//	SERIALIZATION
	//==============================================================================================
	
	/**
	 * Returns a JSON string representation of this tree. The nodeValueType and the
	 * tree indexes are not included in the JSON string.
	 * 
	 * @return a JSON String representing this tree
	 */
	public String toJson() {
		return gsonDefault.toJson(this);
	}
	
	/**
	 * Creates and returns a {@link NTree} given a JSON string representation of a NTree. 
	 * 
	 * @return a NTree instance that matches with its JSON representation
	 */
	@SuppressWarnings("static-access")
	public static <K extends Comparable<K>,V> NTree<K,V> fromJson(String json, Class<K> idClass,  Type nodeValueType) {
		argsNotNull(json, idClass, nodeValueType);
		NTree<K,V> tree = gsonDefault.fromJson(json, TypeToken.getParameterized(NTree.class, idClass, nodeValueType).getType());
		tree.root.reassignMissingReferences(tree.root, tree);
		tree.root.parent = new NTreeNode<K,V>(tree);
		tree.nodeValueType = nodeValueType;
		return tree;
	}
	
	//==============================================================================================
	//	CONVENIENCE
	//==============================================================================================
	
	/**
	 * Returns a new {@link NTreeNode} with the provided id and {@code null} 
	 * value whose treeOfBelonging is this tree.
	 * 
	 * @param id the id of the node to be created
	 * @return a new NTreeNode with the provided id and {@code null} value
	 */
	public NTreeNode<K,V> createNode(K id) {
		argsNotNull(id);
		return new NTreeNode<K,V>(this, id);
	}
	
	/**
	 * Returns a new {@link NTreeNode} with the provided id and value whose
	 * treeOfBelonging is this tree.
	 * 
	 * @param id the id of the node to be created
	 * @param value the value of the node to be created
	 * @return a new NTreeNode with the provided id and value
	 */
	public NTreeNode<K,V> createNode(K id, V value) {
		argsNotNull(id, value);
		return new NTreeNode<K,V>(this, id, value);
	}
	
	/**
	 * Returns a new {@link NTreeNode} with the provided id and {@code null} value whose
	 * treeOfBelonging is this tree. It's the same as {@link #createNode(Comparable)}.
	 * 
	 * @param id the id of the node to be created
	 * @return a new NTreeNode with the provided id and null value
	 */
	public NTreeNode<K,V> n(K id) {
		argsNotNull(id);
		return createNode(id);
	}
	
	/**
	 * Returns a new {@link NTreeNode} with the provided id and value whose
	 * treeOfBelonging is this tree. It's the same as {@link #createNode(Comparable, Object)}.
	 * 
	 * @param id the id of the node to be created
	 * @param value the value of the node to be created
	 * @return a new NTreeNode with the provided id and value
	 */
	public NTreeNode<K,V> n(K id, V value) {
		argsNotNull(id, value);
		return createNode(id, value);
	}
	
	//TODO test
	/**
	 * Returns a list of all the nodes for which the provided predicate returns 
	 * {@code true}.
	 * 
	 * @param predicate the predicate used for filtering which nodes from the tree
	 * 		to get
	 * @return a list of nodes for which the provided predicate returns {@code true}.
	 */
	public List<NTreeNode<K,V>> findAll(Predicate<NTreeNode<K,V>> predicate) {
		argsNotNull(predicate);
		if (this.root == null) {
			return new LinkedList<>();
		}
		return this.root.findAll(predicate);
	}
	
	//TODO test
	/**
	 * Returns the first node that satisfies the provided predicate being 
	 * {@code true} when traversing this tree in a preorder manner to find it.
	 * 
	 * @param predicate Predicate used to filter if a node is a match or not
	 * @return the first node found to satisfy the predicate or {@code null} if
	 * 			no node in this tree satisfies the predicate or the tree is empty
	 */
	public NTreeNode<K,V> findFirst(Predicate<NTreeNode<K,V>> predicate) {
		argsNotNull(predicate);
		if (this.root == null) {
			return null;
		}
		return this.root.findFirst(predicate);
	}
	
	/**
	 * Returns the first node that has the provided id when traversing this tree
	 * in a preorder manner to find it.
	 * 
	 * @param id the id used to find the first node with given id
	 * @return the first node found that has the id or {@code null} if no node 
	 * 			in this tree has the id or the tree is empty
	 */
	public NTreeNode<K,V> findFirstWithId(K id) {
		argsNotNull(id);
		if (this.root == null) {
			return null;
		}
		return this.root.findFirstWithId(id);
	}
	
	/**
	 * Returns the first node that has the provided value when traversing this 
	 * tree in a preorder manner to find it.
	 * 
	 * @param value the value used to find the first node with given value
	 * @return the first node found that has the value or {@code null} if no node 
	 * 			in this tree has the value or the tree is empty
	 */
	public NTreeNode<K,V> findFirstWithValue(V value) {
		argsNotNull(value);
		if (this.root == null) {
			return null;
		}
		return this.root.findFirstWithValue(value);
	}
	
	/**
	 * Returns a List of the nodes in this tree in the order in which the
	 * tree is traversed specified by the provided {@code TreeTraversalOrder} enum.
	 * The traversal order between children is unordered by default or if the tree
	 * was configured with {@link #dontUseOrdering()}. Configure this tree with
	 * {@link #useNaturalOrdering()} or {@link #useCustomOrdering(BiFunction)} 
	 * for ordered traversal between children.
	 * 
	 * @param traversal the traversal order in which this tree is traversed in 
	 * 					order to create the list
	 * @return a LinkedList of the nodes in this tree ordered based on traversal
	 * 			mode chosen
	 */
	public List<NTreeNode<K,V>> toList(TreeTraversalOrder traversal) {
		argsNotNull(traversal);
		if (this.root == null) {
			return new LinkedList<>();
		}
		return this.root.toList(traversal);
	}
	
	/**
	 * Returns a List of the nodes in this tree in the order in which the
	 * tree is traversed which is in a preorder manner.
	 * The traversal order between children is unordered by default or if the tree
	 * was configured with {@link #dontUseOrdering()}. Configure this tree with
	 * {@link #useNaturalOrdering()} or {@link #useCustomOrdering(BiFunction)} 
	 * for ordered traversal between children.
	 * 
	 * @return a LinkedList of the nodes in this tree ordered based on a preorder
	 * 			traversal of this tree.
	 */
	public List<NTreeNode<K,V>> toList() {
		if (this.root == null) {
			return new LinkedList<>();
		}
		return this.root.toList();
	}
	
	/**
	 * Returns an LList of the nodes in the given level.
	 * 
	 * @param level the level from which to get the nodes
	 * @return a list of the nodes in the given level
	 * @throws RuntimeException if the provided level is less than 1
	 */
	public List<NTreeNode<K,V>> nodesInLevel(int level) {
		if (this.root == null) {
			return new LinkedList<>();
		}
		return this.root.nodesInLevel(level);
	}
	
	/**
	 * Returns a List of values that are the result of the execution 
	 * of the provided mapping function to each node in this tree.
	 * 
	 * @param <R> the type of the values returned by the provided function
	 * @param function the mapping function for each node
	 * @return a list of values that are the result of applying the provided function
	 * 			to each node in this tree
	 */
	public <R> List<R> mapToList(Function<NTreeNode<K,V>,R> function) {
		argsNotNull(function);
		if (this.root == null) {
			return new LinkedList<>();
		}
		return this.root.mapToList(function);
	}
	
	/**
	 * Returns a new tree which is a deep copy of this tree including its version
	 * and indexes but with a different id.
	 * 
	 * @param id the id of the cloned tree
	 * @return a deep copy of this tree with the provided id
	 */
	@SuppressWarnings("unchecked")
	public NTree<K,V> clone(K id) {
		argsNotNull(id);
		NTree<K,V> clone = new NTree<K,V>(id);
		clone.nodeValueCloningMode = this.nodeValueCloningMode;
		clone.nodeValueType = this.nodeValueType;
		clone.root = this.root.clone(clone);
		clone.version = this.version;
		clone.isOrdered = this.isOrdered;
		if (this.nodeComparator != null) {
			clone.nodeComparator = new NodeComparator<>(this.nodeComparator.compareBiFunction);
		}
		Map<String, TreeNodeIndex> clonedIndexes = new HashMap<>();
		this.indexes.forEach((name, index) -> clonedIndexes.put(name, index.cloneIndex(clone)));
		clone.indexes = clonedIndexes;
		return clone;
	}
	
	/**
	 * Returns a new tree which is a deep copy of this tree including its version
	 * and indexes.
	 * 
	 * @return a deep copy of this tree.
	 */
	public NTree<K,V> clone() {
		return clone(this.id);
	}
	
	/**
	 * Traverses this tree and performs an action for each node. The tree is 
	 * traversed in the order specified by the provided {@code TreeTraversalOrder} 
	 * enum. The traversal order between children is unordered by default or if 
	 * the tree was configured with {@link #dontUseOrdering()}. Configure this 
	 * tree with {@link #useNaturalOrdering()} or {@link #useCustomOrdering(BiFunction)} 
	 * for ordered traversal between children.
	 * 
	 * @param traversal the traversal order to use
	 * @param action a consumer that performs an action for each node
	 */
	public void forEachNode(TreeTraversalOrder traversal, Consumer<NTreeNode<K,V>> action) {
		argsNotNull(traversal, action);
		if (this.root != null) {
			this.root.forEachNode(traversal, action);
		}
	}
	
	/**
	 * Traverses this tree and performs an action for each node. The tree is 
	 * traversed in a preorder manner. The traversal order between children is 
	 * unordered by default or if the tree was configured with 
	 * {@link #dontUseOrdering()}. Configure this tree with {@link #useNaturalOrdering()} 
	 * or {@link #useCustomOrdering(BiFunction)} for ordered traversal between children.
	 * 
	 * @param action a consumer that performs an action for each node
	 */
	public void forEachPreOrder(Consumer<NTreeNode<K,V>> action) {
		argsNotNull(action);
		if (this.root != null) {
			this.root.forEachPreOrder(action);
		}
	}
	
	/**
	 * Traverses this tree and performs an action for each node. The tree is 
	 * traversed in a postorder manner. The traversal order between children is 
	 * unordered by default or if the tree was configured with 
	 * {@link #dontUseOrdering()}. Configure this tree with {@link #useNaturalOrdering()} 
	 * or {@link #useCustomOrdering(BiFunction)} for ordered traversal between children.
	 * 
	 * @param action a consumer that performs an action for each node
	 */
	public void forEachPostOrder(Consumer<NTreeNode<K,V>> action) {
		argsNotNull(action);
		if (this.root != null) {
			this.root.forEachPostOrder(action);
		}
	}
	
	/**
	 * Traverses this tree and performs an action for each node. The tree is 
	 * traversed in a levelorder manner from root node down each level. The 
	 * traversal order between children of each node is unordered by default or 
	 * if the tree was configured with {@link #dontUseOrdering()}. Configure this
	 * tree with {@link #useNaturalOrdering()} or {@link #useCustomOrdering(BiFunction)} 
	 * for ordered traversal between children.
	 * 
	 * @param action a consumer that performs an action for each node
	 */
	public void forEachLevelOrder(Consumer<NTreeNode<K,V>> action) {
		argsNotNull(action);
		if (this.root != null) {
			this.root.forEachLevelOrder(action);
		}
	}
	
	/**
	 * Traverses this tree and performs an action for each node. The tree is 
	 * traversed in a levelorder manner from deepest node up each level up to root.
	 * The traversal order between children of each node is unordered by default or 
	 * if the tree was configured with {@link #dontUseOrdering()}. Configure this
	 * tree with {@link #useNaturalOrdering()} or {@link #useCustomOrdering(BiFunction)} 
	 * for ordered traversal between children.
	 * 
	 * @param action a consumer that performs an action for each node
	 */
	public void forEachLevelOrderFromBottom(Consumer<NTreeNode<K,V>> action) {
		argsNotNull(action);
		if (this.root != null) {
			this.root.forEachLevelOrderFromBottom(action);
		}
	}
	
	/**
	 * Performs an action for each node in the specified level. The traversal 
	 * order between children of each node is unordered by default or if the tree
	 * was configured with {@link #dontUseOrdering()}. Configure this tree with 
	 * {@link #useNaturalOrdering()} or {@link #useCustomOrdering(BiFunction)} 
	 * for ordered traversal between children.
	 * <p>
	 * The root is level 1, it's children would be level 2 and so on.
	 * 
	 * @param action a consumer that performs an action for each node
	 * @throws RuntimeException if the provided level is less than 1
	 */
	public void forEachOfLevel(int level, Consumer<NTreeNode<K,V>> action) {
		argsNotNull(action);
		if (this.root != null) {
			this.root.forEachOfLevel(level, action);
		}
	}
	
	/**
	 * Returns a {@link Stream} with the nodes ordered based on a a preorder 
	 * traversal of this tree.
	 * 
	 * @return a {@code Stream} of the nodes in this tree
	 */
	public Stream<NTreeNode<K,V>> stream() {
		if (this.root == null) {
			return new LinkedList<NTreeNode<K,V>>().stream();
		}
		return toList(TreeTraversalOrder.PRE_ORDER).stream();
	}
	
	//TODO test
	/**
	 * Returs true if the passed {code NTree} is a clone of this tree. This
	 * method checks other things that the overridden equals method does not. It
	 * A clone of an {@code NTree} must be equals in terms of the result of
	 * calling the {@code equals(Object)} method plus equality based on the
	 * following properties:<br>
	 * &#8226 If this tree is not the same instance as the other tree.<br>
	 * &#8226 If the version of the trees are equal.<br>
	 * &#8226 If the nodeValueType of both trees are equals. This is set using
	 * {@link #nodeValueCloningUsesSerialization(Type)}<br>
	 * &#8226 If the nodeValueCloningMode of both trees are equals. This is set
	 * using {@link #nodeValueCloningUsesCopyConstructor()} or 
	 * {@link #nodeValueCloningUsesSerialization()}.<br>
	 * &#8226 If the internal node comparators produce the same results for a sample
	 * of nodes. This internal node comparator is set when calling
	 * {@link #useCustomOrdering(BiFunction)}<br>
	 * &#8226 If the indexes of both trees are equal.
	 * 
	 * @param other the other tree to check if its a clone of this tree
	 * @param sampleNodes a {@code Collection} of nodes used to verify that this
	 * tree's nodeComparator gives the same result as the one from the clone.
	 * @return {@code true} if the other tree is a clone of this tree
	 */
	public boolean isClone(NTree<K,V> other, Collection<NTreeNode<K,V>> sampleNodes) {
		if (this == other)
			return false;
		if (!this.equals(other))
			return false;
		if (this.version != other.version)
			return false;
		if (this.nodeValueType == null) {
			if (other.nodeValueType != null)
				return false;
		} else if (!this.nodeValueType.equals(other.nodeValueType))
			return false;
		if (this.nodeValueCloningMode == null) {
			if (other.nodeValueCloningMode != null)
				return false;
		} else if (this.nodeValueCloningMode != other.nodeValueCloningMode)
			return false;
		if (this.isOrdered != other.isOrdered)
			return false;
		if (this.nodeComparator == null) {
			if (other.nodeComparator != null)
				return false;
		} else if (sampleNodes != null && sampleNodes.size() > 1) {
			Iterator<NTreeNode<K,V>> iterator = sampleNodes.iterator();
			NTreeNode<K,V> node1 = iterator.next();
			NTreeNode<K,V> node2 = iterator.next();
			if (this.nodeComparator.compare(node1, node2) != other.nodeComparator.compare(node1, node2))
				return false;
			while(iterator.hasNext()) {
				node1 = node2;
				node2 = iterator.next();
				if (this.nodeComparator.compare(node1, node2) != other.nodeComparator.compare(node1, node2))
				return false;
			}
		};
		if (!this.indexes.equals(other.indexes)) {
			return false;
		}
		return true;
	}
	
	/**
	 * Returns a simple graphic representation of this tree in a {@code String}.
	 * 
	 * @param <R> the return type of the provided dataFunction parameter. Java
	 * 			can imply it.
	 * @param width the width factor. If {@code null} or less than 1 then it
	 *              defaults to 4.
	 * @param height the height factor. If {@code null} or less than 1 then it 
	 * 				 defaults to 2.
	 * @param dataFunction the {@code Function} whose result is displayed next to
	 * each node's id. If {@code null} then no data is displayed next to each
	 * node's id.
	 * @return a {@code String} that when printed forms a simple graphic
	 * representation of this tree
	 */
	public <R> String treeGraph(Integer width, Integer height, Function<NTreeNode<K,V>,R> dataFunction) {
		if (this.root == null) {
			return "Empty Tree";
		}
		return this.root.treeGraph(width, height, dataFunction);
	}
	
	/**
	 * Returns a simple graphic representation of this tree in a {@code String}.
	 * 
	 * @param <R> the return type of the provided dataFunction parameter. Java
	 * 			can imply it.
	 * @param dataFunction the {@code Function} whose result is displayed next to
	 * each node's id. If {@code null} then no data is displayed next to each
	 * node's id.
	 * @return a {@code String} that when printed forms a simple graphic
	 * representation of this tree
	 */
	public <R> String treeGraph(Function<NTreeNode<K,V>,R> dataFunction) {
		return treeGraph(null, null, dataFunction);
	}
	
	/**
	 * Returns a simple graphic representation of this tree in a {@code String}.
	 * 
	 * @return a {@code String} that when printed forms a simple graphic
	 * representation of this tree
	 */
	public <R> String treeGraph() {
		return treeGraph(null, null, null);
	}
	
	//==============================================================================================
	//	OVERRIDEN
	//==============================================================================================
	
	/**
	 * Returns the hashcode of this tree which is based on it's id and the nodes
	 * in this tree.
	 * 
	 * @return an integer which is the hashcode for this tree
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((root == null) ? 0 : root.subtreeHashCode());
		return result;
	}

	/**
	 * Returns true if the id is equal, the node hierarchy is the same and each 
	 * node in the hierarchy is equal to the corresponding node in the other tree's
	 * hierarchy.
	 * 
	 * @param obj the other {@code NTree} being compared to this tree
	 * @return {@code true} if the provided tree is equal to this tree
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
		@SuppressWarnings("rawtypes")
		NTree other = (NTree) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (root == null) {
			if (other.root != null)
				return false;
		} else if (!root.equalsSubtree(other.root))
			return false;
		return true;
	}
	
	//TODO test
	/**
	 * Returns the {@code String} representation of this tree. It returns a JSON
	 * string representation of this tree but unlike {@link #toJson()} this JSON
	 * representation is pretty printed and includes nulls.
	 * 
	 * @return the {@code String} representation of this tree.
	 */
	@Override
	public String toString() {
		return gsonForTreeToString.toJson(this);
	}
	
	/**
	 * Returns an {@link Iterator} that can be used to iterate over this tree's
	 * nodes. The order of iteration over the nodes in this tree is based on the
	 * ordering of the nodes returned by {@link #toList()}.
	 * 
	 * @return the {@code Iterator} used to iterate over the nodes in this tree
	 */
	@Override
	public Iterator<NTreeNode<K,V>> iterator() {
		if (this.root == null) {
			return new LinkedList<NTreeNode<K,V>>().iterator();
		}
		return toList().iterator();
	}
	
}
  