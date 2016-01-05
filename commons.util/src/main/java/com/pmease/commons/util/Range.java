package com.pmease.commons.util;

import java.io.Serializable;

public class Range implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int from;
	
	private final int to;
	
	public Range(int from, int to) {
		this.from = from;
		this.to = to;
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}
	
	@Override
	public String toString() {
		return from+ "-" + to;
	}
	
}
