package com.redfin.fuzzy.pairwise;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

public class PairTest {

	private Pair subject;

	@Before
	public void before() {
		Param p1 = new Param(0, Arrays.asList(1, 2));
		Param p2 = new Param(1, Arrays.asList(3, 4));

		subject = new Pair(p1.values.get(0), p2.values.get(0));
	}

	@Test
	public void testEqualsSelf() {
		assertEquals(subject, subject);
	}

	@Test
	public void testEqualsNonPair() {
		assertNotEquals(subject, "Hello");
	}

}
