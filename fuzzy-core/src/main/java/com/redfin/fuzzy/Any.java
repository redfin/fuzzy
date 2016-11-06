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
	public static Case<Byte> positiveByteInteger() { return NumericCase.ofBytes().greaterThan((byte)0); }
	public static Case<Byte> positiveNonZeroByteInteger() { return NumericCase.ofBytes().greaterThan((byte)1); }
	public static Case<Byte> negativeByteInteger() { return NumericCase.ofBytes().lessThan((byte)-1); }

	public static NumericCase<Short> shortInteger() { return NumericCase.ofShorts(); }
	public static Case<Short> positiveShortInteger() { return NumericCase.ofShorts().greaterThan((short)0); }
	public static Case<Short> positiveNonZeroShortInteger() { return NumericCase.ofShorts().greaterThan((short)1); }
	public static Case<Short> negativeShortInteger() { return NumericCase.ofShorts().lessThan((short)-1); }

	public static NumericCase<Integer> integer() {
		return NumericCase.ofIntegers();
	}
	public static Case<Integer> positiveInteger() { return NumericCase.ofIntegers().greaterThan(0); }
	public static Case<Integer> positiveNonZeroInteger() { return NumericCase.ofIntegers().greaterThan(1); }
	public static Case<Integer> negativeInteger() { return NumericCase.ofIntegers().lessThan(-1); }

	public static NumericCase<Long> longInteger() { return NumericCase.ofLongs(); }
	public static Case<Long> positiveLongInteger() { return NumericCase.ofLongs().greaterThan(0L); }
	public static Case<Long> positiveNonZeroLongInteger() { return NumericCase.ofLongs().greaterThan(1L); }
	public static Case<Long> negativeLongInteger() { return NumericCase.ofLongs().lessThan(-1L); }

	public static StringCase string() {
		return new StringCase();
	}

}
