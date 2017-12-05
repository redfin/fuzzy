package com.redfin.fuzzy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The generator is a stand-in for an input value in a test case. It is composed of one or more {@linkplain Case cases}
 * that describe the types of value the generator represents. The fuzzy engine will ensure that your test is executed
 * with each <a href="https://en.wikipedia.org/wiki/Equivalence_partitioning"><em>equivalency class</em></a> supported
 * by your generators.
 *
 * <pre><code>
 * &#064;Test
 * public void myTest() {
 *     // Declare all generators at the beginning of your test, before you call .get() on any of them.
 *     Generator&lt;String&gt; inputString = Generator.of(Any.string());
 *     Generator&lt;Integer&gt; inputInt = Generator.of(Any.integer());
 *
 *     String actual = subject.someMethod(inputString.get(), inputInt.get());
 *
 *     // Each subsequent call to .get() will return the same value, so you can use them multiple times in your test.
 *     assertEquals(inputString.get() + " " + inputInt.get(), actual);
 * }
 * </code></pre>
 * <p>Once you have declared the generators used by your test, you can obtain specific test values by calling the
 * {@link #get()} method. For a given iteration, each subsequent call to {@code get} will return exactly the same value,
 * so it is not necessary to store the result in a separate variable. For example:
 * </p>
 * <p> You must declare each generator used by your test before any of them are used (by calling {@code get}). This
 * pattern ensures that fuzzy understands how many test permutations are necessary without the need for too much
 * boilerplate code.
 * </p>
 *
 * @param <T> the type of value produced by this generator.
 */
public class Generator<T> implements Comparable<Generator<T>> {

	/*package*/ Generator(Context c) { context = c; }

	private final Context context;
	private String name = "Unknown";
	private StackTraceElement[] creationSite = new StackTraceElement[0];

	public final String getName() {
		return name;
	}

	public final List<StackTraceElement> getCreationSite() {
		return Collections.unmodifiableList(Arrays.asList(creationSite));
	}

	public final T get() {
		return context.currentValue(this);
	}

	/*package*/ final Subcase<T> getCurrentSubcase() {
		return context.currentSubcase(this);
	}

	public static GeneratorBuilder named(String name) {
		return new GeneratorBuilder(FuzzyPreconditions.checkNotNull(name));
	}

	@SafeVarargs
	public static <X> Generator<X> of(Case<X>... cases) { return new GeneratorBuilder().of(cases); }

	@SafeVarargs
	public static <X> Generator<X> of(X... literals) { return new GeneratorBuilder().of(literals); }

	@SafeVarargs
	public static <X> Generator<X> ofCases(Case<X>... cases) { return new GeneratorBuilder().of(cases); }

	@SafeVarargs
	public static <X> Generator<X> of(Subcase<X>... subcases) { return new GeneratorBuilder().of(subcases); }

	@Override
	public final int hashCode() {
		return name.hashCode();
	}

	@Override
	public final boolean equals(Object obj) {
		return obj == this || (obj instanceof Generator && name.equals(((Generator) obj).name));
	}

	@Override
	public final int compareTo(Generator<T> o) {
		return name.compareTo(o.name);
	}

	public static class GeneratorBuilder {
		private final String name;
		private final StackTraceElement[] creationSite;

		private GeneratorBuilder(String name) {
			this.name = name;
			this.creationSite = Thread.currentThread().getStackTrace();
		}

		private GeneratorBuilder() {
			this.creationSite = Thread.currentThread().getStackTrace();
			this.name = "at " + creationSite[4].toString();
		}

		@SafeVarargs
		public final <X> Generator<X> of(Case<X>... cases) {
			FuzzyPreconditions.checkNotNullAndContainsNoNulls(cases);
			FuzzyPreconditions.checkNotEmpty(cases);

			// Build the generator
			Context c = Context.getUnlocked();
			Generator<X> g = new Generator<>(c);
			g.creationSite = creationSite;
			g.name = name;

			// Register and return
			c.register(g, cases);
			return g;
		}

		@SafeVarargs
		public final <X> Generator<X> ofCases(Case<X>... cases) {
			return of(cases);
		}

		@SafeVarargs
		public final <X> Generator<X> of(X... literals) { return this.of(Any.of(literals)); }

		@SafeVarargs
		public final <X> Generator<X> of(Subcase<X>... subcases) {
			FuzzyPreconditions.checkNotNullAndContainsNoNulls(subcases);
			return this.of(Cases.of(subcases));
		}
	}

}
