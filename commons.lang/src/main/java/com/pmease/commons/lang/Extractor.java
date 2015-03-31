package com.pmease.commons.lang;

import java.util.List;

public interface Extractor {
	
	List<Symbol> extract(String text) throws ExtractException;
	
	boolean accept(String fileName);
	
	int getVersion();
}
