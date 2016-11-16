package com.redfin.fuzzy.cases;

import com.redfin.fuzzy.Case;
import com.redfin.fuzzy.FuzzyPreconditions;
import com.redfin.fuzzy.Suppliers;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

public class ExcludingCase<T> implements Case<T> {

	private static final int MAX_ATTEMPTS = 100;

	private final Case<T> baseCase;
	private final Set<T> excludedValues = new HashSet<T>();

	public ExcludingCase(Case<T> baseCase, T excludedValue) {
		this(baseCase, Collections.singleton(excludedValue));
	}

	public ExcludingCase(Case<T> baseCase, T... excludedValues) {
		this(baseCase, excludedValues == null ? null : Arrays.asList(excludedValues));
	}

	public ExcludingCase(Case<T> baseCase, Iterable<T> excludedValues) {
		FuzzyPreconditions.checkNotNull(baseCase);

		this.baseCase = baseCase;

		if(excludedValues != null) {
			for (T t : excludedValues)
				this.excludedValues.add(t);
		}
	}

	@Override
	public Set<Function<Random, T>> getSuppliers() {
		return Suppliers.map(
			baseCase.getSuppliers(),
			supplier -> (r -> {
				for(int i = 0; i < MAX_ATTEMPTS; i++) {
					T t = supplier.apply(r);
					if(!excludedValues.contains(t)) {
						return t;
					}
				}
				throw new IllegalStateException(String.format(
					"Failed to exclude unwanted values from a base case of type %s.",
					baseCase.getClass()
				));
			})
		);
	}

	@Override
	public ExcludingCase<T> excluding(Iterable<T> values) {
		if(values != null) {
			for(T t : values)
				excludedValues.add(t);
		}
		return this;
	}

}
