package com.pmease.commons.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class StringUtilsTest {

	@Test public void shouldConvertCameCaseToLowerCaseWithUnderscore() {
		String input = null;
		assertEquals(StringUtils.camelCaseToLowerCaseWithUnderscore(input), null);
		
		input = "FirstSecondThird";
		assertEquals(StringUtils.camelCaseToLowerCaseWithUnderscore(input), "first_second_third");
		
		input = "ILoveYou";
		assertEquals(StringUtils.camelCaseToLowerCaseWithUnderscore(input), "i_love_you");
	}

	@Test public void shouldConvertCamelCaseToLowerCaseWithHyphen() {
		String input = null;
		assertEquals(StringUtils.camelCaseToLowerCaseWithHyphen(input), null);
		
		input = "FirstSecondThird";
		assertEquals(StringUtils.camelCaseToLowerCaseWithHyphen(input), "first-second-third");
		
		input = "ILoveYou";
		assertEquals(StringUtils.camelCaseToLowerCaseWithHyphen(input), "i-love-you");
	}
}
