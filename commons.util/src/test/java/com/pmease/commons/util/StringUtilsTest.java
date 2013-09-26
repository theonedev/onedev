package com.pmease.commons.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class StringUtilsTest {

	@Test public void testCameCaseToLowerCaseWithUnderscore() {
		String input = null;
		assertTrue(StringUtils.camelCaseToLowerCaseWithUnderscore(input) == null);
		
		input = "FirstSecondThird";
		assertEquals(StringUtils.camelCaseToLowerCaseWithUnderscore(input), "first_second_third");
		
		input = "ILoveYou";
		assertEquals(StringUtils.camelCaseToLowerCaseWithUnderscore(input), "ilove_you");
	}

	@Test public void testCamelCaseToLowerCaseWithHyphen() {
		String input = null;
		assertTrue(StringUtils.camelCaseToLowerCaseWithHyphen(input) == null);
		
		input = "FirstSecondThird";
		assertEquals(StringUtils.camelCaseToLowerCaseWithHyphen(input), "first-second-third");
		
		input = "ILoveYou";
		assertEquals(StringUtils.camelCaseToLowerCaseWithHyphen(input), "ilove-you");
	}
}
