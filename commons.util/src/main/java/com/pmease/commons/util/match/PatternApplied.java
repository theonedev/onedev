package com.pmease.commons.util.match;

import java.io.Serializable;

import com.pmease.commons.util.Range;

public class PatternApplied implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String text;
	
	private final Range matchRange;
	
	public PatternApplied(String text, Range matchRange) {
		this.text = text;
		this.matchRange = matchRange;
	}

	public String getText() {
		return text;
	}

	public Range getMatchRange() {
		return matchRange;
	}

	@Override
	public String toString() {
		return text + ":" + matchRange;
	}
}
