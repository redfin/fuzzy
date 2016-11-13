package com.redfin.fuzzy.cases;

import com.redfin.fuzzy.Any;
import com.redfin.fuzzy.Case;
import com.redfin.fuzzy.Literal;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ExcludingCaseTest {

	@Test
	public void testExcluding() {
		AtomicInteger i = new AtomicInteger(0);
		ExcludingCase<Integer> subject = new ExcludingCase<>(Any.of(i::incrementAndGet), 0, 1);

		assertEquals(Collections.singleton(2), subject.generateAllOnce());
	}

	@Test
	public void testExcludingMaxAttempts() {
		ExcludingCase<Integer> subject = new ExcludingCase<>(Literal.value(5), 5);

		try {
			subject.generateAllOnce();
			fail("Expected IllegalStateException");
		}
		catch(IllegalStateException e) {
			// expected
		}
	}

	@Test
	public void testExcludingLocalImplementation() {
		AtomicInteger i = new AtomicInteger(0);
		Case<Integer> subject = new ExcludingCase<>(Any.of(i::incrementAndGet), 0, 1).excluding(2);

		assertEquals(Collections.singleton(3), subject.generateAllOnce());
	}

}
