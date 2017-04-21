package com.redfin.fuzzy.junit;

import com.redfin.fuzzy.Context;
import org.junit.runner.Description;

public interface TestReporter {

	void preTest(Description description);

	void preIteration(Description description, int index);

	void postIteration(Description description, int index, boolean success);

	void failure(Description description, int index, Throwable failure);

	void postTest(Description description, int iterations, boolean success);

	Throwable wrapFailure(Description description, int iteration, Throwable failure);

	class BaseTestReporter implements TestReporter {
		@Override public void preTest(Description description) {}
		@Override public void preIteration(Description description, int index) {}
		@Override public void postIteration(Description description, int index, boolean success) {}
		@Override public void postTest(Description description, int iterations, boolean success) {}
		@Override public void failure(Description description, int index, Throwable failure) {}

		@Override
		public Throwable wrapFailure(Description description, int iteration, Throwable failure) {
			if(Context.valuesForCurrentIteration().isEmpty()) {
				return failure;
			}
			else {
				StringBuilder message = new StringBuilder();

				message.append("Test failed with exception ");
				message.append(failure.getClass().getSimpleName());

				if(failure.getMessage() != null && failure.getMessage().length() > 0)
					message.append(": ").append(failure.getMessage());

				message.append("\n\n");
				message.append("This test failed after generating the following inputs:\n");
				Context.reportTo(message);
				message.append("\n");

				Exception wrapped = new Exception(message.toString(), failure);
				wrapped.setStackTrace(failure.getStackTrace());

				return wrapped;
			}
		}
	}

	TestReporter DEFAULT = new BaseTestReporter();

	TestReporter SUMMARIZING = new BaseTestReporter() {
		@Override
		public void postTest(Description description, int iterations, boolean success) {
			System.out.println(
				"Test " + description.toString() +
				" ran with " + iterations +
				" iteration" + (iterations == 1 ? "" : "s") + "."
			);
		}
	};

	TestReporter REPORTING_ALL_FAILURES = new BaseTestReporter() {
		@Override
		public void failure(Description description, int index, Throwable failure) {
			System.err.println(description + " failed with inputs");
			System.err.println(Context.report());
		}
	};

	TestReporter VERBOSE = new BaseTestReporter() {
		@Override
		public void preTest(Description description) {
			System.out.println("Beginning test " + description.toString() + ".");
		}

		@Override
		public void postIteration(Description description, int index, boolean success) {
			System.out.println(Context.report());
			System.out.println("  Iteration " + index + " completed with status " + (success ? "success" : "failure") + ".");
		}

		@Override
		public void postTest(Description description, int iterations, boolean success) {
			System.out.println("Test " + description.toString() + " completed after " + iterations + " iteration(s).");
		}
	};

}
