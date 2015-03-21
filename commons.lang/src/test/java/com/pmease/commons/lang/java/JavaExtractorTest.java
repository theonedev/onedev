package com.pmease.commons.lang.java;

import org.junit.Test;

import com.pmease.commons.lang.ExtractorTest;

public class JavaExtractorTest extends ExtractorTest {

	@Test
	public void testComposite() {
		assertSymbol(readFile("composite.syms"), 
				new JavaExtractor().extract(readFile("composite.src")));
	}

	@Test
	public void testPackageInfo() {
		assertSymbol(readFile("package-info.syms"), 
				new JavaExtractor().extract(readFile("package-info.src")));
	}
	
	@Test
	public void testLCount() {
		assertSymbol(readFile("LCount.syms"), 
				new JavaExtractor().extract(readFile("LCount.src")));
	}
	
	@Test
	public void testResource() {
		assertSymbol(readFile("Resource.syms"), 
				new JavaExtractor().extract(readFile("Resource.src")));
	}

}
