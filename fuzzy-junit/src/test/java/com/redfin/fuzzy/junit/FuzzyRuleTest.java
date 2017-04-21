package com.redfin.fuzzy.junit;

import static org.junit.Assert.*;

import com.redfin.fuzzy.Generator;
import com.redfin.fuzzy.FuzzyUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class FuzzyRuleTest {

	@Test
	public void testBasicIntegration() throws Throwable {
		FuzzyRule subject = FuzzyRule.DEFAULT;
		Set<String> actuals = new HashSet<>();

		subject.apply(
			new Statement() {
				@Override
				public void evaluate() throws Throwable {
					Generator<String> myString = Generator.of("A", "B", "C");
					Generator<Integer> myInt = Generator.of(1, 2, 3);

					actuals.add(myString.get() + myInt.get());
				}
			},
			Description.EMPTY
		).evaluate();

		assertEquals(
			FuzzyUtil.setOf("A1", "A2", "A3", "B1", "B2", "B3", "C1", "C2", "C3"),
			actuals
		);
	}

	@Test
	public void testFailingIntegration() throws Throwable {
		FuzzyRule subject = FuzzyRule.DEFAULT;

		Statement s = subject.apply(
			new Statement() {
				@Override
				public void evaluate() throws Throwable {
					Generator<Integer> myInt = Generator.named("myInt").of(1, 2, 3, 4);
					Generator<String> myString = Generator.named("myString").of("A", "B", "C", "D");

					assertFalse(myInt.get() == 3 && "C".equals(myString.get()));
				}
			},
			Description.EMPTY
		);

		try {
			s.evaluate();
			fail();
		}
		catch(Exception e) {
			// expected
			assertTrue(e.getMessage().contains("AssertionError"));
			assertTrue(e.getMessage().contains("myInt"));
			assertTrue(e.getMessage().contains("myString"));
			assertTrue(e.getMessage().contains("3"));
			assertTrue(e.getMessage().contains("\"C\""));
		}
	}

	@Test
	public void testDeferredFailureIntegration() throws Throwable {
		FuzzyRule subject = FuzzyRule.custom().deferringFailures().build();
		Set<String> actuals = new HashSet<>();

		Statement s = subject.apply(
			new Statement() {
				@Override
				public void evaluate() throws Throwable {
					Generator<Integer> myInt = Generator.named("myInt").of(1, 2, 3);
					Generator<String> myString = Generator.named("myString").of("A", "B", "C");

					actuals.add(myString.get() + myInt.get());
					assertFalse(myInt.get() == 3 && "C".equals(myString.get()));
				}
			},
			Description.EMPTY
		);

		try {
			s.evaluate();
			fail();
		}
		catch(Exception e) {
			// expected
			assertEquals(
				FuzzyUtil.setOf("A1", "A2", "A3", "B1", "B2", "B3", "C1", "C2", "C3"),
				actuals
			);
		}
	}

	@Test
	public void testTooManyIterationsNotFailing() throws Throwable {
		FuzzyRule subject = FuzzyRule.custom()
			.withMaxIterations(2)
			.withFailAfterMaxIterations(false)
			.build();

		int[] i = new int[] { 0 };

		Statement s = subject.apply(
			new Statement() {
				@Override
				public void evaluate() throws Throwable {
					Generator<Integer> myInt = Generator.of(1, 2, 3, 4);

					myInt.get();
					i[0]++;
				}
			},
			Description.EMPTY
		);

		s.evaluate();
		assertEquals(2, i[0]);
	}

	@Test
	public void testTooManyIterations() throws Throwable {
		FuzzyRule subject = FuzzyRule.custom()
			.withMaxIterations(2)
			.build();

		int[] i = new int[] { 0 };

		Statement s = subject.apply(
			new Statement() {
				@Override
				public void evaluate() throws Throwable {
					Generator<Integer> myInt = Generator.of(1, 2, 3, 4);

					myInt.get();
					i[0]++;
				}
			},
			Description.EMPTY
		);

		try {
			s.evaluate();
			fail();
		}
		catch(IllegalStateException e) {
			// expected
			assertEquals(2, i[0]);
		}
	}

	@Test
	public void testFloatsInterruptedExceptionImmediately() throws Throwable {
		FuzzyRule subject = FuzzyRule.REPORTING_ALL_FAILURES;
		int[] count = new int[] { 0 };

		Statement s = subject.apply(
			new Statement() {
				@Override
				public void evaluate() throws Throwable {
					Generator.of(1, 2, 3, 4).get();
					count[0]++;

					throw new InterruptedException();
				}
			},
			Description.EMPTY
		);

		try {
			s.evaluate();
			fail();
		}
		catch(InterruptedException e) {
			// expected
			assertEquals(1, count[0]);
		}
	}

	@Test
	public void testFloatsErrorsImmediately() throws Throwable {
		FuzzyRule subject = FuzzyRule.REPORTING_ALL_FAILURES;
		int[] count = new int[] { 0 };

		Statement s = subject.apply(
			new Statement() {
				@Override
				public void evaluate() throws Throwable {
					Generator.of(1, 2, 3, 4).get();
					count[0]++;

					throw new StackOverflowError();
				}
			},
			Description.EMPTY
		);

		try {
			s.evaluate();
			fail();
		}
		catch(StackOverflowError e) {
			// expected
			assertEquals(1, count[0]);
		}
	}

	@Test
	public void testEachSubcaseAtLeastOnce() throws Throwable {
		FuzzyRule subject = FuzzyRule.EACH_SUBCASE_AT_LEAST_ONCE;

		int[] count = new int[] { 0 };

		Statement s = subject.apply(
			new Statement() {
				@Override
				public void evaluate() throws Throwable {
					Generator<Integer> a = Generator.of(1, 2, 3, 4);
					Generator<Integer> b = Generator.of(5, 6);

					a.get();

					count[0]++;
				}
			},
			Description.EMPTY
		);

		s.evaluate();

		assertEquals(4, count[0]);
	}

}
