package com.redfin.fuzzy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.Test;

public class ContextTest {

	@After
	public void after() {
		Context.cleanUp();
	}

	@Test
	public void testInitReinitialized() {
		Context.init();
		try {
			Context.init();
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
		Context.init();
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
		Context.init();
		assertNotNull(Context.getUnlocked());
	}

	@Test
	public void testCleanUp() {
		Context.init();
		assertNotNull(Context.getUnlocked());
		Context.cleanUp();
		Context.init();
		assertNotNull(Context.getUnlocked());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testRegisterLocked() {
		Context.init();
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
		Context.init();
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
		Context.init();

		Set<String> actuals = new HashSet<>();
		do {
			Generator<String> myString = Generator.of(Any.of("One", "Two", "Three"));
			actuals.add(myString.get());
		} while(Context.next());

		assertEquals(Util.setOf("One", "Two", "Three"), actuals);
	}

	@Test
	public void testMultiVariableIntegration() {
		Context.init();

		Set<String> actuals = new HashSet<>();
		do {
			Generator<String> a = Generator.of(Any.of("A", "B", "C"));
			Generator<String> b = Generator.of(Any.of("1", "2", "3"));

			actuals.add(a.get() + b.get());
		} while(Context.next());

		assertEquals(
			Util.setOf("A1", "A2", "A3", "B1", "B2", "B3", "C1", "C2", "C3"),
			actuals
		);
	}

	@Test(expected = IllegalStateException.class)
	public void testNextUninitialized() {
		Context.next();
	}

	@Test
	public void testNextWithNoGenerators() {
		Context.init();
		assertFalse(Context.next());
	}

	@Test
	public void testReport() {
		Context.init();

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
	public void testReportUninitialized() {
		assertEquals("", Context.report());
	}

	@Test
	public void testReportUnlocked() {
		Context.init();
		assertEquals("", Context.report());
	}

	@Test
	public void testReportFullyIterated() {
		Context.init();

		Generator.of(Literal.value("A")).get();
		Context.next();

		assertEquals("", Context.report());
	}

	public void blan() {


		Generator<String> myString = Generator.of(Any.string()
			.withOnlyHexChars()
			.withLengthOf(Any.of(0, 10, 20))
		);

	}

}
