package com.redfin.fuzzy.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.redfin.fuzzy.Any;
import com.redfin.fuzzy.Generator;
import com.redfin.fuzzy.junit.FuzzyRule;
import java.time.LocalDate;
import org.junit.Rule;
import org.junit.Test;

public class ToDoTaskFactoryTest {

	public @Rule FuzzyRule fuzzyRule = FuzzyRule.REPORTING_ALL_FAILURES;

	@Test
	public void testCreate() {
		Generator<String> description = Generator.of(Any.string()
			.nonEmpty()
			.withOnlyAlphabetChars()
		);

		Generator<Integer> dueInDays = Generator.of(Any.integer());

		ToDoTask task = ToDoTaskFactory.create(description.get(), dueInDays.get());

		// Make sure the properties are copied over to the task correctly.
		assertEquals(description.get(), task.getDescription());
		assertEquals(LocalDate.now().plusDays(dueInDays.get()), task.getDue());

		// Task due dates should always be in the future.
		assertTrue(task.getDue().isAfter(LocalDate.now()));
	}

}
