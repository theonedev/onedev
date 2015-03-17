package com.pmease.commons.lang.java;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.pmease.commons.lang.AnalyzerTest;
import com.pmease.commons.util.FileUtils;

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

	@Test
	public void testMine() throws IOException {
		for (File file: FileUtils.listFiles(new File("w:\\temp\\commons"), "**/*.java")) {
			System.out.println(file);
			new JavaAnalyzer().analyze(FileUtils.readFileToString(file));
		}
	}
}
