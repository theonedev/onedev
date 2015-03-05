package com.pmease.commons.lang;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DefaultTokenizers implements Tokenizers {
	
	private final Set<Tokenizer> tokenizers;
	
	@Inject
	public DefaultTokenizers(Set<Tokenizer> tokenizers) {
		this.tokenizers = tokenizers;
	}

	@Override
	public List<TokenizedLine> tokenize(List<String> lines, String fileName) {
		for (Tokenizer tokenizer: tokenizers) {
			if (tokenizer.accept(fileName))
				return tokenizer.tokenize(lines);
		}
		return null;
	}

}
