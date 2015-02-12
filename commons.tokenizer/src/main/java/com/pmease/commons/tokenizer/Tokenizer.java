package com.pmease.commons.tokenizer;

import java.util.List;

public interface Tokenizer {
	
	List<TokenizedLine> tokenize(List<String> lines);
	
	boolean accept(String fileName);
	
}
