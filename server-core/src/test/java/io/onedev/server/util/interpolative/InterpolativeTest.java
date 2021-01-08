package io.onedev.server.util.interpolative;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class InterpolativeTest {

	@Test
	public void test() {
		assertEquals("@", Interpolative.parse("@@").interpolateWith(it->"!"));
		assertEquals("@@", Interpolative.parse("@@@@").interpolateWith(it->it));
		assertEquals("hello", Interpolative.parse("@hello@").interpolateWith(it->it));
		assertEquals("helloworlddo", Interpolative.parse("hello@world@do").interpolateWith(it->it));
		assertEquals("1234", Interpolative.parse("1@2@@3@4").interpolateWith(it->it));
		assertEquals("1!!4", Interpolative.parse("1@2@@3@4").interpolateWith(it->"!"));
		assertEquals("1!-!4", Interpolative.parse("1@2@-@3@4").interpolateWith(it->"!"));
		try {
			Interpolative.parse("hello@world@@do");
			assertTrue(false);
		} catch (RuntimeException e) {
		}
		try {
			Interpolative.parse("@@@");
			assertTrue(false);
		} catch (RuntimeException e) {
		}
	}

}
