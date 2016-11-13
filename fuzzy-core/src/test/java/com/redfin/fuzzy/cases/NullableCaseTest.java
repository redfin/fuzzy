package com.redfin.fuzzy.cases;

import static org.junit.Assert.*;

import com.redfin.fuzzy.Any;
import com.redfin.fuzzy.Case;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;

public class NullableCaseTest {

	private Random random;

	@Before
	public void before() {
		random = new Random(12345); // keep tests consistent
	}

	@Test
	public void testGetSuppliers() {
		Case<Integer> subject = Any.nullableOf(Any.integer().greaterThanOrEqualTo(0));
		Set<Function<Random, Integer>> suppliers = subject.getSuppliers();

		assertEquals(3, suppliers.size());

		boolean foundNull, foundZero, foundPositive;
		foundNull = foundZero = foundPositive = false;

		for(Function<Random, Integer> supplier : suppliers) {
			Integer i = supplier.apply(random);
			if(i == null) {
				assertFalse(foundNull);
				foundNull = true;
			}
			else if(i == 0) {
				assertFalse(foundZero);
				foundZero = true;
			}
			else if(i > 0) {
				assertFalse(foundPositive);
				foundPositive = true;
			}
		}

		assertTrue(foundNull);
		assertTrue(foundZero);
		assertTrue(foundPositive);
	}

}
