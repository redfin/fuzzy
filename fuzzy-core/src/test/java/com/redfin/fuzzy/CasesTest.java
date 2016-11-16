package com.redfin.fuzzy;

import org.junit.Test;

import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CasesTest {

	@Test
	public void testComposePairwise() {
		Case<Integer> composedCase = Cases.compose(
			CaseCompositionMode.PAIRWISE_PERMUTATIONS_OF_SUBCASES,
			new Case[] { Any.of(2, 3), Any.of(10, 100, 1000) },
			(random, values) -> (int)values[0] * (int)values[1]
		);

		Set<Integer> actuals = composedCase.generateAllOnce();

		assertEquals(Util.setOf(20, 200, 2000, 30, 300, 3000), actuals);
	}

	@Test
	public void testComposeAtLeastOnce() {
		Case<Integer> composedCase = Cases.compose(
			CaseCompositionMode.EACH_SUBCASE_AT_LEAST_ONCE,
			new Case[] { Any.of(2, 3), Any.of(10, 100, 1000) },
			(random, values) -> (int)values[0] * (int)values[1]
		);

		Set<Integer> actuals = composedCase.generateAllOnce();

		assertEquals(3, actuals.size());

		Set<Integer> expecteds = Util.setOf(20, 200, 2000, 30, 300, 3000);
		assertFalse(expecteds.stream().anyMatch(i -> !expecteds.contains(i)));
	}

	@Test
	public void testCompose1() {
		Case<String> subject = Cases.compose(Literal.value(1), (random, a) -> "" + a);
		assertEquals(Util.setOf("1"), subject.generateAllOnce());
	}

	@Test
	public void testCompose2() {
		Case<String> subject = Cases.compose(
			CaseCompositionMode.EACH_SUBCASE_AT_LEAST_ONCE,
			Literal.value(1), Literal.value(2),
			(random, a, b) -> "" + a + b
		);
		assertEquals(Util.setOf("12"), subject.generateAllOnce());
	}

	@Test
	public void testCompose3() {
		Case<String> subject = Cases.compose(
			CaseCompositionMode.EACH_SUBCASE_AT_LEAST_ONCE,
			Literal.value(1), Literal.value(2), Literal.value(3),
			(random, a, b, c) -> "" + a + b + c
		);
		assertEquals(Util.setOf("123"), subject.generateAllOnce());
	}

	@Test
	public void testCompose4() {
		Case<String> subject = Cases.compose(
			CaseCompositionMode.EACH_SUBCASE_AT_LEAST_ONCE,
			Literal.value(1), Literal.value(2), Literal.value(3), Literal.value(4),
			(random, a, b, c, d) -> "" + a + b + c + d
		);
		assertEquals(Util.setOf("1234"), subject.generateAllOnce());
	}

	@Test
	public void testCompose5() {
		Case<String> subject = Cases.compose(
			CaseCompositionMode.EACH_SUBCASE_AT_LEAST_ONCE,
			Literal.value(1), Literal.value(2), Literal.value(3), Literal.value(4), Literal.value(5),
			(random, a, b, c, d, e) -> "" + a + b + c + d + e
		);
		assertEquals(Util.setOf("12345"), subject.generateAllOnce());
	}

	@Test
	public void testCompose6() {
		Case<String> subject = Cases.compose(
			CaseCompositionMode.EACH_SUBCASE_AT_LEAST_ONCE,
			Literal.value(1), Literal.value(2), Literal.value(3), Literal.value(4), Literal.value(5), Literal.value(6),
			(random, a, b, c, d, e, f) -> "" + a + b + c + d + e + f
		);
		assertEquals(Util.setOf("123456"), subject.generateAllOnce());
	}

	@Test
	public void testCompose7() {
		Case<String> subject = Cases.compose(
			CaseCompositionMode.EACH_SUBCASE_AT_LEAST_ONCE,
			Literal.value(1), Literal.value(2), Literal.value(3), Literal.value(4), Literal.value(5), Literal.value(6), Literal.value(7),
			(random, a, b, c, d, e, f, g) -> "" + a + b + c + d + e + f + g
		);
		assertEquals(Util.setOf("1234567"), subject.generateAllOnce());
	}

	@Test
	public void testCompose8() {
		Case<String> subject = Cases.compose(
			CaseCompositionMode.EACH_SUBCASE_AT_LEAST_ONCE,
			Literal.value(1), Literal.value(2), Literal.value(3), Literal.value(4), Literal.value(5), Literal.value(6), Literal.value(7), Literal.value(8),
			(random, a, b, c, d, e, f, g, i) -> "" + a + b + c + d + e + f + g + i
		);
		assertEquals(Util.setOf("12345678"), subject.generateAllOnce());
	}

	@Test
	public void testCompose9() {
		Case<String> subject = Cases.compose(
			CaseCompositionMode.EACH_SUBCASE_AT_LEAST_ONCE,
			Literal.value(1), Literal.value(2), Literal.value(3), Literal.value(4), Literal.value(5), Literal.value(6), Literal.value(7), Literal.value(8), Literal.value(9),
			(random, a, b, c, d, e, f, g, i, j) -> "" + a + b + c + d + e + f + g + i + j
		);
		assertEquals(Util.setOf("123456789"), subject.generateAllOnce());
	}

	@Test
	public void testMap() {
		Case<String> original = () -> Collections.singleton(r -> "1234");
		Case<Integer> mapped = Cases.map(original, Integer::new);

		Set<Function<Random, Integer>> suppliers = mapped.getSuppliers();
		assertEquals(1, suppliers.size());
		assertEquals(new Integer(1234), suppliers.stream().findFirst().get().apply(null));
	}

	@Test
	public void testMapWithMultipleSuppliers() {
		Case<String> original = () -> Util.setOf(
			r -> "1234",
			r -> "5678"
		);

		Case<Integer> mapped = Cases.map(original, s -> -Integer.valueOf(s));
		Set<Function<Random, Integer>> suppliers = mapped.getSuppliers();

		assertEquals(2, suppliers.size());

		Set<Integer> values = suppliers.stream()
			.map(s -> s.apply(null))
			.collect(Collectors.toSet())
		;

		assertEquals(
			Util.setOf(-1234, -5678),
			values
		);
	}

	@Test
	public void testConstructorForCoverage() { new Cases(); }

}
