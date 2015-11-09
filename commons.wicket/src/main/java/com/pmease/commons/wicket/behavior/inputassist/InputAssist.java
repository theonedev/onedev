package com.pmease.commons.wicket.behavior.inputassist;

import java.io.Serializable;

public class InputAssist implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String input;
	
	private final int caret;
	
	public InputAssist(String input, int caret) {
		this.input = input;
		this.caret = caret;
	}
	
	public String getInput() {
		return input;
	}

	public int getCaret() {
		return caret;
	}

}
