package com.pmease.commons.antlr.codeassist;

public class CaretAwareText {
	
	private final String content;
	
	private final int caret;
	
	public CaretAwareText(String content, int caret) {
		this.content = content;
		this.caret = caret;
	}
	
	public CaretAwareText(String content) {
		this(content, content.length());
	}

	public String getContent() {
		return content;
	}

	public int getCaret() {
		return caret;
	}
	
	public String getContentBeforeCaret() {
		return content.substring(0, caret);
	}
	
	public String getContentAfterCaret() {
		return content.substring(caret, content.length());
	}
	
	@Override
	public String toString() {
		return content + ":" + caret;
	}
}
