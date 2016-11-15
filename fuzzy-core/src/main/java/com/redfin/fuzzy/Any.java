package com.redfin.fuzzy;

import com.redfin.fuzzy.cases.DoubleNumericCase;
import com.redfin.fuzzy.cases.EnumCase;
import com.redfin.fuzzy.cases.FloatNumericCase;
import com.redfin.fuzzy.cases.NullableCase;
import com.redfin.fuzzy.cases.NumericCase;
import com.redfin.fuzzy.cases.StringCase;
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
	public static Case<Byte> positiveByteInteger() { return NumericCase.ofBytes().greaterThanOrEqualTo((byte)0); }
	public static Case<Byte> positiveNonZeroByteInteger() { return NumericCase.ofBytes().greaterThanOrEqualTo((byte)1); }
	public static Case<Byte> negativeByteInteger() { return NumericCase.ofBytes().lessThanOrEqualTo((byte)-1); }

	public static NumericCase<Short> shortInteger() { return NumericCase.ofShorts(); }
	public static Case<Short> positiveShortInteger() { return NumericCase.ofShorts().greaterThanOrEqualTo((short)0); }
	public static Case<Short> positiveNonZeroShortInteger() { return NumericCase.ofShorts().greaterThanOrEqualTo((short)1); }
	public static Case<Short> negativeShortInteger() { return NumericCase.ofShorts().lessThanOrEqualTo((short)-1); }

	public static NumericCase<Integer> integer() { return NumericCase.ofIntegers(); }
	public static Case<Integer> positiveInteger() { return NumericCase.ofIntegers().greaterThanOrEqualTo(0); }
	public static Case<Integer> positiveNonZeroInteger() { return NumericCase.ofIntegers().greaterThanOrEqualTo(1); }
	public static Case<Integer> negativeInteger() { return NumericCase.ofIntegers().lessThanOrEqualTo(-1); }

	public static NumericCase<Long> longInteger() { return NumericCase.ofLongs(); }
	public static Case<Long> positiveLongInteger() { return NumericCase.ofLongs().greaterThanOrEqualTo(0L); }
	public static Case<Long> positiveNonZeroLongInteger() { return NumericCase.ofLongs().greaterThanOrEqualTo(1L); }
	public static Case<Long> negativeLongInteger() { return NumericCase.ofLongs().lessThanOrEqualTo(-1L); }

	public static DoubleNumericCase doublePrecisionNumber() { return new DoubleNumericCase(); }
	public static Case<Double> doublePrecisionFraction() { return new DoubleNumericCase().inRange(0, 1); }
	public static Case<Double> positiveDoublePrecisionNumber() { return new DoubleNumericCase().greaterThanOrEqualTo(0); }
	public static Case<Double> positiveNonZeroDoublePrecisionNumber() { return new DoubleNumericCase().greaterThan(0); }
	public static Case<Double> negativeDoublePrecisionNumber() { return new DoubleNumericCase().lessThan(0); }

	public static FloatNumericCase singlePrecisionNumber() { return new FloatNumericCase(); }
	public static Case<Float> singlePrecisionFraction() { return new FloatNumericCase().inRange(0, 1); }
	public static Case<Float> positiveSinglePrecisionNumber() { return new FloatNumericCase().greaterThanOrEqualTo(0); }
	public static Case<Float> positiveNonZeroSinglePrecisionNumber() { return new FloatNumericCase().greaterThan(0); }
	public static Case<Float> negativeSinglePrecisionNumber() { return new FloatNumericCase().lessThan(0); }

	public static StringCase string() { return new StringCase(); }

	public static <T extends Enum> EnumCase<T> enumValueFrom(Class<T> enumClass) { return new EnumCase<>(enumClass); }

}
