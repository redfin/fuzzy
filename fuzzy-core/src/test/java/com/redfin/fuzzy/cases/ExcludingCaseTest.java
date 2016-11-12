package com.redfin.fuzzy.cases;

import static org.junit.Assert.*;

import com.redfin.fuzzy.Any;
import com.redfin.fuzzy.Literal;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class ExcludingCaseTest {

	@Test
	public void testExcluding() {
		AtomicInteger i = new AtomicInteger(0);
		ExcludingCase<Integer> subject = new ExcludingCase<>(Any.of(i::incrementAndGet), 0, 1);

		Set<Integer> expected = new HashSet<>();
		expected.add(2);

		assertEquals(expected, subject.resolveAllOnce());
	}

	@Test
	public void testExcludingMaxAttempts() {
		ExcludingCase<Integer> subject = new ExcludingCase<>(Literal.value(5), 5);

		try {
			subject.resolveAllOnce();
			fail("Expected IllegalStateException");
		}
		catch(IllegalStateException e) {
			// expected
		}
	}

}
