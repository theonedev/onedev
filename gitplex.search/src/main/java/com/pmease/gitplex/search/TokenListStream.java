package com.pmease.gitplex.search;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class TokenListStream extends TokenStream {

	private final CharTermAttribute termAttr = addAttribute(CharTermAttribute.class);
	
	private final List<String> tokens;
	
	private int index;
	
	public TokenListStream(List<String> tokens) {
		this.tokens = tokens;
	}
	
	@Override
	public void reset() throws IOException {
		super.reset();
		index = 0;
	}

	@Override
	public boolean incrementToken() throws IOException {
		 clearAttributes();
		 if (index < tokens.size()) {
			 termAttr.setEmpty();
			 termAttr.append(tokens.get(index));
			 index++;
			 return true;
		 } else {
			 return false;
		 }	
	}

}
