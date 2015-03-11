package com.pmease.commons.lang.java;

import org.junit.Test;

import com.pmease.commons.lang.AnalyzerTest;

public class JavaAnalyzerTest extends AnalyzerTest {

	@Test
	public void testComposite() {
		assertOutline(readFile("composite.outline"), 
				new JavaAnalyzer().analyze(readFile("composite.source")));
	}

	@Test
	public void testPackageInfo() {
		assertOutline(readFile("package-info.outline"), 
				new JavaAnalyzer().analyze(readFile("package-info.source")));
	}
	
	@Test
	public void testLCount() {
		assertOutline(readFile("LCount.outline"), 
				new JavaAnalyzer().analyze(readFile("LCount.source")));
	}
	
	@Test
	public void testResource() {
		assertOutline(readFile("Resource.outline"), 
				new JavaAnalyzer().analyze(readFile("Resource.source")));
	}
	
}
