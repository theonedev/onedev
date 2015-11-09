package com.pmease.commons.wicket.behavior.inputassist;

import java.io.Serializable;

public class InputError implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int from;
	
	private final int to;
	
	public InputError(int from, int to) {
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
