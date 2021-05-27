package veve.common;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.gson.reflect.TypeToken;

public class GeneralUtilsTest {
	
	void functionToTestArgsNotNull(String s, Integer i) {
		Object obj = null;
		GeneralUtils.argsNotNull(obj, obj);
	}
	
	void functionToTestArgsNotNullIncludingCollection() {
		Object obj = new Object();
		LinkedList<Object> list = new LinkedList<>(Arrays.asList(obj, obj, null, obj));
		GeneralUtils.argsNotNull(obj, list);
	}
	
	@Test void test_argsNotNull() {
		Exception exception = assertThrows(RuntimeException.class, () -> {
			functionToTestArgsNotNull("",1);
		});
		String expectedMessage = "null argument passed to functionToTestArgsNotNull(String, Integer) in class: veve.common.GeneralUtilsTest";
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage));
	}
	
	@Test void test_argsNotNull_includes_collection() {
		Exception exception = assertThrows(RuntimeException.class, () -> {
			functionToTestArgsNotNullIncludingCollection();
		});
		String expectedMessage = "null argument passed to functionToTestArgsNotNullIncludingCollection() in class: veve.common.GeneralUtilsTest";
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage));
	}
	
	@Test void test_classAndMethod() {
		String excpectedMessage = "veve.common.GeneralUtilsTest.test_classAndMethod()";
		assertEquals(excpectedMessage, GeneralUtils.classAndMethod());
	}
	
	@Test void test_cloneWithCopyConstructor_passedNull() {
		ClassForTesting<Integer> obj = null;
		ClassForTesting<Integer> clone = GeneralUtils.cloneWithCopyConstructor(obj);
		assertTrue(clone == null);
	}
	
	@Test void test_cloneWithCopyConstructor_clonesObject() {
		ClassForTesting<Integer> obj = new ClassForTesting<Integer>(1);
		ClassForTesting<Integer> clone = GeneralUtils.cloneWithCopyConstructor(obj);
		assertTrue(clone instanceof ClassForTesting);
		assertTrue(obj != clone);
		assertTrue(obj.value == clone.value);
	}
	
	@Test void test_cloneUsingSerialization_objectNull() {
		ClassForTesting<Integer> obj = null;
		ClassForTesting<Integer> clone = GeneralUtils.cloneUsingSerialization(obj);
		assertTrue(clone == null);
	}
	
	@Test void test_cloneUsingSerialization_null_typeToken() {
		ClassForTesting<Integer> obj = new ClassForTesting<Integer>(1);
		ClassForTesting<Integer> clone = GeneralUtils.cloneUsingSerialization(obj);
		assertTrue(obj != clone);
		assertTrue(clone instanceof ClassForTesting);
		assertTrue(obj.value == 1);
	}
	
	@Test void test_cloneUsingSerialization_with_typeToken() {
		ClassForTesting<Integer> c1 = new ClassForTesting<>(1);
		ClassForTesting<Integer> c2 = new ClassForTesting<>(2);
		Map<String, ClassForTesting<Integer>> map = new HashMap<>();
		map.put("A", c1);
		map.put("B", c2);
		List<ClassForTesting<Integer>> list = new ArrayList<>(Arrays.asList(c1,c2));
		Type mapToken = new TypeToken<Map<String, ClassForTesting<Integer>>>() {}.getType();
		Type listToken = new TypeToken<List<ClassForTesting<Integer>>>() {}.getType();
		Map<String, ClassForTesting<Integer>> mapClone = GeneralUtils.cloneUsingSerialization(map, mapToken);
		List<ClassForTesting<Integer>> listClone = GeneralUtils.cloneUsingSerialization(list, listToken);
		assertAll("mapClone",
				() -> assertTrue(mapClone.size() == 2),
				() -> assertTrue(mapClone instanceof Map),
				() -> assertTrue(mapClone.get("A") instanceof ClassForTesting),
				() -> assertTrue(mapClone.get("B") instanceof ClassForTesting),
				() -> assertTrue(mapClone.get("A").value == 1),
				() -> assertTrue(mapClone.get("B").value == 2)
		);
		assertAll("listClone",
				() -> assertTrue(listClone.size() == 2),
				() -> assertTrue(listClone instanceof List),
				() -> assertTrue(listClone.get(0) instanceof ClassForTesting),
				() -> assertTrue(listClone.get(1) instanceof ClassForTesting),
				() -> assertTrue(listClone.get(0).value == 1),
				() -> assertTrue(listClone.get(1).value == 2)
		);
	}
	
	@Test void test_safePredicate() {
		List<Integer> integers = new LinkedList<>(Arrays.asList(1,2,3));
		integers.add(null);
		integers.add(4);
		
		Predicate<Integer> unsafePredicate = num -> num > 0;
		Predicate<Integer> safePredicate = GeneralUtils.safePredicate(unsafePredicate);
		
		Multiset<Integer> filtered = HashMultiset.create(integers.stream().filter(safePredicate).collect(Collectors.toList()));
		Multiset<Integer> expected =  HashMultiset.create(Arrays.asList(1,2,3,4));
		assertEquals(expected, filtered);
	}
	
	@Test void test_safeConsumer() {
		List<Integer> integers = new LinkedList<>(Arrays.asList(1,2,3));
		integers.add(null);
		integers.add(4);
		List<Integer> integers2 = new LinkedList<>();
		
		Consumer<Integer> unsafeConsumer = num -> {
			if (num > 0)
				integers2.add(num);
		};
		Consumer<Integer> safeConsumer = GeneralUtils.safeConsumer(unsafeConsumer);
		
		integers.forEach(safeConsumer);
		List<Integer> expected =   new LinkedList<>(Arrays.asList(1,2,3,4));
		assertEquals(expected, integers2);
	}
	
	@Test void test_safeFunction() {
		List<Integer> integers = new LinkedList<>(Arrays.asList(1,2,3));
		integers.add(null);
		integers.add(4);
		
		Function<Integer, Integer> unsafeFunction = num -> num * 10;
		Function<Integer, Integer> safeFunction = GeneralUtils.safeFunction(unsafeFunction);
		
		Multiset<Integer> mapped = HashMultiset.create(integers.stream().map(safeFunction).collect(Collectors.toList()));
		Multiset<Integer> expected =  HashMultiset.create(Arrays.asList(null,10,20,30,40));
		assertEquals(expected, mapped);
	}
	
	@Test void test_safeBiPredicate() {
		BiPredicate<String,Integer> unsafeBiPredicate = (letter, num) -> num > 0;
		BiPredicate<String,Integer> safeBiPredicate = GeneralUtils.safeBiPredicate(unsafeBiPredicate);
		
		assertTrue(safeBiPredicate.test("", 1));
		assertFalse(safeBiPredicate.test("", 0));
		assertFalse(safeBiPredicate.test("", null));
	}
	
	@Test void test_safeCompareBiFunction() {
		BiFunction<Integer, Integer, Integer> unsafeCompareBiFunction = (a,b) -> a - b;
		BiFunction<Integer, Integer, Integer> safeCompareBiFunction = GeneralUtils.safeCompareBiFunction(unsafeCompareBiFunction);
		
		assertEquals(0, safeCompareBiFunction.apply(null, null));
		assertEquals(0, safeCompareBiFunction.apply(null, 1));
		assertEquals(0, safeCompareBiFunction.apply(1, null));
		assertEquals(0, safeCompareBiFunction.apply(1, 1));
		assertEquals(1, safeCompareBiFunction.apply(2, 1));
		assertEquals(-1, safeCompareBiFunction.apply(1, 2));
	}

}
