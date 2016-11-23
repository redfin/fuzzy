package com.redfin.fuzzy.cases;

import com.redfin.fuzzy.Case;
import com.redfin.fuzzy.FuzzyPreconditions;
import com.redfin.fuzzy.Subcase;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class EnumCase<T extends Enum> implements Case<T> {

	private final Class<T> enumClass;
	private final Set<T> excluded = new HashSet<>();

	public EnumCase(Class<T> enumClass) {
		this.enumClass = FuzzyPreconditions.checkNotNull(enumClass);
	}

	@Override
	public EnumCase<T> excluding(Iterable<T> values) {
		if(values != null) {
			for(T t : values)
				excluded.add(t);
		}
		return this;
	}

	@Override
	public Set<Subcase<T>> getSubcases() {
		Set<Subcase<T>> subcases = Arrays.stream(enumClass.getEnumConstants())
			.filter(t -> !excluded.contains(t))
			.map(t -> (Subcase<T>)(r -> t))
			.collect(Collectors.toSet());

		if(subcases.isEmpty())
			throw new IllegalStateException(String.format(
				"Cannot generate subcases for enum of type %s because all possible values have been excluded.",
				enumClass
			));
		else
			return subcases;
	}

}
