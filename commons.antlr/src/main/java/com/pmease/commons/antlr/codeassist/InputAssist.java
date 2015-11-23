package com.pmease.commons.antlr.codeassist;

import java.io.Serializable;

public class InputAssist implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String content;
	
	private final int caret;
	
	public InputAssist(String content, int caret) {
		this.content = content;
		this.caret = caret;
	}
	
	public String getContent() {
		return content;
	}

	public int getCaret() {
		return caret;
	}

}
