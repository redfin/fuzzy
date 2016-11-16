package com.redfin.fuzzy.cases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.redfin.fuzzy.Any;
import com.redfin.fuzzy.Literal;
import com.redfin.fuzzy.FuzzyUtil;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class CollectionCaseTest {

	private Random random;

	@Before
	public void before() {
		random = new Random(1234); // keep tests consistent
	}

	@Test
	public void testBasic() {
		Set<List<Integer>> actuals = Any.listOf(Any.integer().inRange(5, 10)).generateAllOnce(random);

		assertTrue(actuals.stream().anyMatch(List::isEmpty));
		assertFalse(actuals.stream().anyMatch(l -> l.size() > 100));

		assertFalse(actuals.stream().anyMatch(l -> l.stream().anyMatch(i -> i < 5 || i > 10)));
	}

	@Test
	public void testWithSize() {
		Set<List<Integer>> actuals = Any.listOf(Any::integer).withSize(5).generateAllOnce(random);

		assertFalse(actuals.stream().anyMatch(l -> l.size() != 5));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testWithInvalidSize() {
		Any.listOf(Any::integer).withSize(-1);
	}

	@Test
	public void testWithSizeOf() {
		Set<List<Integer>> actuals = Any.listOf(Any::integer).withSizeOf(Any.of(1, 2, 3)).generateAllOnce(random);

		assertTrue(actuals.stream().anyMatch(l -> l.size() == 1));
		assertTrue(actuals.stream().anyMatch(l -> l.size() == 2));
		assertTrue(actuals.stream().anyMatch(l -> l.size() == 3));

		assertFalse(actuals.stream().anyMatch(l -> l.isEmpty() || l.size() > 3));
	}

	@Test
	public void testWithElementsOf() {
		Set<Set<Integer>> actuals = Any.setOf(Any.of(5, 19, 82)).generateAllOnce(random);

		assertTrue(actuals.stream().anyMatch(s -> s.contains(5)));
		assertTrue(actuals.stream().anyMatch(s -> s.contains(19)));
		assertTrue(actuals.stream().anyMatch(s -> s.contains(82)));

		assertFalse(actuals.stream().anyMatch(s -> s.stream().anyMatch(i -> i != 5 && i != 19 && i != 82)));
	}

	@Test
	public void testWithElementsOfDelegateCase() {
		Set<List<Integer>> actuals = new CollectionCase.ListCase<Integer>()
			.withElementsOf(Any::positiveNonZeroInteger)
			.generateAllOnce(random);

		assertFalse(actuals.stream().anyMatch(l -> l.stream().anyMatch(i -> i <= 0)));
	}

	@Test
	public void testWithHomogeneousElementSuppliers() {
		Set<List<Integer>> actuals = Any
			.listOf(Any.of(1, 2))
			.withSizeOf(Any.of(3, 4))
			.withHomogeneousElementSuppliers()
			.generateAllOnce(random);

		Set<List<Integer>> expecteds = FuzzyUtil.setOf(
			Arrays.asList(1, 1, 1),
			Arrays.asList(1, 1, 1, 1),
			Arrays.asList(2, 2, 2),
			Arrays.asList(2, 2, 2, 2)
		);

		assertEquals(expecteds, actuals);
	}

	@Test(expected = IllegalStateException.class)
	public void testWithSizeOfInvalidSizeSupplier() {
		Any.listOf(Any::integer).withSizeOf(Literal.nil()).generateAllOnce(random);
	}

	@Test(expected = IllegalStateException.class)
	public void testWithHomogeneousElementSuppliersAndInvalidSizeSupplier() {
		Any.listOf(Any::integer).withSizeOf(Literal.nil()).withHomogeneousElementSuppliers().generateAllOnce(random);
	}

}
