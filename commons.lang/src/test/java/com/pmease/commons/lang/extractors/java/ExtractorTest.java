package com.pmease.commons.lang.extractors.java;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

import com.google.common.io.Resources;
import com.pmease.commons.lang.extractors.Symbol;

public class ExtractorTest {

	protected void assertSymbol(String expected, List<Symbol> symbols) {
		StringBuilder builder = new StringBuilder();
		for (Symbol symbol: symbols) {
			if (symbol.getParent() == null)
				builder.append(symbol.describe(symbols));
		}
		
		Assert.assertEquals(StringUtils.replace(expected, "\r", "").trim(), builder.toString().toString().trim());
	}

	protected String readFile(String fileName) {
		try {
			return Resources.toString(Resources.getResource(getClass(), fileName), Charset.forName("UTF8"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
