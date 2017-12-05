package com.redfin.fuzzy;

import com.redfin.fuzzy.cases.LiteralCase;
import java.util.Random;

/**
 * Helper methods for building cases from literal and static values.
 */
public class Literal {

	/**
	 * Returns a case that always resolved to the given value.
	 */
	public static <T> Case<T> value(T value) {
		return new LiteralCase<>(value);
	}

	/**
	 * Returns a case that always returns the value created by the given generator in each test iteration.
	 *
	 * <p>Recall that generators will create a new value once every test iteration, and then that same value will be
	 * used each time the generator's {@link Generator#get()} method is called. By bounding a second generator to the
	 * output of another, you can build test variables that depend on each other.</p>
	 *
	 * <p>For example, if you wanted to create one test variable for the size of a list, and a second test variable
	 * for the contents of the list, you could write the following code:</p>
	 *
	 * <pre>
	 * Generator&lt;Integer&gt; listSize = Generator.of(Any.integer().inRange(10, 30));
	 * Generator&lt;List&lt;Integer&gt;&gt; list = Generator.of(
	 *   Any
	 *     .listOf(Any.integer())
	 *     .withSizeOf(Literal.valueBoundTo(listSize))
	 *     ;
	 * </pre>
	 *
	 * <p>Note that if you tried to use {@code listSize} to directly set {@code list}'s size, as in
	 * {@code .withSizeOf(listSize.get())}, you would encounter a run-time error because all generators must be
	 * defined before any of them can be accessed.</p>
	 *
	 * <p>Similarly, it would be a mistake to create a separate {@code Case<Integer>} variable to store the list
	 * size for use in both generators. In this approach, each usage of that case could end up providing different
	 * values in the same test iteration.</p>
	 *
	 * @param generator the source of the values to be returned by the new case.
	 * @param <T> the type of values returned by the new case.
	 */
	public static <T> Case<T> valueBoundTo(Generator<T> generator) {
		FuzzyPreconditions.checkNotNull("The given generator cannot be null.", generator);
		return Cases.of(new Subcase<T>() {

			@Override
			public T generate(Random random) {
				return generator.get();
			}

			@Override
			public void describeTo(StringBuilder sink, T value) {
				generator.getCurrentSubcase().describeTo(sink, value);
			}

		});
	}

	/**
	 * Returns a case that always resolves to {@code null}.
	 */
	public static <T> Case<T> nil() { return new LiteralCase<>(null); }

}
