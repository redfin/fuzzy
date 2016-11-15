package com.redfin.fuzzy.cases;

import com.redfin.fuzzy.Any;
import com.redfin.fuzzy.Case;
import com.redfin.fuzzy.Suppliers;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

public class FloatNumericCase implements Case<Float> {

	public static final float MAX_GENERATED = 1e38f;
	public static final float MIN_GENERATED = -MAX_GENERATED;

	private Float min;
	private Float max;

	private final Set<Double> excluding = new HashSet<>();

	public Case<Float> inRange(float minInclusive, float maxInclusive) {
		if(maxInclusive <= minInclusive) {
			throw new IllegalArgumentException("minInclusive must be less than maxInclusive.");
		}

		min = minInclusive;
		max = maxInclusive;

		return this;
	}

	public Case<Float> lessThan(float maxExclusive) {
		max = maxExclusive;
		min = null;
		excluding.add((double)maxExclusive);

		return this;
	}

	public Case<Float> lessThanOrEqualTo(float maxInclusive) {
		max = maxInclusive;
		min = null;

		return this;
	}

	public Case<Float> greaterThan(float minExclusive) {
		max = null;
		min = minExclusive;
		excluding.add((double)minExclusive);

		return this;
	}

	public Case<Float> greaterThanOrEqualTo(float minInclusive) {
		max = null;
		min = minInclusive;

		return this;
	}

	@Override
	public Case<Float> excluding(Iterable<Float> values) {
		if(values != null)
			for(Float f : values)
				if(f != null)
					excluding.add(f.doubleValue());

		return this;
	}

	@Override
	public Set<Function<Random, Float>> getSuppliers() {
		Case<Double> baseCase = Any
			.doublePrecisionNumber()
			.inRange(min == null ? MIN_GENERATED : min, max == null ? MAX_GENERATED : max)
			.excluding(excluding);

		return Suppliers.mapOutput(baseCase.getSuppliers(), Double::floatValue);
	}
}
