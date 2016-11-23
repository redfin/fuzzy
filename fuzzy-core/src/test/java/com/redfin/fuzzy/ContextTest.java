package com.redfin.fuzzy;

import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class ContextTest {

	@After
	public void after() {
		Context.cleanUp();
	}

	@Test
	public void testInitReinitialized() {
		Context.init(0);
		try {
			Context.init(0);
			fail();
		}
		catch(IllegalStateException e) {
			// expected
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testGetUnlockedNotInitialized() {
		Context.getUnlocked();
	}

	@Test
	public void testGetUnlockedLocked() {
		Context.init(0);
		Context.getUnlocked().lock();

		try {
			Context.getUnlocked();
			fail();
		}
		catch(IllegalStateException e) {
			// expected
		}
	}

	@Test
	public void testInitGetUnlocked() {
		Context.init(0);
		assertNotNull(Context.getUnlocked());
	}

	@Test
	public void testCleanUp() {
		Context.init(0);
		assertNotNull(Context.getUnlocked());
		Context.cleanUp();
		Context.init(0);
		assertNotNull(Context.getUnlocked());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testRegisterLocked() {
		Context.init(0);
		Context c = Context.getUnlocked();

		c.lock();

		try {
			c.register(mock(Generator.class), new Case[] { Any.string() });
			fail();
		}
		catch(IllegalStateException e) {
			// expected
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testRegisterDuplicate() {
		Context.init(0);
		Context c = Context.getUnlocked();

		Generator g = new Generator(c);
		c.register(g, new Case[] { Any.string() });

		try {
			c.register(g, new Case[] { Any.string() });
			fail();
		}
		catch(IllegalStateException e) {
			// expected
		}
	}

	@Test
	public void testBasicIntegration() {
		Context.init(0);

		Set<String> actuals = new HashSet<>();
		do {
			Generator<String> myString = Generator.of(Any.of("One", "Two", "Three"));
			actuals.add(myString.get());
		} while(Context.next());

		assertEquals(FuzzyUtil.setOf("One", "Two", "Three"), actuals);
	}

	@Test
	public void testMultiVariableIntegration() {
		Context.init(0);

		Set<String> actuals = new HashSet<>();
		do {
			Generator<String> a = Generator.of(Any.of("A", "B", "C"));
			Generator<String> b = Generator.of(Any.of("1", "2", "3"));

			actuals.add(a.get() + b.get());
		} while(Context.next());

		assertEquals(
			FuzzyUtil.setOf("A1", "A2", "A3", "B1", "B2", "B3", "C1", "C2", "C3"),
			actuals
		);
	}

	@Test(expected = IllegalStateException.class)
	public void testNextUninitialized() {
		Context.next();
	}

	@Test
	public void testNextWithNoGenerators() {
		Context.init(0);
		assertFalse(Context.next());
	}

	@Test
	public void testReport() {
		Context.init(0);

		Generator<String> variableA = Generator.named("variableA").of(Literal.value("Hello, "));
		Generator<String> variableB = Generator.named("variableB").of(Literal.value("World!"));

		assertEquals("Hello, ", variableA.get());
		assertNotNull(variableB); // just to prevent warnings about variableB being unused

		String report = Context.report();

		assertEquals(
			"  \"Hello, \" from generator variableA\n",
			report
		);
	}

	@Test
	public void testReportWithCustomDescription() {
		Context.init(0);

		Generator<String> variableA = Generator.named("variableA").of(() -> FuzzyUtil.setOf(
			new Subcase<String>() {
				@Override
				public String generate(Random random) { return "I'm a subcase!"; }
				@Override
				public void describeTo(StringBuilder sink, String value) {
					assertEquals("I'm a subcase!", value);
					sink.append("<I'm a description!>");
				}
			}
		));

		assertEquals("I'm a subcase!", variableA.get());

		String report = Context.report();
		assertEquals(
			"  <I'm a description!> from generator variableA\n",
			report
		);
	}

	@Test
	public void testReportUninitialized() {
		assertEquals("", Context.report());
	}

	@Test
	public void testReportUnlocked() {
		Context.init(0);
		assertEquals("", Context.report());
	}

	@Test
	public void testReportFullyIterated() {
		Context.init(0);

		Generator.of(Literal.value("A")).get();
		Context.next();

		assertEquals("", Context.report());
	}

	@Test
	public void testRandomDeterminism() {
		Context.init(0);

		List<Integer> firstValues = new ArrayList<>();
		do {
			Generator<Integer> r = Generator.of(Random::nextInt, Random::nextInt, Random::nextInt);
			firstValues.add(r.get());
		}
		while(Context.next());

		assertEquals(3, firstValues.size());

		Context.cleanUp();
		Context.init(0);

		List<Integer> secondValues = new ArrayList<>();
		do {
			Generator<Integer> r = Generator.of(Random::nextInt, Random::nextInt, Random::nextInt);
			secondValues.add(r.get());
		}
		while(Context.next());

		assertEquals(firstValues, secondValues);
	}

	@Test
	public void testRandomDeterminismWithNewSeed() {
		Context.init(0);

		List<Integer> firstValues = new ArrayList<>();
		do {
			Generator<Integer> r = Generator.of(Random::nextInt, Random::nextInt, Random::nextInt);
			firstValues.add(r.get());
		}
		while(Context.next());

		assertEquals(3, firstValues.size());

		Context.cleanUp();
		Context.init(1); // different seed this time

		List<Integer> secondValues = new ArrayList<>();
		do {
			Generator<Integer> r = Generator.of(Random::nextInt, Random::nextInt, Random::nextInt);
			secondValues.add(r.get());
		}
		while(Context.next());

		assertEquals(3, secondValues.size());
		assertNotEquals(firstValues, secondValues);
	}

	@Test
	public void testValuesForCurrentIteration() {
		Context.init(0);

		Generator<String> string = Generator.of("Hello");
		Generator<Integer> integer = Generator.of(5);

		string.get();
		integer.get();

		Map<Generator, Object> expected = new HashMap<>();
		expected.put(string, "Hello");
		expected.put(integer, 5);

		assertEquals(expected, Context.valuesForCurrentIteration());

		Context.cleanUp();
	}

	@Test
	public void testValuesForCurrentIterationUninitialized() {
		Context.cleanUp();
		try {
			Context.valuesForCurrentIteration();
			fail("Expected IllegalStateException");
		}
		catch(IllegalStateException e) {
			// expected
		}
	}

}
