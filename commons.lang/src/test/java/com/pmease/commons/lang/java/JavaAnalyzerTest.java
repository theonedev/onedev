package com.pmease.commons.lang.java;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.Resources;

public class JavaAnalyzerTest {

	@Test
	public void test() {
		try {
			String source = Resources.toString(
					Resources.getResource(JavaAnalyzerTest.class, "source.txt"), 
					Charset.forName("UTF8"));
			String outline = Resources.toString(
					Resources.getResource(JavaAnalyzerTest.class, "outline.txt"), 
					Charset.forName("UTF8"));
			Assert.assertEquals(outline, new JavaAnalyzer().analyze(source).toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}

	@Test
	public void testAwtComponent() {
		try {
			String content = FileUtils.readFileToString(new File("w:\\temp\\Component.java"));
			long time = System.currentTimeMillis();
			for (int i=0; i<100; i++) {
				new JavaAnalyzer().analyze(content);
			}
			System.out.println(System.currentTimeMillis()-time);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
