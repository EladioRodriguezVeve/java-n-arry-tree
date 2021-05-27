package veve.datastructures.trees;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiPredicate;

import static veve.common.GeneralUtils.argsNotNull;

/**
 * A tool used to find the nearest common ancestor between two {@link NTreeNode}s,
 * the nodes from the first node to the ancestor, from the ancestor to the first
 * node, the nodes from the second node to the ancestor, the nodes from the 
 * ancestor  to the second node, the nodes from the first node to the second node
 * passing through the ancestor and the nodes from the second node to the first node
 * passing through the ancestor.
 */
public class NearestCommonAncestorTool<K extends Comparable<K>,V> {
	
	List<NTreeNode<K,V>> nodesFromAtoAncestor = new LinkedList<>();
	List<NTreeNode<K,V>> nodesFromBtoAncestor = new LinkedList<>();
	List<NTreeNode<K,V>> nodesFromAtoB = new LinkedList<>();
	boolean valid = true;
	
	/**
	 * Static factory method to create an instance of {@code NearestCommonAncestorTool}.
	 * 
	 * @param nodeA the node that has a common ancestor with nodeB
	 * @param nodeB the node that has a common ancestor with nodeA
	 * @return an instance of {@code NearestCommonAncestorTool}
	 */
	public static <K extends Comparable<K>,V> NearestCommonAncestorTool<K,V> forNodes(NTreeNode<K,V> nodeA, NTreeNode<K,V> nodeB) {
		return new NearestCommonAncestorTool<K,V>(nodeA, nodeB);
	}
	
	/**
	 * Constructor
	 * 
	 * @param nodeA the node that has a common ancestor with nodeB
	 * @param nodeB the node that has a common ancestor with nodeA
	 * @return an instance of {@code NearestCommonAncestorTool}
	 */
	public NearestCommonAncestorTool(NTreeNode<K,V> nodeA, NTreeNode<K,V> nodeB) {
		argsNotNull(nodeA, nodeB); 
		BiPredicate<NTreeNode<K,V>,NTreeNode<K,V>> bipredicate = ((a,b) -> 
			a.treeOfBelonging == b.treeOfBelonging &&
			nodeA.farthestAncestor() != null && nodeB.farthestAncestor() != null &&
			nodeA.farthestAncestor() == nodeB.farthestAncestor()
		);
		if (!bipredicate.test(nodeA, nodeB)) {
			this.valid = false;
			return;
		}
		if (nodeB.hasAncestor(nodeA)) {
			if (nodeA.parent == null) {
				this.valid = false;
				return;
			}
			List<NTreeNode<K,V>> nodesFromAtoB = nodeB.getNodesUpToAncestor(nodeA);
			Collections.reverse(nodesFromAtoB);
			this.nodesFromAtoB = nodesFromAtoB;
			this.nodesFromAtoAncestor = new LinkedList<>(Arrays.asList(nodeA, nodeA.parent));
			this.nodesFromBtoAncestor = nodeB.getNodesUpToAncestor(nodeA.parent);
		}
		else if (nodeA.hasAncestor(nodeB)) {
			if (nodeB.parent == null) {
				this.valid = false;
				return;
			}
			this.nodesFromAtoB = nodeA.getNodesUpToAncestor(nodeB);
			this.nodesFromAtoAncestor = nodeA.getNodesUpToAncestor(nodeB.parent);
			this.nodesFromBtoAncestor= new LinkedList<>(Arrays.asList(nodeB, nodeB.parent));
		}
		else {
			NTreeNode<K,V> currNodeA = nodeA;
			NTreeNode<K,V> currNodeB = nodeB;
			List<NTreeNodeUUID<K,V>> nodeAandAncestors = new ArrayList<>();
			List<NTreeNodeUUID<K,V>> nodeBandAncestors = new ArrayList<>();
			while(true) {
				if (currNodeA != null) {
					nodeAandAncestors.add(new NTreeNodeUUID<K,V>(currNodeA));
					currNodeA = currNodeA.parent;
				}
				if (currNodeB != null) {
					nodeBandAncestors.add(new NTreeNodeUUID<K,V>(currNodeB));
					currNodeB = currNodeB.parent;
				}
				if ((currNodeA == null && currNodeB == null) && 
						!nodeAandAncestors.get(nodeAandAncestors.size()-1).equals(nodeBandAncestors.get(nodeBandAncestors.size()-1))) {
					this.valid = false;
					return;
				}
				List<NTreeNodeUUID<K,V>> common = new ArrayList<>(nodeAandAncestors);
				common.retainAll(nodeBandAncestors);
				if (common.size() == 1) {
					NTreeNodeUUID<K,V> ancestor = common.get(0);
					// if both lists end with common ancestor
					if (nodeAandAncestors.get(nodeAandAncestors.size()-1).equals(ancestor) && nodeBandAncestors.get(nodeBandAncestors.size()-1).equals(ancestor)) {
						nodeAandAncestors.forEach(uuidNode -> nodesFromAtoAncestor.add(uuidNode.getNode()));
						nodeBandAncestors.forEach(uuidNode -> nodesFromBtoAncestor.add(uuidNode.getNode()));
					}
					else {
						if (nodeAandAncestors.get(nodeAandAncestors.size()-1).equals(ancestor)) {
							nodeAandAncestors.forEach(uuidNode -> nodesFromAtoAncestor.add(uuidNode.getNode()));
							//trim otherAndAncestors and put all nodes wrapped by its NTreeNodeUUID elements into nodesFromBtooAncestor
							int indexOfAncestor = nodeBandAncestors.indexOf(ancestor);
							nodeBandAncestors.subList(indexOfAncestor+1, nodeBandAncestors.size()).clear();
							nodeBandAncestors.forEach(uuidNode -> nodesFromBtoAncestor.add(uuidNode.getNode()));
						}
						else {
							nodeBandAncestors.forEach(uuidNode -> nodesFromBtoAncestor.add(uuidNode.getNode()));
							//trim nodeAndAncestors and put all nodes wrapped by its NTreeNodeUUID elements into nodesFromAtoAncestor
							int indexOfAncestor = nodeAandAncestors.indexOf(ancestor);
							nodeAandAncestors.subList(indexOfAncestor+1, nodeAandAncestors.size()).clear();
							nodeAandAncestors.forEach(uuidNode -> nodesFromAtoAncestor.add(uuidNode.getNode()));
						}
					}
					break;
				}
			}
			List<NTreeNode<K,V>> nodesFromAtoB = new LinkedList<>(this.nodesFromAtoAncestor);
			nodesFromAtoB.remove(nodesFromAtoB.size() - 1);
			nodesFromAtoB.addAll(nodesFromAncestorToB());
			this.nodesFromAtoB = nodesFromAtoB;
		}
	}
	
