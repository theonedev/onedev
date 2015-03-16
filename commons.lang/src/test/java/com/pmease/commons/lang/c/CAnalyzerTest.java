package com.pmease.commons.lang.c;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.pmease.commons.lang.AnalyzeException;

public class CAnalyzerTest {

	@Test
	public void test() throws AnalyzeException, IOException {
		CAnalyzer analyzer = new CAnalyzer();
		System.out.println(analyzer.analyze(FileUtils.readFileToString(new File("W:\\linux\\arch\\alpha\\kernel\\core_cia.c"))).getSymbols().size());
	}

}
