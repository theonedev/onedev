package com.pmease.commons.lang.tokenizers;

import java.util.List;

public interface Tokenizer {
	
	List<List<Token>> tokenize(List<String> lines);
	
	boolean accept(String fileName);
	
}
