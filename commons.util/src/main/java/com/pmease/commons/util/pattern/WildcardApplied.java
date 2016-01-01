package com.pmease.commons.util.pattern;

import java.io.Serializable;

public class WildcardApplied implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String text;
	
	private final Highlight highlight;
	
	public WildcardApplied(String text, Highlight highlight) {
		this.text = text;
		this.highlight = highlight;
	}

	public String getText() {
		return text;
	}

	public Highlight getHighlight() {
		return highlight;
	}

	@Override
	public String toString() {
		return text + ":" + highlight;
	}
}
