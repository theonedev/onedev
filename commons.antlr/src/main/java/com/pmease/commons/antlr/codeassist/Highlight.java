package com.pmease.commons.antlr.codeassist;

import java.io.Serializable;

public class Highlight implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int from;
	
	private final int to;
	
	public Highlight(int from, int to) {
		this.from = from;
		this.to = to;
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}
	
}
