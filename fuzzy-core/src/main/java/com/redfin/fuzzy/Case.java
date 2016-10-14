package com.redfin.fuzzy;

import java.util.Random;
import java.util.Set;
import java.util.function.Function;

public interface Case<T>
{

	Set<Function<Random, T>> getSuppliers();

	default Case<T> or(Case<T> other) { return Any.of(this, other); }
}
