package com.redfin.fuzzy;

import com.redfin.fuzzy.pairwise.Pairwise;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

public class Context {

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

	public static void init(long randomSeed) {
		if(CONTEXT.get() != null)
			throw new IllegalStateException("TODO: error message reinitialized");

		Context c = new Context();
		c.random.setSeed(randomSeed);

		CONTEXT.set(c);
	}

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

	public static void cleanUp() {
		CONTEXT.remove();
	}

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

	public static String report() {
		StringBuilder sb = new StringBuilder();
		reportTo(sb);
		return sb.toString();
	}

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

	private Context() {}

	private final Random random = new Random();

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
			throw new IllegalStateException("TODO: unregistered generator");

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
		if(previousGenerators == null) return;

		for(Generator g : generators.keySet()) {
			if(!previousGenerators.containsKey(g)) {
				throw new IllegalStateException("TODO: inconsistent generators message.");
			}
		}

		// Technically, nothing bad happens if this run does not define a generator that the previous run did; still
		// it probably means that they're setting tests up weird so we'll still complain.
		if(generators.size() != previousGenerators.size())
			throw new IllegalStateException("TODO: inconsistent generators message.");
	}

	private void generateTestCases() {
		iterations = new Stack<>();

		if(generators.isEmpty())
			return;

		// Build the raw permutations
		List<Variable> variables = new ArrayList<>();
		for(Map.Entry<Generator, Case[]> generator : generators.entrySet()) {
			variables.add(new Variable(generator.getKey(), generator.getValue()));
		}

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

	private static IllegalStateException newUninitializedException() {
		return new IllegalStateException("TODO: error message no init");
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
				message.append("  at ").append(e.toString());

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

	private static class Variable extends HashSet<Subcase<?>> {
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
