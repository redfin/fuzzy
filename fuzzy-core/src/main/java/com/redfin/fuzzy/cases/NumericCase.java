package com.redfin.fuzzy.cases;

import com.redfin.fuzzy.Any;
import com.redfin.fuzzy.Case;
import com.redfin.fuzzy.Generator;
import com.redfin.fuzzy.Preconditions;
import com.redfin.fuzzy.Suppliers;

import java.util.Collections;
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

	public Case<T> greaterThan(Generator<T> minExclusive) {
		Preconditions.checkNotNull(minExclusive);
		return () -> Suppliers.pairwisePermutations(
			Collections.<Function<Random, T>>singleton(random -> minExclusive.get()),
			newCase().greaterThan(i2t(1)).getSuppliers(),
			(random, base, distance) -> add(base, distance)
		);
	}

	public Case<T> lessThan(Generator<T> maxExclusive) {
		Preconditions.checkNotNull(maxExclusive);
		return () -> Suppliers.pairwisePermutations(
			Collections.<Function<Random, T>>singleton(random -> maxExclusive.get()),
			newCase().greaterThan(i2t(1)).getSuppliers(),
			(random, base, distance) -> add(base, negate(distance))
		);
	}

	public WithinChain<T> within(T range) {
		Preconditions.checkNotNull(range);
		if(lt(range, i2t(1)))
			throw new IllegalArgumentException();

		return new WithinChain<T>(newCase(), range);
	}

	public static class WithinChain<T extends Number> {
		private final NumericCase<T> baseCase;
		private final T range;

		private WithinChain(NumericCase<T> baseCase, T range) { this.baseCase = baseCase; this.range = range; }

		public Case<T> of(Generator<T> number) {
			Preconditions.checkNotNull(number);
			return () -> Suppliers.pairwisePermutations(
				Collections.<Function<Random, T>>singleton(random -> number.get()),
				baseCase.inRange(baseCase.negate(range), range).getSuppliers(),
				(random, base, distance) -> baseCase.add(base, distance)
			);
		}
	}

	@Override
	public Set<Function<Random, T>> getSuppliers() {
		Set<Function<Random, T>> suppliers = new HashSet<>(3);
		final T zero = i2t(0);

		// Negative
		if(min != null && max != null && lt(max, zero)) {
			final T d = add(max, negate(min));
			suppliers.add(r -> add(min, rngLessThan(r, d)));
		}
		else if(min == null || lt(min, zero)) {
			if(min == null) {
				suppliers.add(r -> negate(abs(rng(r))));
			}
			else {
				final T max = negate(min);
				suppliers.add(r -> negate(rngLessThan(r, max)));
			}
		}

		// Positive
		if(max != null && min != null && lt(zero, min)) {
			final T d = add(max, negate(min));
			suppliers.add(r -> add(min, rngLessThan(r, d)));
		}
		else if(max == null || lt(zero, max)) {
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

		// Cover the specific boundaries
		if(min != null && !min.equals(zero)) suppliers.add(r -> min);
		if(max != null && !max.equals(zero)) suppliers.add(r -> max);

		return suppliers;
	}

	protected abstract NumericCase<T> newCase();

	protected abstract T add(T a, T b);
	protected abstract T negate(T t);
	protected abstract T abs(T t);
	protected abstract T i2t(int i);
	protected abstract boolean lt(T a, T b);

	protected abstract T rng(Random random);
	protected abstract T rngLessThan(Random random, T maxInclusive);

	public static NumericCase<Byte> ofBytes() {
		return new NumericCase<Byte>() {
			@Override protected NumericCase<Byte> newCase() { return Any.byteInteger(); }

			@Override protected Byte add(Byte a, Byte b) {
				long r = a.longValue() + b.longValue();
				return r < Byte.MIN_VALUE ? Byte.MIN_VALUE : (r > Byte.MAX_VALUE ? Byte.MAX_VALUE : (byte)r);
			}

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
			@Override protected NumericCase<Short> newCase() { return Any.shortInteger(); }

			@Override protected Short add(Short a, Short b) {
				long r = a.longValue() + b.longValue();
				return r < Short.MIN_VALUE ? Short.MIN_VALUE : (r > Short.MAX_VALUE ? Short.MAX_VALUE : (short)r);
			}

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
			@Override protected NumericCase<Integer> newCase() { return Any.integer(); }

			@Override protected Integer add(Integer a, Integer b) {
				long r = a.longValue() + b.longValue();
				return r < Integer.MIN_VALUE ? Integer.MIN_VALUE : (r > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)r);
			}

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
			@Override protected NumericCase<Long> newCase() { return Any.longInteger(); }

			// TODO: cap overflow
			@Override protected Long add(Long a, Long b) { return a + b; }

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
