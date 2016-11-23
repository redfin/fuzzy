package com.redfin.fuzzy.cases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.redfin.fuzzy.Any;
import com.redfin.fuzzy.Case;
import com.redfin.fuzzy.Subcase;
import java.util.Random;
import java.util.Set;
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
		Set<Subcase<Integer>> subcases = subject.getSubcases();

		assertEquals(3, subcases.size());

		boolean foundNull, foundZero, foundPositive;
		foundNull = foundZero = foundPositive = false;

		for(Subcase<Integer> supplier : subcases) {
			Integer i = supplier.generate(random);
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
