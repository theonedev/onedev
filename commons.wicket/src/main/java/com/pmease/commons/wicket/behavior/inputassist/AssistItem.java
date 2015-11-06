package com.pmease.commons.wicket.behavior.inputassist;

import java.io.Serializable;

public class AssistItem implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String input;
	
	private final int cursor;
	
	public AssistItem(String input, int cursor) {
		this.input = input;
		this.cursor = cursor;
	}
	
	public String getInput() {
		return input;
	}

	public int getCursor() {
		return cursor;
	}

}
