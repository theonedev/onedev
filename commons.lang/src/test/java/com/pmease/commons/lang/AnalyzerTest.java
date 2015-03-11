package com.pmease.commons.lang;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

import com.google.common.io.Resources;
import com.pmease.commons.lang.java.JavaAnalyzerTest;

public class AnalyzerTest {

	protected void assertOutline(String expected, Outline outline) {
		Assert.assertEquals(StringUtils.replace(expected, "\r", "").trim(), outline.toString().trim());
	}

	protected String readFile(String fileName) {
		try {
			return Resources.toString(Resources.getResource(getClass(), fileName), Charset.forName("UTF8"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
