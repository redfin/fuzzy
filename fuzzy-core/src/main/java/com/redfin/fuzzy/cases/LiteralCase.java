package com.redfin.fuzzy.cases;

import com.redfin.fuzzy.Case;
import com.redfin.fuzzy.Subcase;
import java.util.Collections;
import java.util.Random;
import java.util.Set;

public class LiteralCase<T> implements Case<T> {

	private final T literal;

	@SuppressWarnings("unchecked")
	public LiteralCase(T literal) {
		this.literal = literal;
	}

	private T get(Random ignored) { return literal; }

	@Override
	public Set<Subcase<T>> getSubcases() {
		return Collections.singleton(this::get);
	}

}
