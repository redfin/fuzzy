package com.redfin.fuzzy.junit;

import com.redfin.fuzzy.CaseCompositionMode;
import com.redfin.fuzzy.Context;
import com.redfin.fuzzy.FuzzyPreconditions;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A JUnit {@link TestRule} that automatically runs each test in a configured suite enough times to ensure all subcases
 * of that test's {@linkplain com.redfin.fuzzy.Generator generators} are executed.
 *
 * <pre><code>
 * public class MyTestSuite {
 *
 *   public final @Rule TestRule fuzzyRule = FuzzyRule.DEFAULT;
 *
 *   &#064;Test
 *   public void test() {
 *     Generator&lt;Integer&gt; generatorA = Generator.of(1, 2, 3);
 *     Generator&lt;String&gt; generatorB = Generator.of("A", "B");
 *
 *     System.out.println(generatorA.get() + "-" + generatorB.get());
 *   }
 *
 * }
 * </code></pre>
 *
 * <p>When the previous example is run, it will result in the following console output:
 * </p>
 *
 * <pre>
 * 1-A
 * 1-B
 * 2-A
 * 2-B
 * 3-A
 * 3-B
 * </pre>
 *
 * @see #DEFAULT
 * @see #EACH_SUBCASE_AT_LEAST_ONCE
 * @see #REPORTING_ALL_FAILURES
 */
public class FuzzyRule implements TestRule {

	/**
	 * A reusable test rule with the default configuration.
	 *
	 * <p>Usage:</p>
	 *
	 * <pre>
	 *   public class MyTestSuite {
	 *
	 *     public final @Rule TestRule fuzzyRule = FuzzyRule.DEFAULT;
	 *
	 *     // ...
	 *   }
	 * </pre>
	 */
	public static final FuzzyRule DEFAULT = new FuzzyRule(new Config());

	/**
	 * A reusable test rule configured to use the {@linkplain CaseCompositionMode#EACH_SUBCASE_AT_LEAST_ONCE each subcase at least once}
	 * case composition mode. This configuration is useful when the default configuration fails due to generating too
	 * many test iterations.
	 *
	 * <p>Usage:</p>
	 *
	 * <pre>
	 *   public class MyTestSuite {
	 *
	 *     public final @Rule TestRule fuzzyRule = FuzzyRule.EACH_SUBCASE_AT_LEAST_ONCE;
	 *
	 *     // ...
	 *   }
	 * </pre>
	 *
	 * @see Config#withEachSubcaseAtLeastOnce()
	 */
	public static final FuzzyRule EACH_SUBCASE_AT_LEAST_ONCE = custom().withEachSubcaseAtLeastOnce().build();

	/**
	 * A variant of the {@linkplain #DEFAULT default test rule} that will execute all test iterations even when one of
	 * them fails. Using this rule, you will be informed of all test failures.
	 *
	 * <p>Usage:</p>
	 *
	 * <pre>
	 *   public class MyTestSuite {
	 *
	 *     public final @Rule TestRule fuzzyRule = FuzzyRule.REPORTING_ALL_FAILURES;
	 *
	 *     // ...
	 *   }
	 * </pre>
	 */
	public static final FuzzyRule REPORTING_ALL_FAILURES = custom().reportingAllFailures().build();

	/**
	 * Provides an entry point for building a customized {@code FuzzyRule} configuration.
	 *
	 * <p>Usage:</p>
	 *
	 * <pre>
	 *   public class MyTestSuite {
	 *
	 *     public final @Rule TestRule fuzzyRule = FuzzyRule.custom()
	 *       .withMaxIterations(100)
	 *       .reportingVerbosely()
	 *       .build();
	 *
	 *     // ...
	 *   }
	 *
	 * </pre>
	 *
	 * @return a configuration builder ready for method chaining.
	 */
	public static Config custom() {
		return new Config();
	}

