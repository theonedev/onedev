package com.pmease.commons.antlr.codeassist;

import java.io.Serializable;

import javax.annotation.Nullable;

public class InputCompletion implements Serializable {

	private static final long serialVersionUID = 1L;

	private int replaceBegin;
	
	private int replaceEnd;
	
	private String replaceContent;
	
	private int caret;
	
	private String description;
	
	public InputCompletion(int replaceBegin, int replaceEnd, String replaceContent, 
			int caret, @Nullable String description) {
		this.replaceBegin = replaceBegin;
		this.replaceEnd = replaceEnd;
		this.replaceContent = replaceContent;
		this.caret = caret;
		this.description = description;
	}
	
	public int getReplaceBegin() {
		return replaceBegin;
	}

	public int getReplaceEnd() {
		return replaceEnd;
	}

	public String getReplaceContent() {
		return replaceContent;
	}

	public int getCaret() {
		return caret;
	}

	public String getDescription() {
		return description;
	}
	
	public InputStatus complete(InputStatus inputStatus) {
		String beforeContent = inputStatus.getContent().substring(0, replaceBegin);
		String afterContent = inputStatus.getContent().substring(replaceEnd, inputStatus.getContent().length());
		return new InputStatus(beforeContent + replaceContent + afterContent, caret);
	}
	
}
