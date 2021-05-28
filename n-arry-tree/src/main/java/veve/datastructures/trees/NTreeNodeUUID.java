package veve.datastructures.trees;

class NTreeNodeUUID<K extends Comparable<K>, V> {
	
	private NTreeNode<K,V> node;
	
	public NTreeNodeUUID(NTreeNode<K,V> node) {
		this.node = node;
	}
	
	public NTreeNode<K,V> getNode() {
		return this.node;
	}
	
	@Override
	public int hashCode() {
		return 31 * 17 + node.uuid.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NTreeNodeUUID<?, ?> other = (NTreeNodeUUID<?, ?>) obj;
		if (!this.node.uuid.equals(other.node.uuid))
			return false;
		return true;
	}
	
}