	private final TestReporter testReporter;
	private final int maxIterations;
	private final boolean failAfterMaxIterations;
	private final boolean failImmediately;
	private final CaseCompositionMode caseCompositionMode;

	private FuzzyRule(Config config) {
		testReporter = config.testReporter;
		maxIterations = config.maxIterations;
		failAfterMaxIterations = config.failAfterMaxIterations;
		failImmediately = config.failImmediately;
		caseCompositionMode = config.caseCompositionMode;
	}

	@Override
	public Statement apply(Statement base, Description description) {
		return new FuzzyStatement(this, base, description);
	}

	private static class FuzzyStatement extends Statement {
		private final FuzzyRule parent;
		private final Statement baseStatement;
		private final Description description;

		public FuzzyStatement(FuzzyRule parent, Statement baseStatement, Description description) {
			this.parent = parent;
			this.baseStatement = baseStatement;
			this.description = description;
		}

		@Override
		public void evaluate() throws Throwable {
			// Base the randomization seed on the test name so that it runs deterministically but with different
			// values for each test.
			Context.init(parent.caseCompositionMode, description.getDisplayName().hashCode());

			parent.testReporter.preTest(description);

			boolean overallSuccess = false;
			int iterations = 0;

			try {
				Throwable[] lastFailure = new Throwable[1];

				do {
					iterations++;
					if(iterations > parent.maxIterations) {
						if(parent.failAfterMaxIterations)
							throw new IllegalStateException(
								"The test " + description.toString() + " was not able to complete within " +
								parent.maxIterations + (parent.maxIterations == 1 ? " iteration" : " iterations") +
								", because the number of subcase permutations is too large. You must take one of " +
								"the following actions:\n" +
								"\n" +
								"* increase the allowed number of iterations;\n" +
								"* switch to a different case composition mode, such as EACH_SUBCASE_AT_LEAST_ONCE; " +
								"or\n" +
								"* reduce the complexity of your subcases, for example by breaking your tests into " +
								"smaller units."
							);
						else
							break;
					}

					parent.testReporter.preIteration(description, iterations - 1);

					boolean[] success = new boolean[] { true };
					try {
						baseStatement.evaluate();
					}
					catch(AssertionError failure) {
						handleTestFailure(iterations, lastFailure, success, failure);
					}
					catch(Error e) {
						throw e;
					}
					catch(InterruptedException e) {
						Thread.currentThread().interrupt();
						throw e;
					}
					catch(Throwable failure) {
						handleTestFailure(iterations, lastFailure, success, failure);
					}

					parent.testReporter.postIteration(description, iterations - 1, success[0]);
				}
				while(Context.next());

				if(lastFailure[0] != null)
					throw lastFailure[0];
				else
					overallSuccess = true;
			}
			finally {
				parent.testReporter.postTest(description, iterations, overallSuccess);
				Context.cleanUp();
			}
		}

		private void handleTestFailure(int iterations, Throwable[] lastFailure, boolean[] success, Throwable failure)
		throws Throwable {
			failure = parent.testReporter.wrapFailure(description, iterations - 1, failure);

			lastFailure[0] = failure;
			success[0] = false;

			parent.testReporter.failure(description, iterations - 1, failure);
			if(parent.failImmediately)
				throw failure;
		}
	}

	/**
	 * A builder for configuring unit case {@linkplain FuzzyRule test rules}. You may obtain a new builder instance via
	 * the {@link FuzzyRule#custom()} method.
	 *
	 * @see FuzzyRule#custom()
	 */
	public static class Config {
		private TestReporter testReporter = TestReporter.DEFAULT;

		private CaseCompositionMode caseCompositionMode = CaseCompositionMode.PAIRWISE_PERMUTATIONS_OF_SUBCASES;
		private int maxIterations = 1000;
		private boolean failAfterMaxIterations = true;

		private boolean failImmediately = true;

