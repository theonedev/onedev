package com.pmease.gitplex.search;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import com.pmease.commons.lang.LangToken;

public class LangTokenStream extends TokenStream {

	private final CharTermAttribute termAttr = addAttribute(CharTermAttribute.class);
	
	private final List<LangToken> tokens;
	
	private int index;
	
	public LangTokenStream(List<LangToken> tokens) {
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
			 termAttr.append(tokens.get(index).getText());
			 index++;
			 return true;
		 } else {
			 return false;
		 }	
	}

}
