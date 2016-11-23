package com.redfin.fuzzy;

import com.redfin.fuzzy.pairwise.Pairwise;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Describes the different algorithms the fuzzy library can use to permute subcases when using one of the
 * {@linkplain Cases#compose Case composition} functions.
 */
public enum CaseCompositionMode {
	/**
	 * The composition will generate enough subcases so that each base case's suppliers are covered at least once, or
	 * the a number equivalent to the maximum number of suppliers of any base case. Use this mode when you are concerned
	 * about the number of combined cases growing too large.
	 *
	 * <p>
	 * For example, suppose you are writing a composition of the following three base cases:
	 * </p>
	 * <ul>
	 *     <li><strong>case {@code a}</strong>, with subcases {@code 1} and {@code 2};</li>
	 *     <li><strong>case {@code b}</strong>, with subcases {@code 'X'}, {@code 'Y'}, and {@code 'Z'}; and</li>
	 *     <li><strong>case {@code c}</strong>, with a single subcase {@code true}.</li>
	 * </ul>
	 * <p>
	 * When these cases are composed with {@code EACH_SUBCASE_AT_LEAST_ONCE}, the final set of combined cases might be
	 * the following:
	 * </p>
	 * <table>
	 *     <thead>
	 *         <tr><th></th><th>case {@code a}</th><th>case {@code b}</th><th>case {@code c}</th></tr>
	 *     </thead>
	 *     <tbody>
	 *         <tr><th>1</th><td>{@code 1}</td><td>{@code 'X'}</td><td>{@code true}</td></tr>
	 *         <tr><th>2</th><td>{@code 2}</td><td>{@code 'Y'}</td><td>{@code true}</td></tr>
	 *         <tr><th>3</th><td>{@code 1}</td><td>{@code 'Z'}</td><td>{@code true}</td></tr>
	 *     </tbody>
	 * </table>
	 * <p>
	 * Note that the total number of composed subcases is 3, because case {@code b} had the most subcases (again, 3).
	 * The subcases for {@code a} and {@code c} repeated as necessary so that each composed subcase had a valid value
	 * for all three.
	 * </p>
	 * <p>
	 * Note also that the way in which subcases are reused is not guaranteed by this algorithm and may change between
	 * tests.
	 * </p>
	 */
	EACH_SUBCASE_AT_LEAST_ONCE(baseCases -> {
		List<Subcase[]> permutations = new ArrayList<>();
		int[] indices = new int[baseCases.length];
		int completed = 0;

		Subcase[][] suppliers = new Subcase[baseCases.length][];
		for(int i = 0; i < baseCases.length; i++) {
			@SuppressWarnings("unchecked")
			Set<Function> subcases = baseCases[i].getSubcases();

			Subcase[] subcasesArray = subcases.toArray(new Subcase[] {});
			suppliers[i] = subcasesArray;
		}

		while(completed < baseCases.length) {
			Subcase[] subcase = new Subcase[baseCases.length];
			for(int i = 0; i < baseCases.length; i++) {
				subcase[i] = suppliers[i][indices[i] % suppliers[i].length];
				if(++indices[i] == suppliers[i].length) { completed++; }
			}
			permutations.add(subcase);
		}

		return permutations.toArray(new Subcase[][] {});
	}),

	/**
	 * The composition will generate enough subcases so that each possible pairing of any two base cases will be
	 * included at least once. Use this mode when you want to provide broader test coverage at the expense of a larger
	 * number of permutations.
	 *
	 * <p>
	 * This mode uses the pairwise algorithm defined by the {@link com.redfin.fuzzy.pairwise.Pairwise Pairwise} class;
	 * refer to it for more details. As an example, consider the following three base cases:
	 * </p>
	 * <ul>
	 *     <li><strong>case {@code a}</strong>, with subcases {@code 1} and {@code 2};</li>
	 *     <li><strong>case {@code b}</strong>, with subcases {@code 'X'}, {@code 'Y'}, and {@code 'Z'}; and</li>
	 *     <li><strong>case {@code c}</strong>, with a single subcase {@code true}.</li>
	 * </ul>
	 * <p>
	 * Given this input, the pairwise algorithm might generate the following composed subcases:
	 * </p>
	 * <table>
	 *     <thead>
	 *         <tr><th></th><th>case {@code a}</th><th>case {@code b}</th><th>case {@code c}</th></tr>
	 *     </thead>
	 *     <tbody>
	 *         <tr><th>1</th><td>{@code 1}</td><td>{@code 'X'}</td><td>{@code true}</td></tr>
	 *         <tr><th>2</th><td>{@code 1}</td><td>{@code 'Y'}</td><td>{@code true}</td></tr>
	 *         <tr><th>3</th><td>{@code 1}</td><td>{@code 'Z'}</td><td>{@code true}</td></tr>
	 *         <tr><th>4</th><td>{@code 2}</td><td>{@code 'X'}</td><td>{@code true}</td></tr>
	 *         <tr><th>5</th><td>{@code 2}</td><td>{@code 'Y'}</td><td>{@code true}</td></tr>
	 *         <tr><th>6</th><td>{@code 2}</td><td>{@code 'Z'}</td><td>{@code true}</td></tr>
	 *     </tbody>
	 * </table>
	 * <p>
	 * Note that each possible pair between ({@code a}, {@code b}), ({@code a}, {@code c}), and ({@code b}, {@code c})
	 * are covered at least once. The number of subcases necessary roughly depends on the number of subcases between the
	 * two largest base cases (in this example, case {@code a} had two possible subcases, and case {@code b} had three,
	 * for a total of {@code 2 * 3 = 6} cases).
	 * </p>
	 * <p>
	 * Note also that the specific distribution of subcases is not guaranteed by this algorithm and may change between
	 * tests.
	 * </p>
	 */
	PAIRWISE_PERMUTATIONS_OF_SUBCASES(baseCases -> {
		List<Set> parameters = Arrays
			.stream(baseCases)
			.map((Function<Case, Set>) Case::getSubcases)
			.collect(Collectors.toList());

		Pairwise<Set> p = new Pairwise<>(parameters);
		Stack<List<Object>> permutations = p.generate();

		Subcase[][] subcases = new Subcase[permutations.size()][];
		int i = 0;
		for(List<Object> subcase : permutations) {
			Subcase[] suppliers = new Subcase[baseCases.length];
			for(int j = 0; j < baseCases.length; j++) {
				suppliers[j] = (Subcase)subcase.get(j);
			}
			subcases[i++] = suppliers;
		}

		return subcases;
	}),

	;

	/* package */ final Algorithm algorithm;

	private CaseCompositionMode(Algorithm algorithm) { this.algorithm = algorithm; }

	interface Algorithm { Subcase[][] apply(Case[] baseCases); }
}
