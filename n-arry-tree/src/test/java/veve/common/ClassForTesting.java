package veve.common;

public class ClassForTesting<T> {
	T value;
	
	public ClassForTesting(T value) {
		this.value = value;
	}
	public ClassForTesting(ClassForTesting<T> other) {
		this(other.value);
	}
}
