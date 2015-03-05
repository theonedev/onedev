package com.pmease.commons.lang.tokenizer;

import java.util.List;

public interface Tokenizer {
	
	List<TokenizedLine> tokenize(List<String> lines);
	
	boolean accept(String fileName);
	
}
