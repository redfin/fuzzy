package com.redfin.fuzzy;

import com.redfin.fuzzy.cases.ExcludingCase;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface Case<T>
{

	Set<Function<Random, T>> getSuppliers();

	default Case<T> or(Case<T> other) { return Any.of(this, other); }

	default Case<T> excluding(T value) { return excluding(Collections.singleton(value)); }
	default Case<T> excluding(T... values) { return excluding(values == null ? null : Arrays.asList(values)); }
	default Case<T> excluding(Iterable<T> values) { return new ExcludingCase<>(this, values); }

	default T resolveAnyOnce() { return resolveAnyOnce(new Random()); }

	default T resolveAnyOnce(Random random) {
		Preconditions.checkNotNull(random);

		Set<Function<Random, T>> suppliers = getSuppliers();

		if(suppliers == null || suppliers.isEmpty())
			throw new IllegalStateException(String.format("Case of type %s generated zero suppliers.", getClass()));

		return suppliers.stream().findAny().orElse(null).apply(random);
	}

	default Set<T> resolveAllOnce() { return resolveAllOnce(new Random()); }

	default Set<T> resolveAllOnce(Random random) {
		Preconditions.checkNotNull(random);

		Set<Function<Random, T>> suppliers = getSuppliers();

		if(suppliers == null || suppliers.isEmpty())
			throw new IllegalStateException(String.format("Case of type %s generated zero suppliers.", getClass()));

		return suppliers.stream().map(s -> s.apply(random)).collect(Collectors.toSet());
	}

}
