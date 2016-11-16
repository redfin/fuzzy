package com.redfin.fuzzy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class GeneratorTest {

	@Before
	public void before() {
		Context.init(12345); // keep tests consistent
	}

	@After
	public void after() {
		Context.cleanUp();
	}

	@Test
	public void testOfLiterals() {
		Set<Integer> actuals = new HashSet<>();

		do {
			Generator<Integer> ints = Generator.of(1, 2, 3);
			actuals.add(ints.get());
		}
		while(Context.next());

		assertEquals(FuzzyUtil.setOf(1, 2, 3), actuals);
	}

	@Test
	public void testOfCases() {
		Set<Integer> actuals = new HashSet<>();

		do {
			Generator<Integer> ints = Generator.ofCases(Literal.value(1), Literal.value(2), Literal.value((3)));
			actuals.add(ints.get());
		}
		while(Context.next());

		assertEquals(FuzzyUtil.setOf(1, 2, 3), actuals);
	}

	@Test
	public void testBuilderOfCases() {
		Set<Integer> actuals = new HashSet<>();

		do {
			Generator<Integer> ints = Generator.named("g").ofCases(Literal.value(1), Literal.value(2), Literal.value((3)));
			actuals.add(ints.get());
		}
		while(Context.next());

		assertEquals(FuzzyUtil.setOf(1, 2, 3), actuals);
	}

	@Test
	public void testCompareTo() {
		Generator<Integer> a = Generator.named("a").of(1);
		Generator<Integer> b = Generator.named("b").of(2);
		Generator<Integer> c = Generator.named("c").of(3);

		List<Generator> actuals = new ArrayList<>();
		actuals.add(b);
		actuals.add(a);
		actuals.add(c);

		Collections.sort(actuals);

		assertSame(a, actuals.get(0));
		assertSame(b, actuals.get(1));
		assertSame(c, actuals.get(2));
	}

}
