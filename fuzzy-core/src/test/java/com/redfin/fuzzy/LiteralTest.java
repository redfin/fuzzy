package com.redfin.fuzzy;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.Test;

public class LiteralTest {

	private static final long SEED = LiteralTest.class.hashCode();

	@Test(expected = NullPointerException.class)
	public void testValueBoundToNull() {
		Literal.valueBoundTo(null);
	}

	@Test
	public void testValueBoundTo() {
		Context.init(
			CaseCompositionMode.EACH_SUBCASE_AT_LEAST_ONCE,
			SEED
		);

		Set<Integer> generatedValues = new HashSet<>();

		try {
			do {
				Generator<Integer> valueOne = Generator.of(Any.of(1, 2, 3));
				Generator<Integer> valueTwo = Generator.of(
					Literal
						.valueBoundTo(valueOne)
						.map(i -> i * 2)
				);

				generatedValues.add(valueOne.get());

				int expected = valueOne.get() * 2;
				int actual = valueTwo.get();

				assertEquals(expected, actual);
			}
			while(Context.next());
		}
		finally {
			Context.cleanUp();
		}

		assertEquals(FuzzyUtil.setOf(1, 2, 3), generatedValues);
	}

	@Test
	public void testValueBoundToDescription() {
		Context.init(CaseCompositionMode.EACH_SUBCASE_AT_LEAST_ONCE, SEED);
		try {
			Subcase<String> test = new Subcase<String>() {
				@Override
				public String generate(Random random) {
					return "Hello";
				}

				@Override
				public void describeTo(StringBuilder sink, String value) {
					sink.append("World!");
				}
			};

			Generator<String> valueOne = Generator.of(Cases.of(test));
			Generator<String> valueTwo = Generator.of(Literal.valueBoundTo(valueOne));

			StringBuilder descriptionOne = new StringBuilder();
			valueOne.getCurrentSubcase().describeTo(descriptionOne, valueOne.get());

			StringBuilder descriptionTwo = new StringBuilder();
			valueTwo.getCurrentSubcase().describeTo(descriptionTwo, valueTwo.get());

			assertEquals(descriptionOne.toString(), descriptionTwo.toString());
		}
		finally {
			Context.cleanUp();
		}
	}

	@Test
	public void testConstructorForCoverage() { new Literal(); }

}
