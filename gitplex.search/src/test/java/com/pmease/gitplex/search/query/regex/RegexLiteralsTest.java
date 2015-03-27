package com.pmease.gitplex.search.query.regex;

import static org.junit.Assert.*;

import org.junit.Test;

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
	}

}
