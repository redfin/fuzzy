package com.redfin.fuzzy.cases;

import com.redfin.fuzzy.Case;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

public class LiteralCase<T> implements Case<T> {

	private final T literal;

	@SuppressWarnings("unchecked")
	public LiteralCase(T literal) {
		this.literal = literal;
	}

	private T get(Random ignored) { return literal; }

	@Override
	public Set<Function<Random, T>> getSuppliers() {
		return Collections.singleton(this::get);
	}

}
