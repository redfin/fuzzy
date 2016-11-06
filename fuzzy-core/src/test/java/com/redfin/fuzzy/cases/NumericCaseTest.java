package com.redfin.fuzzy.cases;

import com.redfin.fuzzy.Any;
import com.redfin.fuzzy.Case;
import com.redfin.fuzzy.Context;
import com.redfin.fuzzy.Generator;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class NumericCaseTest {

	private Random random;

	// TODO: test cases for inclusivitiy of bounds

	@Before
	public void before() {
		random = new Random(123456); // keep tests consistent
	}

	@Test
	public void testDefaultConfig() {
		Case<Integer> subject = Any.integer();
		Set<Function<Random, Integer>> suppliers = subject.getSuppliers();

		assertEquals(3, suppliers.size());
		assertSuppliers(suppliers)
			.expectNegative()
			.expectPositive()
			.expectZero()
		;
	}

	@Test
	public void testLessThanPositive() {
		Case<Integer> subject = Any.integer().lessThan(10);
		Set<Function<Random, Integer>> suppliers = subject.getSuppliers();

		assertEquals(4, suppliers.size());
		assertSuppliers(suppliers)
			.expectNegative()
			.expectPositive()
			.expectZero()
			.expectSpecific(10)
			.expectOfPositive(i -> i <= 10)
		;
	}

	@Test
	public void testLessThanZero() {
		Case<Integer> subject = Any.integer().lessThan(0);
		Set<Function<Random, Integer>> suppliers = subject.getSuppliers();

		assertEquals(2, suppliers.size());
		assertSuppliers(suppliers)
			.expectNegative()
			.expectZero()
			.expectNoPositive()
		;
	}

	@Test
	public void testLessThanNegative() {
		Case<Integer> subject = Any.integer().lessThan(-1000000000);
		Set<Function<Random, Integer>> suppliers = subject.getSuppliers();

		assertEquals(2, suppliers.size());
		assertSuppliers(suppliers)
			.expectNegative()
			.expectNoZero()
			.expectNoPositive()
			.expectSpecific(-1000000000)
			.expectOfNegative(i -> i <= -1000000000)
		;
	}

	@Test
	public void testGreaterThanPositive() {
		Case<Integer> subject = Any.integer().greaterThan(1000000000);
		Set<Function<Random, Integer>> suppliers = subject.getSuppliers();

		assertEquals(2, suppliers.size());
		assertSuppliers(suppliers)
			.expectNoNegative()
			.expectNoZero()
			.expectPositive()
			.expectSpecific(1000000000)
			.expectOfPositive(i -> i >= 1000000000)
		;
	}

	@Test
	public void testGreaterThanZero() {
		Case<Integer> subject = Any.integer().greaterThan(0);
		Set<Function<Random, Integer>> suppliers = subject.getSuppliers();

		assertEquals(2, suppliers.size());
		assertSuppliers(suppliers)
			.expectNoNegative()
			.expectZero()
			.expectPositive()
		;
	}

	@Test
	public void testGreaterThanNegative() {
		Case<Integer> subject = Any.integer().greaterThan(-10);
		Set<Function<Random, Integer>> suppliers = subject.getSuppliers();

		assertEquals(4, suppliers.size());
		assertSuppliers(suppliers)
			.expectNegative()
			.expectZero()
			.expectPositive()
			.expectSpecific(-10)
			.expectOfNegative(i -> i >= -10)
		;
	}

	@Test
	public void testInRangeStraddleZero() {
		Case<Integer> subject = Any.integer().inRange(-10, 10);
		Set<Function<Random, Integer>> suppliers = subject.getSuppliers();

		assertEquals(5, suppliers.size());
		assertSuppliers(suppliers)
			.expectNegative()
			.expectZero()
			.expectPositive()
			.expectSpecific(-10)
			.expectSpecific(10)
			.expectOfNegative(i -> i >= -10)
			.expectOfPositive(i -> i <= 10)
		;
	}

	@Test
	public void testInRangeFullyPositive() {
		Case<Integer> subject = Any.integer().inRange(1000, 1010);
		Set<Function<Random, Integer>> suppliers = subject.getSuppliers();

		List<Integer> actual = suppliers.stream()
			.map(s -> s.apply(random))
			.sorted()
			.collect(Collectors.toList());

		List<Object> expected = Arrays.asList(
			1000,
			integerBetween(1000, 1010),
			1010
		);

		assertEquals(expected, actual);
	}

	@Test
	public void testInRangeFullyNegative() {
		Case<Integer> subject = Any.integer().inRange(-1010, -1000);
		Set<Function<Random, Integer>> suppliers = subject.getSuppliers();

		List<Integer> actual = suppliers.stream()
			.map(s -> s.apply(random))
			.sorted()
			.collect(Collectors.toList());

		List<Object> expected = Arrays.asList(
			-1010,
			integerBetween(-1010, -1000),
			-1000
		);

		assertEquals(expected, actual);
	}

	@Test
	public void testByteNewCase() {
		NumericCase<Byte> subject = Any.byteInteger();
		NumericCase<Byte> newCase = subject.newCase();

		assertNotSame(subject, newCase);
		assertNotNull(newCase);
	}

	@Test
	public void testByteAdd() {
		NumericCase<Byte> subject = Any.byteInteger();
		assertEquals((byte)5, subject.add((byte)3, (byte)2).byteValue());
	}

	@Test
	public void testByteNegation() {
		NumericCase<Byte> subject = Any.byteInteger();
		assertEquals(new Byte((byte)-12), subject.negate((byte)12));
		assertEquals(new Byte((byte)12), subject.negate((byte)-12));
	}

	@Test
	public void testByteAbs() {
		NumericCase<Byte> subject = Any.byteInteger();
		assertEquals(new Byte((byte)12), subject.abs((byte)12));
		assertEquals(new Byte((byte)12), subject.abs((byte)-12));
	}

	@Test
	public void testByteI2T() {
		NumericCase<Byte> subject = Any.byteInteger();
		assertEquals(new Byte((byte)12), subject.i2t(12));
	}

	@Test
	public void testByteLT() {
		NumericCase<Byte> subject = Any.byteInteger();
		assertTrue(subject.lt((byte)12, (byte)123));
		assertFalse(subject.lt((byte)123, (byte)12));
	}

	@Test
	public void testByteRng() {
		NumericCase<Byte> subject = Any.byteInteger();
		assertNotNull(subject.rng(random));
	}

	@Test
	public void testByteRngLessThan() {
		NumericCase<Byte> subject = Any.byteInteger();
		assertTrue(subject.rngLessThan(random, (byte)5) <= (byte)5);
	}

	@Test
	public void testShortNewCase() {
		NumericCase<Short> subject = Any.shortInteger();
		NumericCase<Short> newCase = subject.newCase();

		assertNotSame(subject, newCase);
		assertNotNull(newCase);
	}

	@Test
	public void testShortAdd() {
		NumericCase<Short> subject = Any.shortInteger();
		assertEquals((short)5, subject.add((short)3, (short)2).shortValue());
	}

	@Test
	public void testShortNegation() {
		NumericCase<Short> subject = Any.shortInteger();
		assertEquals(new Short((short)-1234), subject.negate((short)1234));
		assertEquals(new Short((short)1234), subject.negate((short)-1234));
	}

	@Test
	public void testShortAbs() {
		NumericCase<Short> subject = Any.shortInteger();
		assertEquals(new Short((short)1234), subject.abs((short)1234));
		assertEquals(new Short((short)1234), subject.abs((short)-1234));
	}

	@Test
	public void testShortI2T() {
		NumericCase<Short> subject = Any.shortInteger();
		assertEquals(new Short((short)1234), subject.i2t(1234));
	}

	@Test
	public void testShortLT() {
		NumericCase<Short> subject = Any.shortInteger();
		assertTrue(subject.lt((short)1234, (short)1235));
		assertFalse(subject.lt((short)1235, (short)1234));
	}

	@Test
	public void testShortRng() {
		NumericCase<Short> subject = Any.shortInteger();
		assertNotNull(subject.rng(random));
	}

	@Test
	public void testShortRngLessThan() {
		NumericCase<Short> subject = Any.shortInteger();
		assertTrue(subject.rngLessThan(random, (short)1234) <= (short)1234);
	}

	@Test
	public void testLongNewCase() {
		NumericCase<Long> subject = Any.longInteger();
		NumericCase<Long> newCase = subject.newCase();

		assertNotSame(subject, newCase);
		assertNotNull(newCase);
	}

	@Test
	public void testLongAdd() {
		NumericCase<Long> subject = Any.longInteger();
		assertEquals(5L, subject.add(3L, 2L).longValue());
	}

	@Test
	public void testLongNegation() {
		NumericCase<Long> subject = Any.longInteger();
		assertEquals(new Long(-1234L), subject.negate(1234L));
		assertEquals(new Long(1234L), subject.negate(-1234L));
	}

	@Test
	public void testLongAbs() {
		NumericCase<Long> subject = Any.longInteger();
		assertEquals(new Long(1234L), subject.abs(1234L));
		assertEquals(new Long(1234L), subject.abs(-1234L));
	}

	@Test
	public void testLongI2T() {
		NumericCase<Long> subject = Any.longInteger();
		assertEquals(new Long(1234L), subject.i2t(1234));
	}

	@Test
	public void testLongLT() {
		NumericCase<Long> subject = Any.longInteger();
		assertTrue(subject.lt(1234L, 12345L));
		assertFalse(subject.lt(12345L, 1234L));
	}

	@Test
	public void testLongRng() {
		NumericCase<Long> subject = Any.longInteger();
		assertNotNull(subject.rng(random));
	}

	@Test
	public void testLongRngLessThanFullLong() {
		NumericCase<Long> subject = Any.longInteger();
		assertTrue(subject.rngLessThan(random, 1L << 48) <= 1L << 48);
	}

	@Test
	public void testLongRngLessThanInteger() {
		NumericCase<Long> subject = Any.longInteger();
		assertTrue(subject.rngLessThan(random, 0x7FFFFFFFL) <= 0x7FFFFFFFL);
	}

	@Test
	public void testGreaterThanGenerator() {
		Context.init(0);

		List<List<Integer>> actual = new ArrayList<>();
		do {
			Generator<Integer> base = Generator.of(Any.of(-10, 0, 10));
			Generator<Integer> greater = Generator.of(Any.integer().greaterThan(base));

			actual.add(Arrays.asList(base.get(), greater.get()));
		}
		while(Context.next());

		actual.sort((a, b) -> (a.get(0).intValue() != b.get(0).intValue()) ? a.get(0) - b.get(0) : a.get(1) - b.get(1));

		List<List<Object>> expected = Arrays.asList(
			Arrays.asList(-10, -9),
			Arrays.asList(-10, integerGreaterThan(-10)),
			Arrays.asList(0, 1),
			Arrays.asList(0, integerGreaterThan(0)),
			Arrays.asList(10, 11),
			Arrays.asList(10, integerGreaterThan(10))
		);

		assertEquals(expected, actual);

		Context.cleanUp();
	}

	@Test
	public void testLessThanGenerator() {
		Context.init(0);

		List<List<Integer>> actual = new ArrayList<>();
		do {
			Generator<Integer> base = Generator.of(Any.of(-10, 0, 10));
			Generator<Integer> lesser = Generator.of(Any.integer().lessThan(base));

			actual.add(Arrays.asList(base.get(), lesser.get()));
		}
		while(Context.next());

		actual.sort((a, b) -> (a.get(0).intValue() != b.get(0).intValue()) ? a.get(0) - b.get(0) : a.get(1) - b.get(1));

		List<List<Object>> expected = Arrays.asList(
			Arrays.asList(-10, integerLessThan(-10)),
			Arrays.asList(-10, -11),
			Arrays.asList(0, integerLessThan(0)),
			Arrays.asList(0, -1),
			Arrays.asList(10, integerLessThan(10)),
			Arrays.asList(10, 9)
		);

		assertEquals(expected, actual);

		Context.cleanUp();
	}

	@Test
	public void testWithinRangeOfGenerator() {
		Context.init(0);

		List<List<Integer>> actual = new ArrayList<>();
		do {
			Generator<Integer> base = Generator.of(Any.of(-10, 0, 10));
			Generator<Integer> withinRange = Generator.of(Any.integer().within(100).of(base));

			actual.add(Arrays.asList(base.get(), withinRange.get()));
		}
		while(Context.next());

		actual.sort((a, b) -> (a.get(0).intValue() != b.get(0).intValue()) ? a.get(0) - b.get(0) : a.get(1) - b.get(1));

		List<List<Object>> expected = Arrays.asList(
			Arrays.asList(-10, -110),
			Arrays.asList(-10, integerBetween(-109, -11)),
			Arrays.asList(-10, -10),
			Arrays.asList(-10, integerBetween(-9, 89)),
			Arrays.asList(-10, 90),

			Arrays.asList(0, -100),
			Arrays.asList(0, integerBetween(-99, -1)),
			Arrays.asList(0, 0),
			Arrays.asList(0, integerBetween(1, 99)),
			Arrays.asList(0, 100),

			Arrays.asList(10, -90),
			Arrays.asList(10, integerBetween(-89, 9)),
			Arrays.asList(10, 10),
			Arrays.asList(10, integerBetween(11, 109)),
			Arrays.asList(10, 110)
		);

		assertEquals(expected, actual);

		Context.cleanUp();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testWithinRangeOfGeneratorWithZeroRange() {
		Any.integer().within(0);
	}

	private SupplierExpectations assertSuppliers(Set<Function<Random, Integer>> suppliers) {
		SupplierExpectations res = new SupplierExpectations();

		for(Function<Random, Integer> supplier : suppliers) {
			int i = supplier.apply(random);

			if(i < 0) {
				assertNull("Expected at most two negative values", res.negative2);
				if(res.negative == null) res.negative = i; else res.negative2 = i;
			}
			else if(i > 0) {
				assertNull("Expected at most two positive values", res.positive2);
				if(res.positive == null) res.positive = i; else res.positive2 = i;
			}
			else {
				assertNull("Expected at most one zero value", res.zero);
				res.zero = i;
			}
		}

		return res;
	}

	private static class SupplierExpectations {
		Integer negative, negative2;
		Integer positive, positive2;
		Integer zero;

		SupplierExpectations expectSpecific(int expected) {
			assertTrue(
				(negative != null && negative == expected) ||
				(negative2 != null && negative2 == expected) ||
				(positive != null && positive == expected) ||
				(positive2 != null && positive2 == expected)
			);
			return this;
		}

		SupplierExpectations expectNegative() {
			assertNotNull("Expected a negative value.", negative);
			return this;
		}

		SupplierExpectations expectPositive() {
			assertNotNull("Expected a positive value.", positive);
			return this;
		}

		SupplierExpectations expectZero() {
			assertNotNull("Expected a zero value.", zero);
			return this;
		}

		SupplierExpectations expectNoNegative() {
			assertNull("Expected no negative value; instead got " + negative, negative);
			return this;
		}

		SupplierExpectations expectNoPositive() {
			assertNull("Expected no positive value; instead got " + positive, positive);
			return this;
		}

		SupplierExpectations expectNoZero() {
			assertNull("Expected no zero value; instead got " + zero, zero);
			return this;
		}

		SupplierExpectations expectOfNegative(Predicate<Integer> predicate) {
			assertTrue("Expected negative {" + negative + "} to match predicate", predicate.test(negative));
			assertTrue("Expected negative {" + negative2 + "} to match predicate", predicate.test(negative2));
			return this;
		}

		SupplierExpectations expectOfPositive(Predicate<Integer> predicate) {
			assertTrue("Expected positive {" + positive + "} to match predicate", predicate.test(positive));
			assertTrue("Expected positive {" + positive2 + "} to match predicate", predicate.test(positive2));
			return this;
		}

	}

	private static Object integerGreaterThan(int i) {
		return new Object() {
			@Override public boolean equals(Object obj) { return obj instanceof Integer && (int)obj > i; }
			@Override public int hashCode() { return i; }
			@Override public String toString() { return "> " + i; }
		};
	}

	private static Object integerLessThan(int i) {
		return new Object() {
			@Override public boolean equals(Object obj) { return obj instanceof Integer && (int)obj < i; }
			@Override public int hashCode() { return i; }
			@Override public String toString() { return "< " + i; }
		};
	}

	private static Object integerBetween(int low, int high) {
		return new Object() {
			@Override public boolean equals(Object obj) { return obj instanceof Integer && (int)obj >= low && (int)obj <= high; }
			@Override public int hashCode() { return low; }
			@Override public String toString() { return low + " ≤ i ≤ " + high; }
		};
	}

}
