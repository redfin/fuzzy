package com.redfin.fuzzy;

import com.redfin.fuzzy.pairwise.Pairwise;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Subcases {

	public static <INPUT, OUTPUT> Set<Subcase<OUTPUT>> map(
		Set<Subcase<INPUT>> subcases,
		Function<Subcase<INPUT>, Subcase<OUTPUT>> mapping
	) {
		FuzzyPreconditions.checkNotNull(subcases);
		FuzzyPreconditions.checkNotNull(mapping);

		Set<Subcase<OUTPUT>> mapped = new HashSet<>(subcases.size());
		mapped.addAll(subcases.stream().map(mapping).collect(Collectors.toList()));

		return mapped;
	}

	public static <INPUT, OUTPUT> Set<Subcase<OUTPUT>> mapOutput(
		Set<Subcase<INPUT>> subcases,
		BiFunction<Random, INPUT, OUTPUT> mapping
	) {
		FuzzyPreconditions.checkNotNull(subcases);
		FuzzyPreconditions.checkNotNull(mapping);

		Function<Subcase<INPUT>, Subcase<OUTPUT>> mapper =
			s -> (r -> mapping.apply(r, s.generate(r)));

		return subcases.stream().map(mapper).collect(Collectors.toSet());
	}

	public static <INPUT, OUTPUT> Set<Subcase<OUTPUT>> mapOutput(
		Set<Subcase<INPUT>> subcases,
		Function<INPUT, OUTPUT> mapping
	) {
		FuzzyPreconditions.checkNotNull(subcases);
		FuzzyPreconditions.checkNotNull(mapping);

		Function<Subcase<INPUT>, Subcase<OUTPUT>> mapper =
			s -> (r -> mapping.apply(s.generate(r)));

		return subcases.stream().map(mapper).collect(Collectors.toSet());
	}

	public interface BiPermutedSupplierFunction<T, U, R> {
		R generate(Random r, T t, U u);
	}

	public static <T, U, R> Set<Subcase<R>> pairwisePermutations(
		Set<Subcase<T>> tSubcases,
		Set<Subcase<U>> uSubcases,
		BiPermutedSupplierFunction<T, U, R> func
	) {
		FuzzyPreconditions.checkNotNull(func);
		FuzzyPreconditions.checkNotNull(tSubcases);
		FuzzyPreconditions.checkNotNull(uSubcases);

		List<Set> options = new ArrayList<>();
		options.add(tSubcases);
		options.add(uSubcases);

		List<List<Object>> permutations = (new Pairwise(options)).generate();

		return permutations.stream()
			.map(permutation -> {
				@SuppressWarnings("unchecked") Subcase<T> tFunc = (Subcase<T>) permutation.get(0);
				@SuppressWarnings("unchecked") Subcase<U> uFunc = (Subcase<U>) permutation.get(1);
				return new BiPermutedSupplier<>(func, tFunc, uFunc);
			})
			.collect(Collectors.toSet());
	}

	private static class BiPermutedSupplier<T, U, R> implements Subcase<R> {
		private final BiPermutedSupplierFunction<T, U, R> func;
		private final Subcase<T> tSubcase;
		private final Subcase<U> uSubcase;

		public BiPermutedSupplier(BiPermutedSupplierFunction<T, U, R> func, Subcase<T> tSubcase, Subcase<U> uSubcase) {
			this.func = func;
			this.tSubcase = tSubcase;
			this.uSubcase = uSubcase;
		}

		@Override
		public R generate(Random random) {
			return func.generate(random, tSubcase.generate(random), uSubcase.generate(random));
		}
	}

}
