package com.redfin.fuzzy;

import com.redfin.fuzzy.pairwise.Pairwise;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Suppliers {

	public static <INPUT, OUTPUT> Set<Function<Random, OUTPUT>> map(
		Set<Function<Random, INPUT>> suppliers,
		Function<Function<Random, INPUT>, Function<Random, OUTPUT>> mapping
	) {
		Preconditions.checkNotNull(suppliers);
		Preconditions.checkNotNull(mapping);

		Set<Function<Random, OUTPUT>> mapped = new HashSet<>(suppliers.size());
		mapped.addAll(suppliers.stream().map(mapping).collect(Collectors.toList()));

		return mapped;
	}

	public interface BiPermutedSupplierFunction<T, U, R> {
		R generate(Random r, T t, U u);
	}

	public static <T, U, R> Set<Function<Random, R>> pairwisePermutations(
		Set<Function<Random, T>> tSuppliers,
		Set<Function<Random, U>> uSuppliers,
		BiPermutedSupplierFunction<T, U, R> func
	) {
		Preconditions.checkNotNull(func);
		Preconditions.checkNotNull(tSuppliers);
		Preconditions.checkNotNull(uSuppliers);

		List<Set> options = new ArrayList<>();
		options.add(tSuppliers);
		options.add(uSuppliers);

		List<List<Object>> permutations = (new Pairwise(options)).generate();

		return permutations.stream()
			.map(permutation -> {
				@SuppressWarnings("unchecked") Function<Random, T> tFunc = (Function<Random, T>) permutation.get(0);
				@SuppressWarnings("unchecked") Function<Random, U> uFunc = (Function<Random, U>) permutation.get(1);
				return new BiPermutedSupplier<>(func, tFunc, uFunc);
			})
			.collect(Collectors.toSet());
	}

	private static class BiPermutedSupplier<T, U, R> implements Function<Random, R> {
		private final BiPermutedSupplierFunction<T, U, R> func;
		private final Function<Random, T> tSupplier;
		private final Function<Random, U> uSupplier;

		public BiPermutedSupplier(BiPermutedSupplierFunction<T, U, R> func, Function<Random, T> tSupplier, Function<Random, U> uSupplier) {
			this.func = func;
			this.tSupplier = tSupplier;
			this.uSupplier = uSupplier;
		}

		@Override
		public R apply(Random random) {
			return func.generate(random, tSupplier.apply(random), uSupplier.apply(random));
		}
	}

}
