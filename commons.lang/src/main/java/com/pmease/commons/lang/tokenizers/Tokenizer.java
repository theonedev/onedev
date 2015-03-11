package com.pmease.commons.lang.tokenizers;

import java.util.List;

public interface Tokenizer {
	
	List<TokenizedLine> tokenize(List<String> lines);
	
	boolean accept(String fileName);
	
}
