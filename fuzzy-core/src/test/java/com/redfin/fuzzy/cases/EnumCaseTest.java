package com.redfin.fuzzy.cases;

import com.redfin.fuzzy.Any;
import com.redfin.fuzzy.Context;
import com.redfin.fuzzy.Generator;
import com.redfin.fuzzy.Util;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class EnumCaseTest {

	@Test
	public void testBasicCase() {
		Set<TestEnum> actual = new HashSet<>();
		int count = 0;

		Context.init(getClass().getName().hashCode());
		do {
			Generator<TestEnum> enumValue = Generator.of(Any.enumValueFrom(TestEnum.class));

			count++;
			actual.add(enumValue.get());
		}
		while(Context.next());
		Context.cleanUp();

		assertEquals(4, count);
		assertEquals(Util.setOf(TestEnum.A, TestEnum.B, TestEnum.C, TestEnum.D), actual);
	}

	@Test
	public void testExcluding() {
		Set<TestEnum> actual = new HashSet<>();
		int count = 0;

		Context.init(getClass().getName().hashCode());
		do {
			Generator<TestEnum> enumValue = Generator.of(Any
				.enumValueFrom(TestEnum.class)
				.excluding(TestEnum.B, TestEnum.C)
			);

			count++;
			actual.add(enumValue.get());
		}
		while(Context.next());
		Context.cleanUp();

		assertEquals(2, count);
		assertEquals(Util.setOf(TestEnum.A, TestEnum.D), actual);
	}

	@Test(expected = IllegalStateException.class)
	public void testAllExcluded() {
		Any
			.enumValueFrom(TestEnum.class)
			.excluding(TestEnum.A, TestEnum.B, TestEnum.C, TestEnum.D)
			.generateAllOnce();
	}

	private enum TestEnum { A, B, C, D }

}
