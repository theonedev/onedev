package com.pmease.gitplex.web.page.test;

import java.io.Serializable;

public class InputError implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int from;
	
	private final int to;
	
	private final String message;

	public InputError(int from, int to, String message) {
		this.from = from;
		this.to = to;
		this.message = message;
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}

	public String getMessage() {
		return message;
	}
	
}
