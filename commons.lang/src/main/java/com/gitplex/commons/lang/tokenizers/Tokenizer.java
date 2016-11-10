package com.gitplex.commons.lang.tokenizers;

import java.util.List;

public interface Tokenizer {
	
	List<List<CmToken>> tokenize(List<String> lines);
	
	boolean accept(String fileName);
	
}
