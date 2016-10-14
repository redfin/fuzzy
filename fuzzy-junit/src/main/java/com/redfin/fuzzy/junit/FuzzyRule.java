package com.redfin.fuzzy.junit;

import com.redfin.fuzzy.Context;
import com.redfin.fuzzy.Preconditions;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class FuzzyRule implements TestRule {

	public static final FuzzyRule DEFAULT = new FuzzyRule(new Config());
	public static final FuzzyRule REPORTING_ALL_FAILURES = custom().reportingAllFailures().build();

	public static final FuzzyRule SUMMARIZING = custom().summarizingTestCases().build();
	public static final FuzzyRule VERBOSE = custom().reportingVerbosely().build();

	public static Config custom() {
		return new Config();
	}

	private final TestReporter testReporter;
	private final int maxIterations;
	private final boolean failAfterMaxIterations;
	private final boolean failImmediately;

	private FuzzyRule(Config config) {
		testReporter = config.testReporter;
		maxIterations = config.maxIterations;
		failAfterMaxIterations = config.failAfterMaxIterations;
		failImmediately = config.failImmediately;
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
			Context.init();
			parent.testReporter.preTest(description);

			boolean overallSuccess = false;
			int iterations = 0;

			try {
				Throwable[] lastFailure = new Throwable[1];

				do {
					iterations++;
					if(iterations > parent.maxIterations) {
						if(parent.failAfterMaxIterations)
							throw new IllegalStateException("TODO: too many iterations test");
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

	public static class Config {
		private TestReporter testReporter = TestReporter.DEFAULT;

		private int maxIterations = 100;
		private boolean failAfterMaxIterations = false;

		private boolean failImmediately = true;

		public Config withTestReporter(TestReporter testReporter) {
			this.testReporter = Preconditions.checkNotNull(testReporter);
			return this;
		}

		public Config withMaxIterations(int maxIterations) {
			if(maxIterations <= 0)
				throw new IllegalArgumentException("Fuzzy config requires at least one iteration.");

			this.maxIterations = maxIterations;
			return this;
		}

		public Config withFailAfterMaxIterations(boolean failAfterMaxIterations) {
			this.failAfterMaxIterations = failAfterMaxIterations;
			return this;
		}

		public Config withFailImmediately(boolean failImmediately) {
			this.failImmediately = failImmediately;
			return this;
		}

		public Config withUnboundedIterations() { return withMaxIterations(Integer.MAX_VALUE); }
		public Config deferringFailures() { return withFailImmediately(false); }

		public Config reportingVerbosely() { return withTestReporter(TestReporter.VERBOSE); }
		public Config summarizingTestCases() { return withTestReporter(TestReporter.SUMMARIZING); }
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

		public FuzzyRule build() { return new FuzzyRule(this); }
	}

}
