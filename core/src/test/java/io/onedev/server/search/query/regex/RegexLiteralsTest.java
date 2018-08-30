package io.onedev.server.search.query.regex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.common.base.Splitter;

import io.onedev.server.search.code.query.regex.RegexLiterals;

public class RegexLiteralsTest {

	@Test
	public void test() {
		assertEquals("", new RegexLiterals("").toString());
		assertEquals("a", new RegexLiterals("a").toString());
		assertEquals("", new RegexLiterals(".").toString());
		assertEquals(".", new RegexLiterals("\\.").toString());
		assertEquals("\\", new RegexLiterals("\\\\").toString());
		assertEquals("ab&cd", new RegexLiterals("ab.cd").toString());
		assertEquals("abmcd|abncd", new RegexLiterals("ab[mn]cd").toString());
		assertEquals("ad|ab&bd|ab&cd|ac&bd|ac&cd", new RegexLiterals("a(b+|c)*d").toString());		
		assertEquals("a&b", new RegexLiterals("a\\d+\\wb").toString());		
		assertEquals("", new RegexLiterals("a*b*c*").toString());		
		assertEquals("abb&bbc", new RegexLiterals("ab{2,}c").toString());

		// should pick the most import part if the regex is too complex to be simplified
		String regex = "[ab1](cd)[pqr](hello)[xyz][ef3][gh4][ij5][kl6][mn7]";
		for (String each: Splitter.on("|").split(new RegexLiterals(regex).toString()))
			assertTrue(each.contains("cd&hello"));
	}

}