	/**
	 * @return {@code true} if nodeA and nodeB have a common ancestor
	 */
	public boolean hasCommonAncestor() {
		return this.valid;
	}
	
	/**
	 * @return the node that is the nearest common ancestor of nodeA and nodeB or
	 * {@code null} if they don't have a common ancestor
	 */
	public NTreeNode<K,V> commonAncestor() {
		if (!this.valid) { 
			return null;
		}
		return this.nodesFromAtoAncestor.get(this.nodesFromAtoAncestor.size() - 1);
	}

	/**
	 * @return the {@code List} of nodes from nodeA to the common ancestor ordered
	 * in that order or {@code null} if nodeA and nodeB don't have a common ancestor
	 */
	public List<NTreeNode<K,V>> nodesFromAtoAncestor() {
		if (!this.valid) { 
			return null;
		}
		return this.nodesFromAtoAncestor;
	}
	
	/**
	 * @return the {@code List} of nodes from nodeB to the common ancestor ordered
	 * in that order or {@code null} if nodeA and nodeB don't have a common ancestor
	 */
	public List<NTreeNode<K,V>> nodesFromBtoAncestor() {
		if (!this.valid) { 
			return null;
		}
		return this.nodesFromBtoAncestor;
	}
	
	/**
	 * @return the {@code List} of nodes from the common ancestor to nodeA ordered
	 * in that order or {@code null} if nodeA and nodeB don't have a common ancestor
	 */
	public List<NTreeNode<K,V>> nodesFromAncestorToA() {
		if (!this.valid) { 
			return null;
		}
		List<NTreeNode<K,V>> nodesFromAncestorToA = new LinkedList<>(this.nodesFromAtoAncestor);
		Collections.reverse(nodesFromAncestorToA);
		return nodesFromAncestorToA;
	}
	
	/**
	 * @return the {@code List} of nodes from the common ancestor to nodeB ordered
	 * in that order or {@code null} if nodeA and nodeB don't have a common ancestor
	 */
	public List<NTreeNode<K,V>> nodesFromAncestorToB() {
		if (!this.valid) { 
			return null;
		}
		List<NTreeNode<K,V>> nodesFromAncestorToB = new LinkedList<>(this.nodesFromBtoAncestor);
		Collections.reverse(nodesFromAncestorToB);
		return nodesFromAncestorToB;
	}
	
	/**
	 * @return the {@code List} of nodes from the nodeA to nodeB that pass through
	 * their nearest common ancestor ordered in that order or {@code null} if nodeA 
	 * and nodeB don't have a common ancestor
	 */
	public List<NTreeNode<K,V>> nodesFromAtoB() {
		if (!this.valid) { 
			return null;
		}
		return this.nodesFromAtoB;
	}
	
	/**
	 * @return the {@code List} of nodes from the nodeB to nodeA that pass through
	 * their nearest common ancestor ordered in that order or {@code null} if nodeA 
	 * and nodeB don't have a common ancestor
	 */
	public List<NTreeNode<K,V>> nodesFromBtoA() {
		if (!this.valid) { 
			return null;
		}
		List<NTreeNode<K,V>> nodesFromBtoA = new LinkedList<>(this.nodesFromAtoB);
		Collections.reverse(nodesFromBtoA);
		return nodesFromBtoA;
	}
	
}
