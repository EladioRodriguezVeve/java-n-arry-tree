package veve.datastructures.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonInstance {
	public static Gson gsonDefault = new Gson();
	public static Gson gsonForNodeToString = new GsonBuilder().setExclusionStrategies(new ExclusionStrategyForGsonIgnoreAnnotation())
			.serializeNulls().setPrettyPrinting().create();
	public static Gson gsonForTreeToString = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
	
}
