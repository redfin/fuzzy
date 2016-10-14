package com.redfin.fuzzy.pairwise;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.redfin.fuzzy.Util;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import org.junit.Test;

public class PairwiseTest {

	@Test
	public void testGeneratePairsSingleVariable() {
		Pairwise pw = new Pairwise<>(Collections.singletonList(
			Util.setOf("A", "B", "C")
		));
		PairSet ps = pw.generatePairs();
		assertEquals(0, ps.size());
	}

	@Test
	public void testGeneratePairsBasicCase() {

		Pairwise pw = new Pairwise<>(Arrays.asList(
			Util.setOf("A", "B", "C"),
			Util.setOf("d", "e", "f", "g"),
			Util.setOf("1", "2", "3")
		));

		PairSet ps = pw.generatePairs();

		assertEquals(3 * 7 + 4 * 3, ps.size());

		List<String> pairs = ps.toSet().stream()
			.map(p -> "" + p.p1.value + p.p2.value)
			.sorted()
			.collect(Collectors.toList())
		;

		assertEquals(
			Arrays.asList(
				"A1", "A2", "A3", "Ad", "Ae", "Af", "Ag",
				"B1", "B2", "B3", "Bd", "Be", "Bf", "Bg",
				"C1", "C2", "C3", "Cd", "Ce", "Cf", "Cg",
				"d1", "d2", "d3",
				"e1", "e2", "e3",
				"f1", "f2", "f3",
				"g1", "g2", "g3"
			),
			pairs
		);
	}

	@Test
	public void testGenerateSingleVariable() {
		Pairwise<?> pw = new Pairwise<>(Collections.singletonList(
			Util.setOf("A", "B", "C")
		));

		Stack<List<Object>> testCases = pw.generate();

		assertEquals(3, testCases.size());

		Set<String> actuals = new HashSet<>();
		for(List<Object> testCase : testCases) {
			actuals.add((String)testCase.get(0));
		}

		assertEquals(Util.setOf("A", "B", "C"), actuals);
	}

	@Test
	public void testGenerateBasicCase() {

		Set<String> p1 = Util.setOf("A", "B", "C");
		Set<String> p2 = Util.setOf("d", "e", "f", "g");
		Set<String> p3 = Util.setOf("1", "2", "3");
		Set<String> p4 = Util.setOf("X", "O");

		int combinations = (3 * 9) + (4 * 5) + (3 * 2);

		Pairwise<?> pw = new Pairwise<>(Arrays.asList(p1, p2, p3, p4));

		Stack<List<Object>> testCases = pw.generate();

		// Output the test cases
		System.out.println(testCases.size() + " test cases (" + combinations + " combinations)");
		System.out.println();

		System.out.println(" P1 | P2 | P3 | P4 ");
		System.out.println("----+----+----+----");

		for(List<Object> tc : testCases) {
			System.out.println(String.format(
				" %s  | %s  | %s  | %s",
				tc.get(0), tc.get(1), tc.get(2), tc.get(3)
			));
		}

		System.out.println();

		// We need at most the same number of test cases as there are combinations
		assertTrue(testCases.size() <= combinations);

		// Each test case needs to contain each parameter, and each parameter needs to be valid
		for(List<Object> tc : testCases) {
			assertNotNull(tc);
			assertEquals(4, tc.size());

			assertTrue(p1.contains(tc.get(0)));
			assertTrue(p2.contains(tc.get(1)));
			assertTrue(p3.contains(tc.get(2)));
			assertTrue(p4.contains(tc.get(3)));
		}

		// Every pair needs to have been included at least one.
		PairSet ps = pw.generatePairs();

		Set<String> expectedPairs = ps.toSet().stream()
			.map(p -> "" + p.p1.value + p.p2.value)
			.collect(Collectors.toSet());

		Set<String> actualPairs = new HashSet<>();
		for(List<Object> tc : testCases) {
			for(int i = 0; i < tc.size() - 1; i++) {
				for(int j = i + 1; j < tc.size(); j++) {
					actualPairs.add("" + tc.get(i) + tc.get(j));
				}
			}
		}

		assertEquals(expectedPairs, actualPairs);
	}

	@Test
	public void benchmark() {
		List<Set> params = Arrays.asList( // 4400 combinations
			Util.setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
			Util.setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
			Util.setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
			Util.setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
			Util.setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
			Util.setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
			Util.setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
			Util.setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
			Util.setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
			Util.setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
		);

		int[] sizes = new int[50];
		long start = System.nanoTime();

		for(int i = 0; i < sizes.length; i++) {
			sizes[i] = (new Pairwise<>(params)).generate().size();
		}

		double avgTime = (System.nanoTime() - start) / (double)sizes.length;

		System.out.println(String.format("Average time: %.2fms", avgTime * 1e-6));
		System.out.println(String.format("%,d test cases", sizes[0]));
		System.out.println();

		assertTrue(sizes[0] <= 4400);
	}

}
