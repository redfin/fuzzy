package com.redfin.fuzzy;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class Cases {

	@SafeVarargs
	public static <T> Case<T> of(Function<Random, T>... suppliers) {
		Preconditions.checkNotNullAndContainsNoNulls(suppliers);
		Set<Function<Random, T>> suppliersSet = Util.setOf(suppliers);
		return () -> suppliersSet;
	}

	@SafeVarargs
	public static <T> Case<T> of(Supplier<T>... suppliers) {
		Preconditions.checkNotNullAndContainsNoNulls(suppliers);

		Set<Function<Random, T>> suppliersSet = new HashSet<>(suppliers.length);
		for(Supplier<T> supplier : suppliers) {
			suppliersSet.add(r -> supplier.get());
		}

		return () -> suppliersSet;
	}

	@SafeVarargs
	public static <T> Case<T> of(T... literalCases) {
		Preconditions.checkNotNull(literalCases);

		Set<Function<Random, T>> suppliersSet = new HashSet<>(literalCases.length);
		for(T t : literalCases) {
			suppliersSet.add(r -> t);
		}

		return () -> suppliersSet;
	}

	@SafeVarargs
	public static <T> Case<T> ofDelegates(Supplier<Case<T>>... delegateCases) {
		Preconditions.checkNotNullAndContainsNoNulls(delegateCases);

		Set<Function<Random, T>> suppliers = new HashSet<>();
		for(Supplier<Case<T>> delegate : delegateCases) {
			suppliers.addAll(delegate.get().getSuppliers());
		}

		return () -> suppliers;
	}

	public static <T, U> Case<U> map(Case<T> original, Function<T, U> mapping) {
		Preconditions.checkNotNull(original);
		Preconditions.checkNotNull(mapping);

		return map(original, (r, t) -> mapping.apply(t));
	}

	public static <T, U> Case<U> map(Case<T> original, BiFunction<Random, T, U> mapping) {
		Preconditions.checkNotNull(original);
		Preconditions.checkNotNull(mapping);

		return () -> {
			Set<Function<Random, T>> sourceSuppliers = original.getSuppliers();

			Set<Function<Random, U>> mappedSuppliers = new HashSet<>(sourceSuppliers.size());
			for(Function<Random, T> source : sourceSuppliers) {
				mappedSuppliers.add(r -> mapping.apply(r, source.apply(r)));
			}

			return mappedSuppliers;
		};
	}

}
