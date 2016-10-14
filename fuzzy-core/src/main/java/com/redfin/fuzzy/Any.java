package com.redfin.fuzzy;

import com.redfin.fuzzy.cases.StringCase;
import com.redfin.fuzzy.cases.NullableCase;
import com.redfin.fuzzy.cases.NumericCase;
import com.redfin.fuzzy.cases.UnionCase;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

public class Any {

	@SafeVarargs
	public static <T> Case<T> of(Case<T>... cases) {
		return new UnionCase<>(cases);
	}

	@SafeVarargs
	public static <T> Case<T> of(Function<Random, T>... cases) { return Cases.of(cases); }

	@SafeVarargs
	public static <T> Case<T> of(Supplier<T>... cases) { return Cases.of(cases); }

	@SafeVarargs
	public static <T> Case<T> of(T... literalCases) { return Cases.of(literalCases); }

	public static <T> Case<T> nullableOf(Case<T> delegateCase) {
		return new NullableCase<>(delegateCase);
	}

	public static <T> Case<T> nullableOf(Supplier<Case<T>> delegateCase) {
		return nullableOf(Preconditions.checkNotNull(delegateCase).get());
	}

	public static NumericCase<Byte> byteInteger() { return NumericCase.ofBytes(); }
	public static NumericCase<Short> shortInteger() { return NumericCase.ofShorts(); }
	public static NumericCase<Integer> integer() {
		return NumericCase.ofIntegers();
	}
	public static NumericCase<Long> longInteger() { return NumericCase.ofLongs(); }

	public static StringCase string() {
		return new StringCase();
	}

}
