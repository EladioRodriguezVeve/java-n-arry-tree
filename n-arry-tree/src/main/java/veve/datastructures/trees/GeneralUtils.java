package veve.datastructures.trees;

import static veve.datastructures.trees.GsonInstance.gsonDefault;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class GeneralUtils {
	
	public static void argsNotNull(Object ...objects) {
		boolean foundNull = false;
		for (int i = 0; i < objects.length && !foundNull; i++) {
			if (objects[i] == null) {
				foundNull = true;
			}
			else if (objects[i] instanceof Collection && ((Collection<?>) objects[i]).contains(null)) {
				foundNull = true;
			}
		}
		if (foundNull) {
			String errorMessage = "null argument passed to " + methodAndClassUsingArgsNotNull();
			throw new RuntimeException(new IllegalArgumentException(errorMessage));
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T cloneWithCopyConstructor(T obj) {
		if (obj == null) {
			return obj;
		}
		try {
			Constructor<?> copyConstructor = obj.getClass().getConstructor(obj.getClass());
			return (T) copyConstructor.newInstance(obj);
			
		} catch (NoSuchMethodException | SecurityException | InstantiationException |
				IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//For POJOS and simple types
	public static <T> T cloneUsingSerialization(T obj) {
		return cloneUsingSerialization(obj, null);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T cloneUsingSerialization(T obj, Type typeToken) {
		if (obj == null) {
			return obj;
		}
		if (typeToken == null) {
			return (T) gsonDefault.fromJson(gsonDefault.toJson(obj), obj.getClass());
		}
		return (T) gsonDefault.fromJson(gsonDefault.toJson(obj), typeToken);
	}
	
	public static String classAndMethod() {
		StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
		String methodName = walker.walk(frames -> frames.skip(1L).findFirst().map(frame -> frame.getMethodName())).get();
		List<Class<?>> parameterTypes = walker.walk(frames -> frames.skip(1L).findFirst().map(frame -> frame.getMethodType().parameterList())).get();
		String parametersSignature = parameterTypes.stream().map(clazz -> clazz.getSimpleName()).collect(Collectors.joining(", ", "(", ")"));
		String className = walker.walk(frames -> frames.skip(1L).findFirst().map(frame -> frame.getClassName())).get();
		return className + "." + methodName + parametersSignature;
	}
	
	private static String methodAndClassUsingArgsNotNull() {
		StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
		String methodName = walker.walk(frames -> frames.skip(2L).findFirst().map(frame -> frame.getMethodName())).get();
		List<Class<?>> parameterTypes = walker.walk(frames -> frames.skip(2L).findFirst().map(frame -> frame.getMethodType().parameterList())).get();
		String parametersSignature = parameterTypes.stream().map(clazz -> clazz.getSimpleName()).collect(Collectors.joining(", ", "(", ")"));
		String className = walker.walk(frames -> frames.skip(2L).findFirst().map(frame -> frame.getClassName())).get();
		return methodName + parametersSignature + " in class: " + className;
	}
	
	public static <T> Predicate<T> safePredicate(Predicate<T> predicate) {
		Predicate<T> safePredicate = obj -> {
			try {
				return predicate.test(obj);
			} catch(Exception e) {
				return false;
			}
		};
		return safePredicate;
	}
	
	public static <T> Consumer<T> safeConsumer(Consumer<T> consumer) {
		Consumer<T> safeConsumer = obj -> {
			try {
				consumer.accept(obj); 
			} catch(Exception e) {}
		};
		return safeConsumer;
	}
	
	public static <T,R> Function<T,R> safeFunction(Function<T,R> function) {
		Function<T,R> safeFunction = obj -> {
			try {
				return function.apply(obj); 
			} catch(Exception e) {
				return null;
			}
		};
		return safeFunction;
	}
	
	public static <T,U> BiPredicate<T,U> safeBiPredicate(BiPredicate<T,U> bipredicate) {
		BiPredicate<T,U> safeBiPredicate = (obj1, obj2) -> {
			try {
				return bipredicate.test(obj1, obj2);
			} catch(Exception e) {
				return false;
			}
		};
		return safeBiPredicate;
	}
	
	public static <T,U> BiFunction<T,U,Integer> safeCompareBiFunction(BiFunction<T,U,Integer> bifunction) {
		BiFunction<T,U,Integer> safeCompareBiFunction = (a, b) -> {
			try {
				return bifunction.apply(a,b); 
			} catch(Exception e) {
				return 0;
			}
		};
		return safeCompareBiFunction;
	}
	
}
