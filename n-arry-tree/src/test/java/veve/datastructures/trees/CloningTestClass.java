package veve.datastructures.trees;

public class CloningTestClass<T> {
	
	T value;
	
	public CloningTestClass(T value) {
		this.value = value;
	}
	
	public CloningTestClass(CloningTestClass<T> other) {
		this(other.value);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CloningTestClass<T> other = (CloningTestClass<T>) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	
}
