package com.pmease.commons.lang.analyzers.java;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.pmease.commons.lang.TokenizedLine;
import com.pmease.commons.lang.tokenizers.clike.JavaTokenizer;

public class JavaAnalyzerTest {

	@Test
	public void test() {
		try {
			String text = FileUtils.readFileToString(new File("W:\\java8\\test\\src\\com\\example\\Test.java"));
			List<TokenizedLine> lines = new JavaTokenizer().tokenize(text);
			System.out.println(new JavaAnalyzer().analyze(lines));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
