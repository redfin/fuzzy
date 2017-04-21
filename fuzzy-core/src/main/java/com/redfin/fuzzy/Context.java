package com.redfin.fuzzy;

import com.redfin.fuzzy.pairwise.Pairwise;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

/**
 * The principal engine of the fuzzy evaluation workflow.
 *
 * <p>Typically, you should not interact with this class directly, preferring to use an integration with your particular
 * testing framework. If that integration is not available, or if you are writing your own, you will need to manage the
 * context manually.
 * </p>
 * <p>The context is backed by state stored in a thread local and therefore any generators must be initialized on the
 * same thread where {@code Context.init} is executed.
 * </p>
 *
 * <p>Example usage:</p>
 *
 * <pre>{@code
 * public void executeFuzzyTest() {
 *   // Initialize the testing context. This applies to all generators
 *   // created on this thread.
 *   Context.init();
 *
 *   try {
 *     do {
 *       // Execute your test code here.
 *       executeSingleTestIteration();
 *     } while(Context.next());
 *   }
 *   catch(AssertionError | Exception e) {
 *     // Handle test failures
 *     // You can use Context.report() or Context.reportTo(StringBuilder)
 *     // to get a sense of the inputs that caused the test failure.
 *   }
 *   finally {
 *     // Clean up for the next test
 *     Context.cleanUp();
 *   }
 * }
 * }</pre>
 */
public class Context {

	/**
	 * Initializes the context in preparation for running a single test (the context should be initialized for each
	 * test individually).
	 *
	 * @param caseCompositionMode the algorithm {@code Context} should use to build permutations of the test variables.
	 * @param randomSeed the seed to use for all randomized calls for this test; setting the seed consistently makes the
	  *       randomization deterministic across different test passes.
	 */
	public static void init(CaseCompositionMode caseCompositionMode, long randomSeed) {
		if(CONTEXT.get() != null)
			throw CONTEXT.get().newReinitializedException();

		Context c = new Context(caseCompositionMode);
		c.random.setSeed(randomSeed);

		CONTEXT.set(c);
	}

	/**
	 * Marks the termination of a single iteration of the test being executed, and returns {@code true} if more
	 * iterations are necessary to execute all test cases.
	 */
	public static boolean next() {
		Context c = CONTEXT.get();
		if(c == null)
			throw newUninitializedException();
		if(c.iterations == null || c.iterations.size() == 0)
			return false;
		else if(c.iterations.size() > 1) {
			c.previousGenerators = c.generators;
			c.generators = new HashMap<>();
			c.locked = false;
			c.iterations.pop();

			return true;
		}
		else { // c.iterations.size == 1
			c.iterations.pop();
			return false;
		}
	}

	/**
	 * Marks the completion of a single test execution and all iterations. {@code remove} must be called before
	 * {@code init} can be called for the next test.
	 */
	public static void cleanUp() {
		CONTEXT.remove();
	}

	/**
	 * Returns a map of the objects that have been chosen for the various generators created by the current test
	 * iteration. Useful for collecting debugging information.
	 */
	public static Map<Generator, Object> valuesForCurrentIteration() {
		Context c = CONTEXT.get();
		if(c == null)
			throw newUninitializedException();

		Map<Generator, Object> res = new HashMap<>();
		if(c.iterations != null && !c.iterations.isEmpty())
			for(Map.Entry<Generator, Iteration> variable : c.iterations.peek().entrySet()) {
				Iteration i = variable.getValue();
				if(i.generated)
					res.put(variable.getKey(), i.getCurrent());
			}

		return res;
	}

	/**
	 * Produces a human-readable report of the context's status, in American English.
	 */
	public static String report() {
		StringBuilder sb = new StringBuilder();
		reportTo(sb);
		return sb.toString();
	}

	/**
	 * Produces a human-readable report of the context's status, in American English.
	 */
	public static void reportTo(StringBuilder sb) {
		Context c = CONTEXT.get();
		if(c == null || c.iterations == null || c.iterations.isEmpty())
			return;

		for(Map.Entry<Generator, Iteration> variable : c.iterations.peek().entrySet()) {
			Iteration i = variable.getValue();
			if(i.generated) {
				sb.append("  ");
				i.describeTo(sb);
				sb.append(" from generator ");
				sb.append(variable.getKey().getName());
				sb.append('\n');
			}
		}
	}

	private static final ThreadLocal<Context> CONTEXT = new ThreadLocal<>();

	/*package*/ static Context getUnlocked() {
		Context c = CONTEXT.get();
		if(c == null) {
			throw newUninitializedException();
		}
		else if (c.locked) {
			throw c.newAlreadyLockedException();
		}
		else {
			return c;
		}
	}

