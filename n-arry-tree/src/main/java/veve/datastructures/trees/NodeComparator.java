package veve.datastructures.trees;

import java.util.Comparator;
import java.util.function.BiFunction;

public class NodeComparator<K extends Comparable<K>,V> implements Comparator<NTreeNode<K,V>>{
	
	BiFunction<NTreeNode<K,V>,NTreeNode<K,V>,Integer> compareBiFunction;
	
	NodeComparator(BiFunction<NTreeNode<K,V>,NTreeNode<K,V>,Integer> compareBiFunction) {
		this.compareBiFunction = compareBiFunction;
	}

	@Override
	public int compare(NTreeNode<K,V> a, NTreeNode<K,V> b) {
		return this.compareBiFunction.apply(a, b);
	}

}
