package com.redfin.fuzzy;

import org.junit.Test;

import static org.junit.Assert.assertSame;

public class PreconditionsTest {

	@Test
	public void testCheckNotNullMessageWithNonnull() {
		Object expected = new Object();
		assertSame(expected, Preconditions.checkNotNull("", expected));
	}

	@Test(expected = NullPointerException.class)
	public void testCheckNotNullMessageWithNull() {
		Preconditions.checkNotNull("my message", null);
	}

	@Test
	public void testConstructorForCoverage() { new Preconditions(); }

}