	private Context(CaseCompositionMode caseCompositionMode) {
		this.caseCompositionMode = FuzzyPreconditions.checkNotNull(
			"A case composition mode is required.",
			caseCompositionMode
		);

		contextInitTrace = Thread.currentThread().getStackTrace();
	}

	private final Random random = new Random();
	private final CaseCompositionMode caseCompositionMode;
	private final StackTraceElement[] contextInitTrace;

	private boolean locked;
	private StackTraceElement[] lockTrace;

	private Map<Generator, Case[]> previousGenerators;
	private Map<Generator, Case[]> generators = new HashMap<>();

	private Stack<Map<Generator, Iteration>> iterations;

	/*package*/ <T> void register(Generator<T> generator, Case<?>[] cases) {
		FuzzyPreconditions.checkNotNull(generator);
		FuzzyPreconditions.checkNotNullAndContainsNoNulls(cases);

		if(locked) {
			throw newAlreadyLockedException();
		}
		else if(generators.containsKey(generator)) {
			throwDuplicateGenerator(generator);
		}

		generators.put(generator, cases);
	}

	/*package*/ <T> T currentValue(Generator<T> generator) {
		lock();

		Iteration i = iterations.peek().get(generator);
		if(i == null)
			throw newUnregisteredGeneratorException(generator);

		@SuppressWarnings("unchecked")
		T value = (T)i.get(random);
		return value;
	}

	/*package*/ void lock() {
		if(!locked) {
			locked = true;
			lockTrace = Thread.currentThread().getStackTrace();

			if (iterations == null) {
				generateTestCases();
			}
			else {
				validateConsistency();
			}
		}
	}

	private void validateConsistency() {
		// TODO: better consistency validation

		if(previousGenerators == null) return;

		for(Generator g : generators.keySet()) {
			if(!previousGenerators.containsKey(g)) {
				throw newInconsistentGeneratorsException();
			}
		}

		// Technically, nothing bad happens if this run does not define a generator that the previous run did; still
		// it probably means that they're setting tests up weird so we'll still complain.
		if(generators.size() != previousGenerators.size())
			throw newInconsistentGeneratorsException();
	}

	private void generateTestCases() {
		iterations = new Stack<>();

		if(generators.isEmpty())
			return;

		List<Variable> variables = new ArrayList<>();
		for(Map.Entry<Generator, Case[]> generator : generators.entrySet()) {
			variables.add(new Variable(generator.getKey(), generator.getValue()));
		}

		if(!variables.isEmpty()) {
			if (caseCompositionMode.equals(CaseCompositionMode.PAIRWISE_PERMUTATIONS_OF_SUBCASES)) {
				generatePairwiseTestCases(variables);
			} else if (caseCompositionMode.equals(CaseCompositionMode.EACH_SUBCASE_AT_LEAST_ONCE)) {
				generateEachSubcaseAtLeastOnceCases(variables);
			} else {
				throw new IllegalStateException("Unexpected caseCompositionMode " + caseCompositionMode);
			}
		}
	}

	private void generatePairwiseTestCases(List<Variable> variables) {
		Pairwise<Variable> permuter = new Pairwise<>(variables);
		Stack<List<Object>> permutations = permuter.generate();

		// Map the permutations back to something a bit more useful.
		for(List<Object> permutation : permutations) {
			Map<Generator, Iteration> wholeIteration = new HashMap<>(variables.size());
			iterations.push(wholeIteration);

			for(int i = 0; i < variables.size(); i++) {
				@SuppressWarnings("unchecked")
				Subcase<?> supplier = (Subcase<?>) permutation.get(i);

				Variable sourceVar = variables.get(i);
				Iteration iteration = new Iteration(supplier);

				wholeIteration.put(sourceVar.g, iteration);
			}
		}
	}

	private void generateEachSubcaseAtLeastOnceCases(List<Variable> variables) {
		int maxSubcases = variables.stream().mapToInt(ArrayList::size).max().orElse(0);

		for(int iteration = 0; iteration < maxSubcases; iteration++) {
			Map<Generator, Iteration> wholeIteration = new HashMap<>(variables.size());
			iterations.push(wholeIteration);

			for(Variable v : variables) {
				Subcase<?> supplier = v.get(iteration % v.size());
				wholeIteration.put(v.g, new Iteration(supplier));
			}
		}
	}

	private static IllegalStateException newUninitializedException() {
		return new IllegalStateException(
			"You initialized a fuzzy Generator when a fuzzy context had not yet been initialized. Verify that your " +
			"test case is properly configured, and that you are not creating Generators on a thread other than the " +
			"main thread executing your tests."
		);
	}

