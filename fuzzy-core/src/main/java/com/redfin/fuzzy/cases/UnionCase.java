package com.redfin.fuzzy.cases;

import com.redfin.fuzzy.Case;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

public class UnionCase<T> implements Case<T> {

	private final Set<Case<T>> _subcases;

	@SafeVarargs
	public UnionCase(Case<T>... subcases) {
		_subcases = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(subcases)));
	}

	@Override
	public Set<Function<Random, T>> getSuppliers() {
		Set<Function<Random, T>> suppliers = new HashSet<>(_subcases.size());
		for(Case<T> subcase : _subcases) {
			suppliers.addAll(subcase.getSuppliers());
		}
		return suppliers;
	}
}
