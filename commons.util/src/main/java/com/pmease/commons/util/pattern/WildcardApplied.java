package com.pmease.commons.util.pattern;

import java.io.Serializable;

public class WildcardApplied implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String text;
	
	private final int from;
	
	private final int to;
	
	public WildcardApplied(String text, int from, int to) {
		this.text = text;
		this.from = from;
		this.to = to;
	}

	public String getText() {
		return text;
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}
	
	@Override
	public String toString() {
		return text + ":" + from + "-" + to;
	}
}