	private IllegalStateException newUnregisteredGeneratorException(Generator<?> generator) {
		StringBuilder message = new StringBuilder();
		message.append("You attempted to read the value of a generator that was not created as part of the current ");
		message.append("test. Perhaps you are using a generator created for a different test?");

		if(generator != null) {
			message.append("\n");
			reportGeneratorTo("unregistered", generator, message);
		}

		if(generators != null && !generators.isEmpty()) {
			message.append("\n");
			message.append("The available generators were:\n\n");

			for(Generator available : generators.keySet())
				reportGeneratorTo("", available, message);
		}

		throw new IllegalStateException(message.toString());
	}

	private IllegalStateException newInconsistentGeneratorsException() {
		StringBuilder message = new StringBuilder();
		message.append("The generators declared for the current iteration of this test are different than those ");
		message.append("declared for the first iteration. This can lead to inconsistent test results and is ");
		message.append("therefore disallowed. Ensure that your generators are being initialized consistently, ");
		message.append("regardless of other state and variables within your test.\n");
		message.append("\n");

		if(previousGenerators == null || previousGenerators.isEmpty()) {
			message.append("The first test iteration did not declare any generators.\n");
			message.append("\n");
		}
		else {
			message.append("The first test iteration declared the following generators:\n");
			message.append("\n");

			for(Generator prev : previousGenerators.keySet()) {
				reportGeneratorTo("", prev, message);
			}
		}

		if(generators == null || generators.isEmpty()) {
			message.append("The current test iteration did not declare any generators.\n");
		}
		else {
			message.append("The current test iteration declared the following generators.\n");
			message.append("\n");

			for(Generator curr : generators.keySet()) {
				reportGeneratorTo("", curr, message);
			}
		}

		return new IllegalStateException(message.toString());
	}

	private IllegalStateException newReinitializedException() {
		StringBuilder message = new StringBuilder();
		message.append("The fuzzy context for this test was incorrectly initialized more than once. This is likely a ");
		message.append("problem with the test framework that you are using.\n");
		message.append("\n");
		message.append("If you are controlling the fuzzy context manually, make sure to invoke Context.cleanUp() ");
		message.append("before you call Context.init(...) a second time. See the documentation for Context for a ");
		message.append("usage example.\n");
		message.append("\n");
		message.append("The context was previously initialized at the following location:\n");
		message.append("\n");

		for(StackTraceElement e : contextInitTrace)
			message.append("  at ").append(e.toString()).append("\n");

		return new IllegalStateException(message.toString());
	}

	private IllegalStateException newAlreadyLockedException() {
		StringBuilder message = new StringBuilder();
		message.append("Cannot modify a test context that has already been locked.\n\n");
		message.append("The most likely cause of this error is that you attempted to declare a new Generator ");
		message.append("after getting the value of another generator in the same test. All generators for a test ");
		message.append("must be declared before calling .get() on any of them.\n\n");

		if(lockTrace != null) {
			message.append("The context was locked at the following stack trace:\n");

			for(StackTraceElement e : lockTrace)
				message.append("  at ").append(e.toString()).append("\n");

			message.append("\n");
		}

		return new IllegalStateException(message.toString());
	}

	private void throwDuplicateGenerator(Generator g) {
		StringBuilder sb = new StringBuilder();
		sb.append("Illegal attempt to register the same generator twice within the same test case.");
		sb.append("\n");

		reportGeneratorTo("New", g, sb);

		Generator existing = generators.keySet().stream().filter(g::equals).findFirst().orElse(null);
		if(existing != null) { reportGeneratorTo("Existing", existing, sb); }

		throw new IllegalStateException(sb.toString());
	}

	private void reportGeneratorTo(String context, Generator g, StringBuilder sb) {
		sb.append("  ");
		sb.append(context);
		sb.append(" generator: ");
		sb.append(g.getName());
		sb.append(", created\n");
		for(Object frame : g.getCreationSite()) {
			sb.append("    at ");
			sb.append(frame.toString());
			sb.append("\n");
		}
		sb.append("\n");
	}

	private static class Variable extends ArrayList<Subcase<?>> {
		private static final long serialVersionUID = 1L;
		transient final Generator g;
		Variable(Generator g, Case<?>[] cases) {
			this.g = g;
			for(Case<?> c : cases) addAll(c.getSubcases());
		}
	}

	private static class Iteration {
		private Object iterationValue;
		private volatile boolean generated;
		private final Subcase<?> subcase;

		public Iteration(Subcase<?> subcase) {
			this.subcase = subcase;
		}

		synchronized Object getCurrent() {
			return iterationValue;
		}

		synchronized Object get(Random random) {
			if(!generated) {
				iterationValue = subcase.generate(random);
				generated = true;
			}
			return iterationValue;
		}

		synchronized void describeTo(StringBuilder sb) {
			if(!generated) {
				sb.append("{not generated}");
			}
			else {
				@SuppressWarnings("unchecked")
				Subcase<Object> castSubcase = (Subcase<Object>) subcase;
				castSubcase.describeTo(sb, iterationValue);
			}
		}
	}

}
