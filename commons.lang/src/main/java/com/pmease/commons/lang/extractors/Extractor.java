package com.pmease.commons.lang.extractors;

import java.util.List;

public interface Extractor {
	
	List<Symbol> extract(String text) throws ExtractException;
	
	boolean accept(String fileName);
	
	int getVersion();
}
