package com.redfin.fuzzy.cases;

import com.redfin.fuzzy.Case;
import com.redfin.fuzzy.FuzzyPreconditions;
import com.redfin.fuzzy.Subcase;
import java.util.HashSet;
import java.util.Set;

public class NullableCase<T> implements Case<T> {

	private final Case<T> delegateCase;

	public NullableCase(Case<T> delegateCase) {
		this.delegateCase = FuzzyPreconditions.checkNotNull(delegateCase);
	}

	@Override
	public Set<Subcase<T>> getSubcases() {
		Set<Subcase<T>> subcases = new HashSet<>();
		subcases.addAll(delegateCase.getSubcases());
		subcases.add(r -> null);
		return subcases;
	}

}
