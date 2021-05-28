package veve.datastructures.trees;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

class GsonInstance {
	public static Gson gsonDefault = new Gson();
	public static Gson gsonForNodeToString = new GsonBuilder().setExclusionStrategies(new ExclusionStrategyForGsonIgnoreAnnotation())
			.serializeNulls().setPrettyPrinting().create();
	public static Gson gsonForTreeToString = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
	
}
