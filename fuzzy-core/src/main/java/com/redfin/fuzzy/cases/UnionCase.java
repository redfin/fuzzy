package com.redfin.fuzzy.cases;

import com.redfin.fuzzy.Case;
import com.redfin.fuzzy.Subcase;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class UnionCase<T> implements Case<T> {

	private final Set<Case<T>> _subcases;

	@SafeVarargs
	public UnionCase(Case<T>... subcases) {
		_subcases = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(subcases)));
	}

	@Override
	public Set<Subcase<T>> getSubcases() {
		Set<Subcase<T>> subcases = new HashSet<>(_subcases.size());
		for(Case<T> subcase : _subcases) {
			subcases.addAll(subcase.getSubcases());
		}
		return subcases;
	}
}
