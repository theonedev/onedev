package com.pmease.commons.lang;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

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
	public int hashCode() {
		return tokens.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
}
