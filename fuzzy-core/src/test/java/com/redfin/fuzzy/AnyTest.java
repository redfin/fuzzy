package com.redfin.fuzzy;

import org.junit.Before;
import org.junit.Test;

import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AnyTest {

	Random random;

	@Before
	public void before() {
		random = new Random(12345); // keep tests consistent
	}

	@Test
	public void testOfFunctions() {
		Case<Integer> c = Any.of(r -> 1, r -> 2, r -> 3);
		assertEquals(Util.setOf(1, 2, 3), c.generateAllOnce(random));
	}

	@Test
	public void testNullableOfSupplier() {
		Case<Integer> c = Any.nullableOf(Any::integer);
		assertTrue(c.generateAllOnce(random).contains(null));
	}

	@Test
	public void testPositiveByteInteger() {
		Set<Byte> bytes = Any.positiveByteInteger().generateAllOnce(random);
		assertFalse(bytes.stream().anyMatch(b -> b < 0));
	}

	@Test
	public void testPositiveNonZeroByteInteger() {
		Set<Byte> bytes = Any.positiveNonZeroByteInteger().generateAllOnce(random);
		assertFalse(bytes.contains((byte)0));
		assertFalse(bytes.stream().anyMatch(b -> b < 0));
	}

	@Test
	public void testNegativeByteInteger() {
		Set<Byte> bytes = Any.negativeByteInteger().generateAllOnce(random);
		assertFalse(bytes.contains((byte)0));
		assertFalse(bytes.stream().anyMatch(b -> b > 0));
	}

	@Test
	public void testPositiveShortInteger() {
		Set<Short> shorts = Any.positiveShortInteger().generateAllOnce(random);
		assertFalse(shorts.stream().anyMatch(s -> s < 0));
	}

	@Test
	public void testPositiveNonZeroShortInteger() {
		Set<Short> shorts = Any.positiveNonZeroShortInteger().generateAllOnce(random);
		assertFalse(shorts.contains((short)0));
		assertFalse(shorts.stream().anyMatch(s -> s < 0));
	}

	@Test
	public void testNegativeShortInteger() {
		Set<Short> shorts = Any.negativeShortInteger().generateAllOnce(random);
		assertFalse(shorts.contains((short)0));
		assertFalse(shorts.stream().anyMatch(s -> s > 0));
	}

	@Test
	public void testPositiveInteger() {
		Set<Integer> ints = Any.positiveInteger().generateAllOnce(random);
		assertFalse(ints.stream().anyMatch(i -> i < 0));
	}

	@Test
	public void testPositiveNonZeroInteger() {
		Set<Integer> ints = Any.positiveNonZeroInteger().generateAllOnce(random);
		assertFalse(ints.contains(0));
		assertFalse(ints.stream().anyMatch(i -> i < 0));
	}

	@Test
	public void testNegativeInteger() {
		Set<Integer> ints = Any.negativeInteger().generateAllOnce(random);
		assertFalse(ints.contains(0));
		assertFalse(ints.stream().anyMatch(i -> i > 0));
	}

	@Test
	public void testPositiveLongInteger() {
		Set<Long> longs = Any.positiveLongInteger().generateAllOnce(random);
		assertFalse(longs.stream().anyMatch(l -> l < 0));
	}

	@Test
	public void testPositiveNonZeroLongInteger() {
		Set<Long> longs = Any.positiveNonZeroLongInteger().generateAllOnce(random);
		assertFalse(longs.contains(0L));
		assertFalse(longs.stream().anyMatch(l -> l < 0));
	}

	@Test
	public void testNegativeLongInteger() {
		Set<Long> longs = Any.negativeLongInteger().generateAllOnce(random);
		assertFalse(longs.contains(0L));
		assertFalse(longs.stream().anyMatch(l -> l > 0));
	}

	@Test
	public void testConstructorForCoverage() {
		new Any();
	}

}
