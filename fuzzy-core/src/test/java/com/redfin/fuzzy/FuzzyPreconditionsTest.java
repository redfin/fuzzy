package com.redfin.fuzzy;

import org.junit.Test;

import static org.junit.Assert.assertSame;

public class FuzzyPreconditionsTest {

	@Test
	public void testCheckNotNullMessageWithNonnull() {
		Object expected = new Object();
		assertSame(expected, FuzzyPreconditions.checkNotNull("", expected));
	}

	@Test(expected = NullPointerException.class)
	public void testCheckNotNullMessageWithNull() {
		FuzzyPreconditions.checkNotNull("my message", null);
	}

	@Test
	public void testConstructorForCoverage() { new FuzzyPreconditions(); }

}
