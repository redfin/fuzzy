package com.redfin.fuzzy;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.Test;

public class CasesTest {

	@Test
	public void testMap() {
		Case<String> original = () -> Collections.singleton(r -> "1234");
		Case<Integer> mapped = Cases.map(original, Integer::new);

		Set<Function<Random, Integer>> suppliers = mapped.getSuppliers();
		assertEquals(1, suppliers.size());
		assertEquals(new Integer(1234), suppliers.stream().findFirst().get().apply(null));
	}

	@Test
	public void testMapWithMultipleSuppliers() {
		Case<String> original = () -> Util.setOf(
			r -> "1234",
			r -> "5678"
		);

		Case<Integer> mapped = Cases.map(original, s -> -Integer.valueOf(s));
		Set<Function<Random, Integer>> suppliers = mapped.getSuppliers();

		assertEquals(2, suppliers.size());

		Set<Integer> values = suppliers.stream()
			.map(s -> s.apply(null))
			.collect(Collectors.toSet())
		;

		assertEquals(
			Util.setOf(-1234, -5678),
			values
		);
	}

}
