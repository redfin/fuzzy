package com.redfin.fuzzy.cases;

import com.redfin.fuzzy.Case;
import com.redfin.fuzzy.Subcase;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class DoubleNumericCase implements Case<Double> {

	public static final double MAX_GENERATED = 1e308d;
	public static final double MIN_GENERATED = -MAX_GENERATED;

	public static final double MAX_GENERATED_FRACTIONAL = 0.99999999d;
	public static final double MIN_GENERATED_FRACTIONAL = 0.00000001d;

	private static final int MAX_ATTEMPTS = 100;

	private Double min;
	private Double max;

	private final Set<Double> excluding = new HashSet<>();

	public Case<Double> inRange(double minInclusive, double maxInclusive) {
		if(maxInclusive <= minInclusive) {
			throw new IllegalArgumentException("minInclusive must be less than maxInclusive.");
		}

		min = minInclusive;
		max = maxInclusive;

		return this;
	}

	public Case<Double> lessThan(double maxExclusive) {
		max = maxExclusive;
		min = null;
		excluding.add(max);

		return this;
	}

	public Case<Double> lessThanOrEqualTo(double maxInclusive) {
		max = maxInclusive;
		min = null;

		return this;
	}

	public Case<Double> greaterThan(double minExclusive) {
		min = minExclusive;
		max = null;
		excluding.add(min);

		return this;
	}

	public Case<Double> greaterThanOrEqualTo(double minInclusive) {
		max = null;
		min = minInclusive;

		return this;
	}

	@Override
	public DoubleNumericCase excluding(Iterable<Double> values) {
		if(values != null)
			for(Double d : values)
				if(d != null)
					excluding.add(d);

		return this;
	}

	private Subcase<Double> exclude(Subcase<Double> subcase) {
		return r -> {
			for(int i = 0; i < MAX_ATTEMPTS; i++) {
				double d = subcase.generate(r);
				if(!excluding.contains(d)) {
					return d;
				}
			}
			throw new IllegalStateException(
				"DoubleNumericCase could not generate a value that was not marked as excluded."
			);
		};
	}

	private static Subcase<Double> subcaseInRange(double min, double max) {
		return r -> min + (r.nextDouble() * (max - min));
	}

	@Override
	public Set<Subcase<Double>> getSubcases() {
		Set<Subcase<Double>> cases = new HashSet<>();

		// Zero
		if(!excluding.contains(0.0) && (min == null || min < 0) && (max == null || max > 0))
			cases.add(r -> 0.0);

		// < -1
		if(min == null)
			cases.add(subcaseInRange(MIN_GENERATED, -1));
		else if(min < -1)
			cases.add(subcaseInRange(min, max == null ? -1 : Math.min(max, -1)));

		// < 0 && > -1
		if((min == null || min < 0) && (max == null || max > -1))
			cases.add(subcaseInRange(
				min == null ? -MAX_GENERATED_FRACTIONAL : Math.max(min, -MAX_GENERATED_FRACTIONAL),
				max == null ? -MIN_GENERATED_FRACTIONAL : Math.min(max, -MIN_GENERATED_FRACTIONAL)
			));

		// > 1
		if(max == null)
			cases.add(subcaseInRange(1, MAX_GENERATED));
		else if(max > 1)
			cases.add(subcaseInRange(min == null ? 1 : Math.max(min, 1), max));

		// > 0 && < 1
		if((min == null || min < 1) && (max == null || max > 0))
			cases.add(subcaseInRange(
				min == null ? MIN_GENERATED_FRACTIONAL : Math.max(min, MIN_GENERATED_FRACTIONAL),
				max == null ? MAX_GENERATED_FRACTIONAL : Math.min(max, MAX_GENERATED_FRACTIONAL)
			));

		// Specific bounds
		if(min != null && !excluding.contains(min))
			cases.add(r -> min);
		if(max != null && !excluding.contains(max))
			cases.add(r -> max);

		return cases.stream().map(this::exclude).collect(Collectors.toSet());
	}
}
