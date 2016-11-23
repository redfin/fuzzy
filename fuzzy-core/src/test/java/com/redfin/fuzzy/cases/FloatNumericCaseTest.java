package com.redfin.fuzzy.cases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.redfin.fuzzy.Any;
import java.util.Random;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class FloatNumericCaseTest {

	private Random random;

	@Before
	public void before() {
		random = new Random(12345); // keep tests consistent
	}

	@Test
	public void testBasicCase() {
		Set<Float> actuals = Any.singlePrecisionNumber().generateAllOnce(random);

		assertTrue(actuals.contains(FloatNumericCase.MIN_GENERATED));
		assertTrue(actuals.stream().anyMatch(d -> d <= -1));
		assertTrue(actuals.stream().anyMatch(d -> d > -1 && d < 0));
		assertTrue(actuals.contains(0f));
		assertTrue(actuals.stream().anyMatch(d -> d > 0 && d < 1));
		assertTrue(actuals.stream().anyMatch(d -> d >= 1));
		assertTrue(actuals.contains(FloatNumericCase.MAX_GENERATED));

		assertEquals(7, actuals.size());
	}

	@Test
	public void testExcludingZero() {
		Set<Float> actuals = Any.singlePrecisionNumber().excluding(0f).generateAllOnce(random);

		assertFalse(actuals.contains(0f));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInRangeIllegalArgument() {
		Any.singlePrecisionNumber().inRange(1, -1);
	}

	@Test
	public void testInRangeLessThanNegativeOne() {
		Set<Float> actuals = Any.singlePrecisionNumber().inRange(-1337, -918).generateAllOnce(random);

		assertTrue(actuals.contains(-1337f));
		assertTrue(actuals.contains(-918f));
		assertTrue(actuals.stream().anyMatch(d -> d > -1337 && d < -918));

		assertEquals(3, actuals.size());
	}

	@Test
	public void testInRangeLessThanZero() {
		Set<Float> actuals = Any.singlePrecisionNumber().inRange(-1337, -0.25f).generateAllOnce(random);

		assertTrue(actuals.contains(-1337f));
		assertTrue(actuals.contains(-0.25f));
		assertTrue(actuals.stream().anyMatch(d -> d > -1337 && d < -1));
		assertTrue(actuals.stream().anyMatch(d -> d > -1 && d < -0.25));

		assertEquals(4, actuals.size());
	}

	@Test
	public void testInRangeNegativeFractional() {
		Set<Float> actuals = Any.singlePrecisionNumber().inRange(-0.75f, -0.25f).generateAllOnce(random);

		assertTrue(actuals.contains(-0.75f));
		assertTrue(actuals.contains(-0.25f));
		assertTrue(actuals.stream().anyMatch(d -> d > -0.75 && d < -0.25));

		assertEquals(3, actuals.size());
	}

	@Test
	public void testInRangeStraddleZero() {
		Set<Float> actuals = Any.singlePrecisionNumber().inRange(-1337, 1337).generateAllOnce(random);

		assertTrue(actuals.contains(-1337f));
		assertTrue(actuals.stream().anyMatch(d -> d > -1337 && d <= -1));
		assertTrue(actuals.stream().anyMatch(d -> d > -1 && d < 0));
		assertTrue(actuals.contains(0f));
		assertTrue(actuals.stream().anyMatch(d -> d > 0 && d < 1));
		assertTrue(actuals.stream().anyMatch(d -> d >= 1 && d < 1337));
		assertTrue(actuals.contains(1337f));

		assertEquals(7, actuals.size());
	}

	@Test
	public void testInRangePositiveFractional() {
		Set<Float> actuals = Any.singlePrecisionNumber().inRange(0.25f, 0.75f).generateAllOnce(random);

		assertTrue(actuals.contains(0.25f));
		assertTrue(actuals.contains(0.75f));
		assertTrue(actuals.stream().anyMatch(d -> d > 0.25 && d < 0.75));

		assertEquals(3, actuals.size());
	}

	@Test
	public void testInRangeGreaterThanZero() {
		Set<Float> actuals = Any.singlePrecisionNumber().inRange(0.25f, 1337).generateAllOnce(random);

		assertTrue(actuals.contains(0.25f));
		assertTrue(actuals.contains(1337f));
		assertTrue(actuals.stream().anyMatch(d -> d > 1 && d < 1337));
		assertTrue(actuals.stream().anyMatch(d -> d > 0.25 && d < 1));

		assertEquals(4, actuals.size());
	}

	@Test
	public void testInRangeGreaterThanOne() {
		Set<Float> actuals = Any.singlePrecisionNumber().inRange(918, 1337).generateAllOnce(random);

		assertTrue(actuals.contains(918f));
		assertTrue(actuals.contains(1337f));
		assertTrue(actuals.stream().anyMatch(d -> d > 918 && d < 1337));

		assertEquals(3, actuals.size());
	}

	@Test
	public void testLessThan() {
		Set<Float> actuals = Any.singlePrecisionNumber().lessThan(-5).generateAllOnce(random);

		assertFalse(actuals.contains(-5f));
		assertFalse(actuals.stream().anyMatch(d -> d >= -5));
	}

	@Test
	public void testLessThanOrEqualTo() {
		Set<Float> actuals = Any.singlePrecisionNumber().lessThanOrEqualTo(-5).generateAllOnce(random);

		assertTrue(actuals.contains(-5f));
		assertFalse(actuals.stream().anyMatch(d -> d > -5));
	}

	@Test
	public void testGreaterThan() {
		Set<Float> actuals = Any.singlePrecisionNumber().greaterThan(-5).generateAllOnce(random);

		assertFalse(actuals.contains(-5f));
		assertFalse(actuals.stream().anyMatch(d -> d <= -5));
	}

	@Test
	public void testGreaterThanOrEqualTo() {
		Set<Float> actuals = Any.singlePrecisionNumber().greaterThanOrEqualTo(-5).generateAllOnce(random);

		assertTrue(actuals.contains(-5f));
		assertFalse(actuals.stream().anyMatch(d -> d < -5));
	}

}