		/**
		 * Sets the test reporter responsible for communicating unit test progress and status. By default, this is
		 * set to {@link TestReporter#DEFAULT}.
		 *
		 * @param testReporter the test reporter to use. Cannot be {@literal null}.
		 *
		 * @return this {@code Config} instance, to aid in method chaining.
		 */
		public Config withTestReporter(TestReporter testReporter) {
			this.testReporter = FuzzyPreconditions.checkNotNull(testReporter);
			return this;
		}

		/**
		 * Sets the {@link CaseCompositionMode} for the unit tests executed by this rule. By default,
		 * the composition mode is {@link CaseCompositionMode#PAIRWISE_PERMUTATIONS_OF_SUBCASES}, which will run the
		 * test enough times to cover every possible combination of every pair of generators in your test case.
		 *
		 * @param caseCompositionMode the composition mode for the tests. Cannot be {@code null}.
		 *
		 * @return this {@code Config} instance, to aid in method chaining.
		 *
		 * @see #withEachSubcaseAtLeastOnce()
		 * @see #withMaxIterations(int)
		 */
		public Config withCaseCompositionMode(CaseCompositionMode caseCompositionMode) {
			this.caseCompositionMode = FuzzyPreconditions.checkNotNull(caseCompositionMode);
			return this;
		}

		/**
		 * Sets the maximum number of times the fuzzy rule will run each test in the suite. By default, this is
		 * {@code 1000}.
		 *
		 * <p>If {@code maxIterations} is insufficient to cover the number of subcase combinations calculated by the
		 * {@linkplain #withCaseCompositionMode(CaseCompositionMode) case composition mode}, then the test may not run
		 * each of the subcases you expect it to. By default, that will cause the test to fail. You can configure this
		 * behavior with the {@link #withFailAfterMaxIterations(boolean) failAfterMaxIterations} property.
		 * </p>
		 *
		 * @param maxIterations the maximum number of times any test in the suite will be executed. Cannot be less than
		 *        or equal to zero.
		 *
		 * @return this {@code Config} instance, to aid in method chaining.
		 *
		 * @see #withCaseCompositionMode(CaseCompositionMode)
		 * @see #withEachSubcaseAtLeastOnce()
		 * @see #withUnboundedIterations()
		 */
		public Config withMaxIterations(int maxIterations) {
			if(maxIterations <= 0)
				throw new IllegalArgumentException("Fuzzy config requires at least one iteration.");

			this.maxIterations = maxIterations;
			return this;
		}

		/**
		 * Determines if tests will fail when the value of the {@link #withMaxIterations(int) maxIterations} property
		 * is too small to cover all of the subcase combinations calculated by the
		 * {@linkplain #withCaseCompositionMode(CaseCompositionMode) case composition mode}. The default value is
		 * {@code true}.
		 *
		 * @param failAfterMaxIterations set to {@code true} to force a failure, and {@code false} to allow tests to
		 *        succeed when they have not covered their generators' subcases.
		 *
		 * @return this {@code Config} instance, to aid in method chaining.
		 *
		 * @see #withCaseCompositionMode(CaseCompositionMode)
		 * @see #withMaxIterations(int)
		 * @see #withUnboundedIterations()
		 */
		public Config withFailAfterMaxIterations(boolean failAfterMaxIterations) {
			this.failAfterMaxIterations = failAfterMaxIterations;
			return this;
		}

		/**
		 * Determines if any unit test failures will cause the test to immediately stop running, therefore reporting at
		 * most one test failure. By default, this is set to {@code true}.
		 *
		 * @param failImmediately set to {@code false} to have the fuzzy rule report all test failures, and {@code true}
		 *        (the default) to have it report
		 *
		 * @return this {@code Config} instance, to aid in method chaining.
		 *
		 * @see #deferringFailures()
		 * @see #reportingAllFailures()
		 */
		public Config withFailImmediately(boolean failImmediately) {
			this.failImmediately = failImmediately;
			return this;
		}

