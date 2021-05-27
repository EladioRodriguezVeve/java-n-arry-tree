package veve.datastructures.trees;

import static veve.common.GeneralUtils.argsNotNull;
import static veve.common.GeneralUtils.safeFunction;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

public class TreeNodeIndex<K extends Comparable<K>,V, R> {
	
	String name;
	Table<R, String, NTreeNode<K,V>> indexTable;
	Function<NTreeNode<K,V>, R> keyGeneratingFunction;
	NTree<K,V> treeOfBelonging;
	
	TreeNodeIndex(String name, NTree<K,V> treeOfBelonging, Function<NTreeNode<K,V>,R> keyGeneratingFunction) {
		this.name = name;
		this.keyGeneratingFunction = safeFunction(keyGeneratingFunction);
		this.indexTable = HashBasedTable.create();
		this.treeOfBelonging = treeOfBelonging;
	}
	
	void computeIndex() {
		this.indexTable.clear();
		if ( this.treeOfBelonging.getRoot() != null) {
			List<NTreeNode<K,V>> nodes = this.treeOfBelonging.getRoot().toList();
			nodes.forEach(node -> { 
				try {
					this.indexTable.put(this.keyGeneratingFunction.apply(node), node.uuid, node);
				} catch(Exception e) {}
			});
		}
	}
	
	void put(NTreeNode<K,V> node) {
		remove(node);
		try {
			this.indexTable.put(this.keyGeneratingFunction.apply(node), node.uuid, node);
		} catch(Exception e) {}
	}
	
	@SuppressWarnings("unchecked")
	void remove(NTreeNode<K,V> node) {
		argsNotNull(node);
		Map<String, Map<R, NTreeNode<K,V>>> columnMap = this.indexTable.columnMap();
		Map<R, NTreeNode<K,V>> rowsMap = columnMap.get(node.uuid);
		R indexKey;
		if (rowsMap != null) {
			indexKey = (R) rowsMap.keySet().toArray()[0];
			this.indexTable.remove(indexKey, node.uuid);
		}
	}
	
	void clear() {
		this.indexTable.clear();
	}
	
	String getIndexName() {
		return this.name;
	}
	
	List<NTreeNode<K,V>> getNodes(R key) {
		return new LinkedList<NTreeNode<K,V>>(this.indexTable.row(key).values());
	}
	
	List<R> keysList() {
		List<R> keysList = new LinkedList<>();
		Iterator<Table.Cell<R, String, NTreeNode<K,V>>> iterator = this.indexTable.cellSet().iterator();
		while(iterator.hasNext()) {
			Cell<R, String, NTreeNode<K,V>> cell = iterator.next();
			keysList.add(cell.getRowKey());
		}
		return keysList;
	}
	
	TreeNodeIndex<K,V,R> cloneIndex(NTree<K,V> treeOfBelonging) {
		TreeNodeIndex<K,V,R> clone = new TreeNodeIndex<>(this.name, treeOfBelonging, this.keyGeneratingFunction);
		clone.computeIndex();
		return clone;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((indexTable == null) ? 0 : indexTable.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TreeNodeIndex other = (TreeNodeIndex) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (indexTable == null) {
			if (other.indexTable != null)
				return false;
		} else {
			List<Pair<R,NTreeNode<K,V>>> thisKeyNodePairsList = indexTable.cellSet().stream()
					.map(cell -> Pair.of(cell.getRowKey(),cell.getValue()))
					.collect(Collectors.toList());
			Multiset<Pair<R,NTreeNode<K,V>>> thisKeyNodePairs =  HashMultiset.create(thisKeyNodePairsList);
			List<Pair<R,NTreeNode<K,V>>> otherKeyNodePairsList = indexTable.cellSet().stream()
					.map(cell -> Pair.of(cell.getRowKey(),cell.getValue()))
					.collect(Collectors.toList());
			Multiset<Pair<R,NTreeNode<K,V>>> otherKeyNodePairs =  HashMultiset.create(otherKeyNodePairsList);
			if (!thisKeyNodePairs.equals(otherKeyNodePairs))
				return false;
		}
		return true;
	}
	
}
