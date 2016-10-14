package com.redfin.fuzzy.cases;

import com.redfin.fuzzy.Preconditions;
import com.redfin.fuzzy.Case;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

public abstract class NumericCase<T extends Number> implements Case<T> {

	private T min;
	private T max;

	public Case<T> inRange(T minInclusive, T maxInclusive) {
		Preconditions.checkNotNull(minInclusive);
		Preconditions.checkNotNull(maxInclusive);

		min = minInclusive;
		max = maxInclusive;

		return this;
	}

	public Case<T> lessThan(T maxInclusive) {
		Preconditions.checkNotNull(maxInclusive);
		min = null;
		max = maxInclusive;
		return this;
	}

	public Case<T> greaterThan(T minInclusive) {
		Preconditions.checkNotNull(minInclusive);
		min = minInclusive;
		max = null;
		return this;
	}

	@Override
	public Set<Function<Random, T>> getSuppliers() {
		Set<Function<Random, T>> suppliers = new HashSet<>(3);
		final T zero = i2t(0);

		// Negative
		if(min == null || lt(min, zero)) {
			if(min == null) {
				suppliers.add(r -> negate(abs(rng(r))));
			}
			else {
				final T max = negate(min);
				suppliers.add(r -> negate(rngLessThan(r, max)));
			}
		}

		// Positive
		if(max == null || lt(zero, max)) {
			if(max == null) {
				suppliers.add(r -> abs(rng(r)));
			}
			else {
				final T max = this.max;
				suppliers.add(r -> rngLessThan(r, max));
			}
		}

		// Zero
		if(
			(min == null || lt(min, zero) || zero.equals(min)) &&
			(max == null || lt(zero, max) || zero.equals(max))
		) {
			suppliers.add(r -> zero);
		}

		return suppliers;
	}

	/*package*/ abstract T negate(T t);
	/*package*/ abstract T abs(T t);
	/*package*/ abstract T i2t(int i);
	/*package*/ abstract boolean lt(T a, T b);

	/*package*/ abstract T rng(Random random);
	/*package*/ abstract T rngLessThan(Random random, T maxInclusive);

	public static NumericCase<Byte> ofBytes() {
		return new NumericCase<Byte>() {
			@Override protected Byte negate(Byte b) { return (byte) -b; }
			@Override protected Byte abs(Byte b) { return (byte) (b < 0 ? -b : b); }
			@Override protected Byte i2t(int i) { return (byte)i; }
			@Override protected boolean lt(Byte a, Byte b) { return a < b; }
			@Override protected Byte rng(Random random) { byte[] b = new byte[1]; random.nextBytes(b); return b[0]; }

			@Override
			protected Byte rngLessThan(Random random, Byte maxInclusive) {
				// Uniformity of distribution is not a huge deal here
				return (byte)((1 + random.nextInt(maxInclusive)) & 0x7F);
			}
		};
	}

	public static NumericCase<Short> ofShorts() {
		return new NumericCase<Short>() {
			@Override protected Short negate(Short s) { return (short) -s; }
			@Override protected Short abs(Short s) { return (short) (s < 0 ? -s : s); }
			@Override protected Short i2t(int i) { return (short)i; }
			@Override protected boolean lt(Short a, Short b) { return a < b; }

			@Override
			protected Short rng(Random random) {
				byte[] b = new byte[2]; random.nextBytes(b);
				return (short)((int)b[0] << 8 & (int)b[1]);
			}

			@Override
			protected Short rngLessThan(Random random, Short maxInclusive) {
				// Uniformity of distribution is not a huge deal here
				return (short)((1 + random.nextInt(maxInclusive)) & 0x7FFF);
			}
		};
	}

	public static NumericCase<Integer> ofIntegers() {
		return new NumericCase<Integer>() {
			@Override protected Integer negate(Integer integer) { return -integer; }
			@Override protected Integer abs(Integer integer) { int i = integer; return i < 0 ? -i : i; }
			@Override protected Integer i2t(int i) { return i; }
			@Override protected boolean lt(Integer a, Integer b) { return a < b; }
			@Override protected Integer rng(Random random) { return random.nextInt(); }

			@Override
			protected Integer rngLessThan(Random random, Integer maxInclusive) {
				return 1 + random.nextInt(maxInclusive);
			}
		};
	}

	public static NumericCase<Long> ofLongs() {
		return new NumericCase<Long>() {
			@Override protected Long negate(Long lng) { return -lng; }
			@Override protected Long abs(Long lng) { long l = lng; return l < 0 ? -l : l; }
			@Override protected Long i2t(int i) { return (long) i; }
			@Override protected boolean lt(Long a, Long b) { return a < b; }
			@Override protected Long rng(Random random) { return random.nextLong(); }

			@Override
			protected Long rngLessThan(Random random, Long maxInclusive) {
				// Deliberately pick a long integer that needs more than 32 bits, if appropriate
				if(maxInclusive > 0x7FFFFFFF) {
					// Uniformity of distribution isn't a huge deal here, so we'll just get a random nonnegative long
					// and shift it right until it's >= max.
					long l = random.nextLong() & 0x7FFFFFFFFFFFFFFFL;
					while(l >= maxInclusive) {
						l >>= 1;
					}
					return l;
				}
				else {
					return (long) 1 + random.nextInt(maxInclusive.intValue());
				}
			}
		};
	}

}
