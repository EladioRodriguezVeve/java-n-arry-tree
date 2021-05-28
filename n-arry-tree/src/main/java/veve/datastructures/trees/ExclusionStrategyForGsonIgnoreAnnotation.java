package veve.datastructures.trees;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

class ExclusionStrategyForGsonIgnoreAnnotation implements ExclusionStrategy {

	@Override
	public boolean shouldSkipField(FieldAttributes f) {
		return f.getAnnotation(GsonIgnore.class) != null;
	}

	@Override
	public boolean shouldSkipClass(Class<?> clazz) {
		return false;
	}

}
