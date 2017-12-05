package com.redfin.fuzzy;

import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class CaseTest {

	@Test
	public void testOr() {
		Case<Integer> c1 = () -> Collections.singleton(r -> 1);
		Case<Integer> c2 = () -> Collections.singleton(r -> 2);

		Case<Integer> subject = c1.or(c2);

		assertEquals(FuzzyUtil.setOf(1, 2), subject.generateAllOnce());
	}

	@Test
	public void testOrNull() {
		Case<Integer> base = () -> Collections.singleton(r -> 1);
		Case<Integer> subject = base.orNull();

		assertEquals(FuzzyUtil.setOf(1, null), subject.generateAllOnce());
	}

	@Test
	public void testExcludingIterable() {
		AtomicInteger i = new AtomicInteger(0);

		Case<Integer> subject = () -> Collections.singleton(r -> i.incrementAndGet());
		subject = subject.excluding(1);

		assertEquals(Integer.valueOf(2), subject.generateAnyOnce());
	}

	@Test(expected = IllegalStateException.class)
	public void testResolveAnyOnceNullSuppliers() {
		Case<Integer> subject = () -> null;
		subject.generateAnyOnce();
	}

	@Test(expected = IllegalStateException.class)
	public void testResolveAnyOnceEmptySuppliers() {
		Case<Integer> subject = Collections::emptySet;
		subject.generateAnyOnce();
	}

	@Test(expected = IllegalStateException.class)
	public void testResolveAllOnceNullSuppliers() {
		Case<Integer> subject = () -> null;
		subject.generateAllOnce();
	}

	@Test(expected = IllegalStateException.class)
	public void testResolveAllOnceEmptySuppliers() {
		Case<Integer> subject = Collections::emptySet;
		subject.generateAllOnce();
	}

	@Test
	public void testMap() {
		Case<Integer> subject = Any.of(1, 2, 3).map(i -> i * 2);
		assertEquals(FuzzyUtil.setOf(2, 4, 6), subject.generateAllOnce());
	}

}
