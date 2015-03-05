package com.pmease.commons.lang;

import java.util.List;

public interface Tokenizer {
	
	List<TokenizedLine> tokenize(List<String> lines);
	
	boolean accept(String fileName);
	
}
