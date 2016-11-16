package com.redfin.fuzzy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

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
	public static <X> Generator<X> of(Function<Random, X>... suppliers) { return new GeneratorBuilder().of(suppliers); }

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
		public final <X> Generator<X> of(Function<Random, X>... suppliers) {
			FuzzyPreconditions.checkNotNullAndContainsNoNulls(suppliers);
			return this.of(() -> FuzzyUtil.setOf(suppliers));
		}
	}

}
