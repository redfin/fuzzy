package com.redfin.fuzzy.cases;

import com.redfin.fuzzy.Preconditions;
import com.redfin.fuzzy.Case;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

public class NullableCase<T> implements Case<T> {

	private final Case<T> delegateCase;

	public NullableCase(Case<T> delegateCase) {
		this.delegateCase = Preconditions.checkNotNull(delegateCase);
	}

	@Override
	public Set<Function<Random, T>> getSuppliers() {
		Set<Function<Random, T>> suppliers = new HashSet<>();
		suppliers.addAll(delegateCase.getSuppliers());
		suppliers.add(r -> null);
		return suppliers;
	}

}