		/**
		 * Sets the {@linkplain #withCaseCompositionMode(CaseCompositionMode) case composition mode} to
		 * {@link CaseCompositionMode#EACH_SUBCASE_AT_LEAST_ONCE}. This mode provides less comprehensive coverage than
		 * the default, but is appropriate for tests with many subcases where
		 * {@link CaseCompositionMode#PAIRWISE_PERMUTATIONS_OF_SUBCASES} would result in too many permutations.
		 *
		 * @return this {@code Config} instance, to aid in method chaining.
		 *
		 * @see #withMaxIterations(int)
		 */
		public Config withEachSubcaseAtLeastOnce() {
			return this.withCaseCompositionMode(CaseCompositionMode.EACH_SUBCASE_AT_LEAST_ONCE);
		}

		/**
		 * Configures the {@code FuzzyRule} so that it supports as many iterations as necessary to cover all generator
		 * combinations. (Note that tests are still limited by the practical considerations of memory and time.)
		 *
		 * @return this {@code Config} instance, to aid in method chaining.
		 */
		public Config withUnboundedIterations() { return withMaxIterations(Integer.MAX_VALUE); }

		/**
		 * Sets the {@link #withFailImmediately(boolean) failImmediately} property to {@code false}, so that tests will
		 * be run for all generator combinations, even when one or more of them fail.
		 *
		 * <p>Note: consider using {@link #reportingAllFailures()} instead of this setting, in order to get the maximum
		 * output possible from your test configuration.</p>
		 *
		 * @return this {@code Config} instance, to aid in method chaining.
		 */
		public Config deferringFailures() { return withFailImmediately(false); }

		/**
		 * Sets the {@link #withTestReporter(TestReporter) testReporter} property to {@link TestReporter#VERBOSE}, for
		 * the maximum possible test output.
		 *
		 * @return this {@code Config} instance, to aid in method chaining.
		 */
		public Config reportingVerbosely() { return withTestReporter(TestReporter.VERBOSE); }

		/**
		 * Sets the {@link #withTestReporter(TestReporter) testReporter} property to {@link TestReporter#SUMMARIZING}.
		 *
		 * @return this {@code Config} instance, to aid in method chaining.
		 */
		public Config summarizingTestCases() { return withTestReporter(TestReporter.SUMMARIZING); }

		/**
		 * Sets the {@link #withTestReporter(TestReporter) testReporter} property to
		 * {@link TestReporter#REPORTING_ALL_FAILURES}, and the {@link #withFailImmediately(boolean) failImmediately}
		 * property to {@code false}, resulting in test output that describes every failed iteration of a test case.
		 *
		 * @return this {@code Config} instance, to aid in method chaining.
		 */
		public Config reportingAllFailures() {
			return
				withTestReporter(TestReporter.REPORTING_ALL_FAILURES)
				.deferringFailures();
		}

		/** @see #withTestReporter(TestReporter)
		 */
		public void setTestReporter(TestReporter testReporter) { withTestReporter(testReporter); }

		/** @see #withMaxIterations(int)
		 */
		public void setMaxIterations(int maxIterations) { withMaxIterations(maxIterations); }

		/** @see #withFailAfterMaxIterations(boolean)
		 */
		public void setFailAfterMaxIterations(boolean failAfterMaxIterations) {
			withFailAfterMaxIterations(failAfterMaxIterations);
		}

		/** @see #withCaseCompositionMode(CaseCompositionMode)
		 */
		public void setCaseCompositionMode(CaseCompositionMode caseCompositionMode) {
			this.caseCompositionMode = caseCompositionMode;
		}

		/** @see #withFailImmediately(boolean)
		 */
		public void setFailImmediately(boolean failImmediately) {
			this.failImmediately = failImmediately;
		}

		/**
		 * Returns a new {@code FuzzyRule} instance with the configuration currently described by this builder.
		 */
		public FuzzyRule build() { return new FuzzyRule(this); }
	}

}
