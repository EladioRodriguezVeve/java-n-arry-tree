package veve.datastructures.trees;

public class ClassForTesting<T> {
	T value;
	
	public ClassForTesting(T value) {
		this.value = value;
	}
	public ClassForTesting(ClassForTesting<T> other) {
		this(other.value);
	}
	
	@Override
	public String toString() {
		return "Value: " + this.value;
	}
}
