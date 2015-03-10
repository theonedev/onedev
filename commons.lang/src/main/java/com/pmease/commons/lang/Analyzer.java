package com.pmease.commons.lang;

public interface Analyzer {
	Outline analyze(String text) throws AnalyzeException;
}
