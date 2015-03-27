package com.pmease.commons.lang;

public interface Extractor {
	
	Symbol extract(String text) throws ExtractException;
	
	boolean accept(String fileName);
	
	int getVersion();
}
