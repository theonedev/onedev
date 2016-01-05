package com.pmease.commons.antlr.codeassist;

import java.io.Serializable;

import javax.annotation.Nullable;

import com.pmease.commons.util.Range;

public class InputCompletion implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int replaceBegin;
	
	private final int replaceEnd;
	
	private final String replaceContent;
	
	private final int caret;
	
	private final String description;
	
	private final Range matchRange;
	
	public InputCompletion(int replaceBegin, int replaceEnd, String replaceContent, 
			int caret, @Nullable String description, @Nullable Range matchRange) {
		this.replaceBegin = replaceBegin;
		this.replaceEnd = replaceEnd;
		this.replaceContent = replaceContent;
		this.caret = caret;
		this.description = description;
		this.matchRange = matchRange;
	}
	
	public InputCompletion(InputCompletion completion) {
		this(completion.getReplaceBegin(), completion.getReplaceEnd(), completion.getReplaceContent(), 
				completion.getCaret(), completion.getDescription(), completion.getMatchRange());
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
	
	public Range getMatchRange() {
		return matchRange;
	}

	public InputStatus complete(InputStatus inputStatus) {
		String beforeContent = inputStatus.getContent().substring(0, replaceBegin);
		String afterContent = inputStatus.getContent().substring(replaceEnd, inputStatus.getContent().length());
		return new InputStatus(beforeContent + replaceContent + afterContent, caret);
	}
	
}
