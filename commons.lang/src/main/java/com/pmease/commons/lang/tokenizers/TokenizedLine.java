package com.pmease.commons.lang.tokenizers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class TokenizedLine implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<Token> tokens = new ArrayList<>();

	public List<Token> getTokens() {
		return tokens;
	}

	public void setTokens(List<Token> tokens) {
		this.tokens = tokens;
	}
	
	public String toHtml(int tabSize) {
		StringBuilder html = new StringBuilder();
	    int col = 0;
	    for (Token token: tokens) {
	    	StringBuilder text = new StringBuilder();
	    	for (int pos = 0; ;) {
	    		int idx = token.text().indexOf('\t', pos);
			    if (idx == -1) {
			    	text.append(token.text().substring(pos));
			        col += token.text().length() - pos;
			        break;
			    } else {
			    	col += idx - pos;
			        text.append(token.text().substring(pos, idx));
			        int size = tabSize - col % tabSize;
			        col += size;
			        for (int i=0; i<size; ++i) 
			        	text.append(" ");
			          	pos = idx + 1;
			    }
	    	}

	    	String escapedText = StringEscapeUtils.escapeHtml4(token.text());
		    if (token.style().length() != 0) {
		    	html.append(String.format("<span class='%s'>%s</span>", 
		    			"cm-" + token.style().replaceAll(" +", " cm-"), escapedText));
		    } else {
		    	html.append(escapedText);
		    }
	    }
	    
	    return html.toString();
	}

	@Override
	public String toString() {
		return tokens.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof TokenizedLine))
			return false;
		if (this == other)
			return true;
		TokenizedLine otherLine = (TokenizedLine) other;
		return getTokens().equals(otherLine.getTokens());
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(getTokens()).toHashCode();
	}
	
}
