package veve.datastructures.trees;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;


class Example<T> {
	
	T value;
	
	Example(T value) {
		this.value = value;
	}
	
	
	public static void main(String[] args) {
		
		Function<Integer, Integer> function = num -> num * 1 ;
		Function<Integer, Integer> function2 = num -> num * 1 ;
//		function = num -> num * 10;
//		
//		int f1 = function.apply(2);
//		int f2 = function2.apply(2);
		
		System.out.println(function.equals(function2));
		
		
	}
	

}
