package com.redfin.fuzzy.cases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.redfin.fuzzy.Any;
import com.redfin.fuzzy.Case;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.Before;
import org.junit.Test;

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

		assertEquals(3, suppliers.size());
		assertSuppliers(suppliers)
			.expectNegative()
			.expectPositive()
			.expectZero()
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

		assertEquals(1, suppliers.size());
		assertSuppliers(suppliers)
			.expectNegative()
			.expectNoZero()
			.expectNoPositive()
			.expectOfNegative(i -> i <= -1000000000)
		;
	}

	@Test
	public void testGreaterThanPositive() {
		Case<Integer> subject = Any.integer().greaterThan(1000000000);
		Set<Function<Random, Integer>> suppliers = subject.getSuppliers();

		assertEquals(1, suppliers.size());
		assertSuppliers(suppliers)
			.expectNoNegative()
			.expectNoZero()
			.expectPositive()
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

		assertEquals(3, suppliers.size());
		assertSuppliers(suppliers)
			.expectNegative()
			.expectZero()
			.expectPositive()
			.expectOfNegative(i -> i >= -10)
		;
	}

	@Test
	public void testInRangeStraddleZero() {
		Case<Integer> subject = Any.integer().inRange(-10, 10);
		Set<Function<Random, Integer>> suppliers = subject.getSuppliers();

		assertEquals(3, suppliers.size());
		assertSuppliers(suppliers)
			.expectNegative()
			.expectZero()
			.expectPositive()
			.expectOfNegative(i -> i >= -10)
			.expectOfPositive(i -> i <= 10)
		;
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

	private SupplierExpectations assertSuppliers(Set<Function<Random, Integer>> suppliers) {
		SupplierExpectations res = new SupplierExpectations();

		for(Function<Random, Integer> supplier : suppliers) {
			int i = supplier.apply(random);
			if(i < 0) {
				assertNull("Expected at most one negative value", res.negative);
				res.negative = i;
			}
			else if(i > 0) {
				assertNull("Expected at most one positive value", res.positive);
				res.positive = i;
			}
			else {
				assertNull("Expected at most one zero value", res.zero);
				res.zero = i;
			}
		}

		return res;
	}

	private static class SupplierExpectations {
		Integer negative;
		Integer positive;
		Integer zero;

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
			return this;
		}

		SupplierExpectations expectOfPositive(Predicate<Integer> predicate) {
			assertTrue("Expected positive {" + positive + "} to match predicate", predicate.test(positive));
			return this;
		}

	}

}
