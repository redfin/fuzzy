package com.redfin.fuzzy.cases;

import static org.junit.Assert.*;

import com.redfin.fuzzy.Any;
import java.util.Random;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class DoubleNumericCaseTest {

	private Random random;

	@Before
	public void before() {
		random = new Random(12345); // keep tests consistent
	}

	@Test
	public void testBasicCase() {
		Set<Double> actuals = Any.doublePrecisionNumber().generateAllOnce(random);

		assertTrue(actuals.stream().anyMatch(d -> d <= -1));
		assertTrue(actuals.stream().anyMatch(d -> d > -1 && d < 0));
		assertTrue(actuals.contains(0d));
		assertTrue(actuals.stream().anyMatch(d -> d > 0 && d < 1));
		assertTrue(actuals.stream().anyMatch(d -> d >= 1));

		assertEquals(5, actuals.size());
	}

	@Test
	public void testExcludingZero() {
		Set<Double> actuals = Any.doublePrecisionNumber().excluding(0d).generateAllOnce(random);

		assertFalse(actuals.contains(0d));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInRangeIllegalArgument() {
		Any.doublePrecisionNumber().inRange(1, -1);
	}

	@Test
	public void testInRangeLessThanNegativeOne() {
		Set<Double> actuals = Any.doublePrecisionNumber().inRange(-1337, -5).generateAllOnce(random);

		assertTrue(actuals.contains(-1337d));
		assertTrue(actuals.contains(-5d));
		assertTrue(actuals.stream().anyMatch(d -> d > -1337 && d < -5));

		assertEquals(3, actuals.size());
	}

	@Test
	public void testInRangeLessThanZero() {
		Set<Double> actuals = Any.doublePrecisionNumber().inRange(-1337, -0.25).generateAllOnce(random);

		assertTrue(actuals.contains(-1337d));
		assertTrue(actuals.contains(-0.25d));
		assertTrue(actuals.stream().anyMatch(d -> d > -1337 && d < -1));
		assertTrue(actuals.stream().anyMatch(d -> d > -1 && d < -0.25));

		assertEquals(4, actuals.size());
	}

	@Test
	public void testInRangeNegativeFractional() {
		Set<Double> actuals = Any.doublePrecisionNumber().inRange(-0.75, -0.25).generateAllOnce(random);

		assertTrue(actuals.contains(-0.75d));
		assertTrue(actuals.contains(-0.25d));
		assertTrue(actuals.stream().anyMatch(d -> d > -0.75 && d < -0.25));

		assertEquals(3, actuals.size());
	}

	@Test
	public void testInRangeStraddleZero() {
		Set<Double> actuals = Any.doublePrecisionNumber().inRange(-1337, 1337).generateAllOnce(random);

		assertTrue(actuals.contains(-1337d));
		assertTrue(actuals.stream().anyMatch(d -> d > -1337 && d <= -1));
		assertTrue(actuals.stream().anyMatch(d -> d > -1 && d < 0));
		assertTrue(actuals.contains(0d));
		assertTrue(actuals.stream().anyMatch(d -> d > 0 && d < 1));
		assertTrue(actuals.stream().anyMatch(d -> d >= 1 && d < 1337));
		assertTrue(actuals.contains(1337d));

		assertEquals(7, actuals.size());
	}

	@Test
	public void testInRangePositiveFractional() {
		Set<Double> actuals = Any.doublePrecisionNumber().inRange(0.25, 0.75).generateAllOnce(random);

		assertTrue(actuals.contains(0.25d));
		assertTrue(actuals.contains(0.75d));
		assertTrue(actuals.stream().anyMatch(d -> d > 0.25 && d < 0.75));

		assertEquals(3, actuals.size());
	}

	@Test
	public void testInRangeGreaterThanZero() {
		Set<Double> actuals = Any.doublePrecisionNumber().inRange(0.25, 1337).generateAllOnce(random);

		assertTrue(actuals.contains(0.25d));
		assertTrue(actuals.contains(1337d));
		assertTrue(actuals.stream().anyMatch(d -> d > 1 && d < 1337));
		assertTrue(actuals.stream().anyMatch(d -> d > 0.25 && d < 1));

		assertEquals(4, actuals.size());
	}

	@Test
	public void testInRangeGreaterThanOne() {
		Set<Double> actuals = Any.doublePrecisionNumber().inRange(5, 1337).generateAllOnce(random);

		assertTrue(actuals.contains(5d));
		assertTrue(actuals.contains(1337d));
		assertTrue(actuals.stream().anyMatch(d -> d > 5 && d < 1337));

		assertEquals(3, actuals.size());
	}

	@Test
	public void testLessThan() {
		Set<Double> actuals = Any.doublePrecisionNumber().lessThan(-5).generateAllOnce(random);

		assertFalse(actuals.contains(-5d));
		assertFalse(actuals.stream().anyMatch(d -> d >= -5));
	}

	@Test
	public void testLessThanOrEqualTo() {
		Set<Double> actuals = Any.doublePrecisionNumber().lessThanOrEqualTo(-5).generateAllOnce(random);

		assertTrue(actuals.contains(-5d));
		assertFalse(actuals.stream().anyMatch(d -> d > -5));
	}

	@Test
	public void testGreaterThan() {
		Set<Double> actuals = Any.doublePrecisionNumber().greaterThan(-5).generateAllOnce(random);

		assertFalse(actuals.contains(-5d));
		assertFalse(actuals.stream().anyMatch(d -> d <= -5));
	}

	@Test
	public void testGreaterThanOrEqualTo() {
		Set<Double> actuals = Any.doublePrecisionNumber().greaterThanOrEqualTo(-5).generateAllOnce(random);

		assertTrue(actuals.contains(-5d));
		assertFalse(actuals.stream().anyMatch(d -> d < -5));
	}

}
