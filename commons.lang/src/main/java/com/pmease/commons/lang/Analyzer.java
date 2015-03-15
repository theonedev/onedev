package com.pmease.commons.lang;

public interface Analyzer {
	AnalyzeResult analyze(String text) throws AnalyzeException;
	
	boolean accept(String fileName);
	
	String getVersion();
}
