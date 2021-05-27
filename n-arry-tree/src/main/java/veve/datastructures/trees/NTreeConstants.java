package veve.datastructures.trees;

/**
 * @author Eladio Rodriguez Veve
 */
public class NTreeConstants {
	
	/**
	 * Used to determine the way the value property of a node is cloned. This 
	 * can be either using the copy constructor of the class of the node value or
	 * using serialization.
	 * <p>
	 * The options are:<br>
	 * {@link #BY_COPY_CONSTRUCTOR}<br>
	 * {@link #BY_SERIALIZATION}
	 */
	public static enum NodeValueCloningMode {
		/**
		 * For cloning NTreeNode.value property using the copy constructor of 
		 * the class of the value.
		 */
		BY_COPY_CONSTRUCTOR,
		/**
		 * For cloning NTreeNode.value property using serialization and 
		 * deserialization to and from JSON.
		 */
		BY_SERIALIZATION;
	}
	
	/**
	 * Used to determine the traversal order of the tree. The order 
	 * in which the children of each node are visited is determined by setting
	 * this configuration using one of the following methods in a NTree:<br>
	 * {@link NTree#dontUseOrdering()}<br>
	 * {@link NTree#useNaturalOrdering()}<br>
	 * {@link NTree#useCustomOrdering(BiFunction)}<br>
	 * <p>
	 * The options are:<br>
	 * {@link #PRE_ORDER}<br>
	 * {@link #POST_ORDER}<br>
	 * {@link #LEVEL_ORDER}<br>
	 * {@link #LEVEL_ORDER_FROM_BOTTOM}<br>
	 */
	public static enum TreeTraversalOrder {
		/**
		 * For traversing the tree in preorder order. Meaning when traversing 
		 * the tree the nodes are visited first and then their children.
		 */
		PRE_ORDER,
		/**
		 * For traversing the tree in postorder order. Meaning when traversing 
		 * the tree the nodes children are visited first and then the nodes.
		 */
		POST_ORDER,
		/**
		 * For traversing the tree down each level visiting each node in 
		 * each level and then the nodes of the lower levels. 
		 */
		LEVEL_ORDER,
		/**
		 * For traversing the tree up from the deepest level up to the current 
		 * level. For example, if you have A->B->C and one traverses this tree
		 * from A then the traversal order would be C, B, A.
		 */
		LEVEL_ORDER_FROM_BOTTOM;
	}
	
}